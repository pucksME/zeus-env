import { Inject, Injectable, InternalServerErrorException, NotFoundException } from "@nestjs/common";
import { ProjectDataService } from "../data/project-data.service";
import { CreateProjectDto } from "../dtos/create-project.dto";
import { Project } from "../entities/project.entity";
import { ProjectUtils } from "../project.utils";
import { User } from "../../user/entities/user.entity";
import { DesignerWorkspace } from "../../designer/entities/designer-workspace.entity";
import { View } from "../../designer/entities/view.entity";
import { ViewType } from "../../designer/enums/view-type.enum";
import { ViewUtils } from "../../designer/view.utils";
import { ProjectDto } from "../dtos/project.dto";
import { WorkspaceDesignerDto } from "../../designer/dtos/workspace-designer.dto";
import { DesignerWorkspaceUtils } from "../../designer/designer-workspace.utils";
import { UserProjectAssignment } from "../../user/entities/user-project-assignment.entity";
import { DesignerPermissionToken } from "../../designer/enums/designer-permission-token.enum";
import { ProjectPermissionToken } from "../enums/project-permission-token.enum";
import { REQUEST } from "@nestjs/core";
import { RequestKeys } from "../../../enums/request-keys.enum";
import {
  UserProjectAssignmentDataService
} from "../../user/data/user-project-assignment-data/user-project-assignment-data.service";
import {
  DesignerWorkspaceDataService
} from "../../designer/data/designer-workspace-data/designer-workspace-data.service";
import { ProjectGalleryDto } from "../dtos/project-gallery.dto";
import { UpdateProjectDto } from "../dtos/update-project.dto";
import { UpdatedProjectDto } from "../dtos/updated-project.dto";
import { VisualizerPermissionToken } from "../../visualizer/enums/visualizer-permission-token.enum";
import { ComponentDataService } from "../../designer/data/component-data/component-data.service";
import {
  BlueprintComponentDataService
} from "../../designer/data/blueprint-component-data/blueprint-component-data.service";
import { ComponentUtils } from "../../designer/component.utils";
import { AppUtils } from "../../../app.utils";
import { ExportProjectDto } from "../dtos/export-project.dto";
import { ZeusCompilerApplicationApi } from "../../../../gen/thunder-api-client";
import { ExportedProjectDto } from "../dtos/exported-project.dto";
import { ExportedProject } from "../entities/exported-project.entity";
import * as archiver from "archiver";
import { Archiver } from "archiver";
import { ExportRainProjectDto } from "../dtos/export-rain-project.dto";
import { v4 as generateUuid } from "uuid";
import { ExportTarget } from "../enums/export-target.enum";
import { Monitor } from "../enums/monitor.enum";

@Injectable()
export class ProjectService {
  constructor(
    @Inject(REQUEST)
    private readonly req,
    private readonly projectDataService: ProjectDataService,
    private readonly userProjectAssignmentDataService: UserProjectAssignmentDataService,
    private readonly workspaceDataService: DesignerWorkspaceDataService,
    private readonly componentDataService: ComponentDataService,
    private readonly blueprintComponentDataService: BlueprintComponentDataService
  ) {
  }

  private thunderApplicationApi = new ZeusCompilerApplicationApi();

  async save(createProjectDto: CreateProjectDto, userUuid: string): Promise<ProjectDto> {

    const project = new Project();
    project.name = createProjectDto.name;

    if (createProjectDto.description !== undefined) {
      project.description = createProjectDto.description;
    }

    // Initialize designer workspace
    const designerWorkspace = new DesignerWorkspace();
    designerWorkspace.type = createProjectDto.type;

    // Add a view
    const view = new View();
    view.sorting = 0;
    view.type = ViewType.PAGE;
    // Set view's position
    const defaultPosition = ViewUtils.getDefaultViewPosition(createProjectDto.type);
    view.positionX = defaultPosition.positionX;
    view.positionY = defaultPosition.positionY;
    // Get view's default dimensions
    const defaultDimensions = ViewUtils.getDefaultViewDimensions(designerWorkspace.type, view.type);
    view.height = defaultDimensions.height;
    view.width = defaultDimensions.width;

    designerWorkspace.views = [view];
    project.designerWorkspace = designerWorkspace;

    // assign user to project
    const userProjectAssignment = new UserProjectAssignment();
    userProjectAssignment.user = { uuid: userUuid } as User;
    userProjectAssignment.projectPermission = ProjectPermissionToken.OWN;
    // designer assignment
    userProjectAssignment.designerPermission = DesignerPermissionToken.OWN;
    // visualizer assignment
    userProjectAssignment.visualizerPermission = VisualizerPermissionToken.OWN;
    // TODO: put these values into a .json
    userProjectAssignment.designerPositionX = 0;
    userProjectAssignment.designerPositionY = 0;
    userProjectAssignment.designerScale = 0.75;
    userProjectAssignment.project = project;

    project.userAssignments = [userProjectAssignment];

    const savedProject = await this.projectDataService.save(project);
    return ProjectUtils.buildProjectDto(savedProject);

  }

  async find(projectUuid: string): Promise<ProjectDto> {

    const project = await this.projectDataService.findOne(projectUuid, ['designerWorkspace']);

    if (project === undefined) {
      throw new NotFoundException(`There was no project with the uuid ${projectUuid}`);
    }

    return ProjectUtils.buildProjectDto(project);

  }

  async findWorkspace(
    projectUuid: string, requestingUserProjectAssignment: UserProjectAssignment
  ): Promise<WorkspaceDesignerDto> {

    const project = await this.projectDataService.findOne(
      projectUuid,
      [
        'designerWorkspace',
        'designerWorkspace.views',
        'designerWorkspace.views.components',
        'designerWorkspace.views.components.shapes'
      ]
    );

    if (project === undefined) {
      throw new NotFoundException(`There was no workspace with project uuid ${projectUuid}`);
    }

    if (!project.designerWorkspace) {
      throw new InternalServerErrorException(`The project with uuid ${projectUuid} had no workspace`);
    }

    return DesignerWorkspaceUtils.buildWorkspaceDto(
      project.designerWorkspace,
      {
        positionX: requestingUserProjectAssignment.designerPositionX,
        positionY: requestingUserProjectAssignment.designerPositionY,
        scale: requestingUserProjectAssignment.designerScale
      }
    );

  }

  async findProjects(): Promise<ProjectGalleryDto[]> {
    const user = this.req[RequestKeys.USER];

    if (user === undefined) {
      throw new InternalServerErrorException('Could not get projects: user was not injected');
    }

    let components = await this.componentDataService.findTrees([
      'view',
      'blueprintComponent',
      'shapes',
      'componentMutations',
      'componentMutations.blueprintComponent',
      'shapeMutations',
      'shapeMutations.shape'
    ]);

    components = ComponentUtils.injectBlueprintComponents(
      components, await this.blueprintComponentDataService.findTrees(['shapes'])
    );

    // TODO: optimize this?
    const userProjectAssignments =  (await this.userProjectAssignmentDataService.find(
      user.sub,
      ['project',
        'project.designerWorkspace',
        'project.designerWorkspace.views',
        'project.designerWorkspace.views.components'
        // 'project.designerWorkspace.views.components',
        // 'project.designerWorkspace.views.components.shapes',
        // 'project.designerWorkspace.views.components.blueprintComponent',
        // 'project.designerWorkspace.views.components.blueprintComponent.shapes'
      ]
    ));

    for (const userProjectAssignment of userProjectAssignments) {
      userProjectAssignment.project.designerWorkspace.views = AppUtils.sort<View>(
        userProjectAssignment.project.designerWorkspace.views
      ).filter(view => view.components.length !== 0);

      if (userProjectAssignment.project.designerWorkspace.views.length === 0) {
        continue;
      }

      const view = userProjectAssignment.project.designerWorkspace.views[0];

      // TODO: handle sorting
      view.components = components.filter(component => component.view.uuid === view.uuid);
      userProjectAssignment.project.designerWorkspace.views = [view];
    }

    return userProjectAssignments.map(
      userProjectAssignment => ProjectUtils.buildProjectGalleryDto(userProjectAssignment.project)
    );
  }

  async update(projectUuid: string, updateProjectDto: UpdateProjectDto): Promise<UpdatedProjectDto> {

    let project = this.req[RequestKeys.PROJECT];

    if (project === undefined) {
      throw new InternalServerErrorException('Could not update project: project was not injected');
    }

    if (updateProjectDto.name !== undefined) {
      project.name = updateProjectDto.name;
    }

    if (updateProjectDto.description !== undefined) {
      project.description = updateProjectDto.description;
    }

    project = await this.projectDataService.save(project);

    return { name: project.name, description: project.description };

  }

  async delete(projectUuid: string): Promise<void> {

    const project = this.req[RequestKeys.PROJECT];

    if (project === undefined) {
      throw new InternalServerErrorException('Could not delete project: project was not injected');
    }

    const deleteResult = await this.projectDataService.delete(projectUuid);

    if (isNaN(deleteResult.affected) || deleteResult.affected === 0) {
      throw new NotFoundException('Could not delete project: project not found');
    }

  }

  async export(projectUuid: string, exportProjectDto: ExportProjectDto): Promise<ExportedProjectDto> {
    const project: Project | undefined = this.req[RequestKeys.PROJECT];

    if (project === undefined) {
      throw new InternalServerErrorException('Could not export project: project was not injected');
    }

    let componentTrees = await this.componentDataService.findTrees([
      'view',
      'blueprintComponent',
      'shapes',
      'shapeMutations',
      'shapeMutations.shape',
      'componentMutations',
      'componentMutations.blueprintComponent',
      'workspace',
      'workspace.codeModuleInstances',
      'workspace.codeModuleInstances.module',
      'workspace.codeModuleInstancesConnections'
    ]);

    const blueprintComponentTrees = await this.blueprintComponentDataService.findTrees(['workspace', 'shapes']);

    project.designerWorkspace.blueprintComponents = blueprintComponentTrees.filter(
      blueprintComponentTree => blueprintComponentTree.workspace.uuid === project.designerWorkspace.uuid
    );

    componentTrees = ComponentUtils.injectBlueprintComponents(componentTrees, blueprintComponentTrees);

    for (const view of project.designerWorkspace.views) {
      view.components = [];
    }

    for (const componentTree of componentTrees) {
      const viewIndex = project.designerWorkspace.views.findIndex(view => view.uuid === componentTree.view.uuid);

      if (viewIndex === -1) {
        continue;
      }

      project.designerWorkspace.views[viewIndex].components.push(componentTree);
    }

    const exportedProjectDto = ProjectUtils.buildExportedProjectDto(
      project.uuid,
      exportProjectDto.exportTarget,
      (await this.thunderApplicationApi.exportProject(
        ProjectUtils.buildExportProjectDto(project, exportProjectDto.exportTarget)
      )).data);

    const exportedProject = new ExportedProject();
    exportedProject.project = new Project();
    exportedProject.project.uuid = project.uuid;
    exportedProject.exportedFiles = exportedProjectDto.exportedFileDtos;
    exportedProject.exportedErrors = exportedProjectDto.errors;
    exportedProject.exportTarget = exportProjectDto.exportTarget;

    if (project.exportedProject) {
      await this.projectDataService.deleteExportedProject(project.exportedProject.uuid);
    }

    await this.projectDataService.saveExportedProject(exportedProject);
    return exportedProjectDto;
  }

  async deleteExport(projectUuid: string): Promise<void> {
    const project: Project | undefined = this.req[RequestKeys.PROJECT];

    if (project === undefined) {
      throw new InternalServerErrorException('Could not delete exported project: project was not injected');
    }

    if (project.exportedProject === undefined) {
      throw new InternalServerErrorException('Could not delete exported project: exported project was not injected');
    }

    await this.projectDataService.deleteExportedProject(project.exportedProject.uuid);
  }

  async download(projectUuid: string): Promise<Archiver> {
    const project = await this.projectDataService.findOne(projectUuid, ['exportedProject']);

    if (project === undefined) {
      throw new InternalServerErrorException('Could not download project: project was not injected');
    }

    if (!project.exportedProject) {
      throw new NotFoundException('Could not download project: project was not exported');
    }

    // https://stackoverflow.com/q/66836740 [accessed 19/9/2023, 10:15]
    let archive = archiver('zip');
    archive = ProjectUtils.buildExportProjectFramework(archive, project.exportedProject.exportTarget);

    for (const exportedFile of project.exportedProject.exportedFiles) {
      archive.append(
        exportedFile.code,
        {name: ProjectUtils.getExportedFilePath(project.exportedProject.exportTarget) + exportedFile.filename}
      );
    }

    archive = ProjectUtils.buildExportProjectErrors(archive, project.exportedProject.exportedErrors);

    await archive.finalize();
    return archive;
  }

  async findExportedProjects(): Promise<ExportedProjectDto[]> {
    const user = this.req[RequestKeys.USER];

    if (user === undefined) {
      throw new InternalServerErrorException('Could not find exported projects: user was not injected');
    }

    const userProjectAssignments = await this.userProjectAssignmentDataService.find(
      user.sub,
      [
        'project',
        'project.exportedProject'
      ]
    );

    return userProjectAssignments
      .filter(projectAssignment => projectAssignment.project.exportedProject !== null)
      .map(projectAssignment => ProjectUtils.buildExportedProjectDto(
        projectAssignment.project.uuid,
        projectAssignment.project.exportedProject.exportTarget,
        {
          exportedClientDtos: [{
            exportedFileDtos: projectAssignment.project.exportedProject.exportedFiles.map(exportedFile => ({
              code: exportedFile.code,
              filename: exportedFile.filename
            }))
          }],
          exportedServerDtos: [],
          errors: projectAssignment.project.exportedProject.exportedErrors
        })
      );
  }

  async exportRain(exportRainProjectDto: ExportRainProjectDto): Promise<ExportedProjectDto> {
    const exportedProjectDto = ProjectUtils.buildExportedProjectDto(
      generateUuid(),
      exportRainProjectDto.exportTarget,
      (await this.thunderApplicationApi.translateProject({
        code: exportRainProjectDto.code,
        exportTarget: ProjectUtils.buildZeusCompilerExportTarget(exportRainProjectDto.exportTarget)
      })).data
    )

    const exportedProject = new ExportedProject();
    exportedProject.project = new Project();
    exportedProject.project.uuid = exportedProjectDto.uuid;
    exportedProject.exportedFiles = exportedProjectDto.exportedFileDtos;
    exportedProject.exportedErrors = exportedProjectDto.errors;
    exportedProject.exportTarget = exportRainProjectDto.exportTarget;
    await this.projectDataService.saveExportedProject(exportedProject);

    await this.projectDataService.saveExportedProject(exportedProject);
    return exportedProjectDto;
  }

  async downloadRain(exportedProjectUuid: string): Promise<Archiver> {
    const exportedProject = await this.projectDataService.findOneExportedProject(exportedProjectUuid);

    if (exportedProject === undefined) {
      throw new NotFoundException('Could not download exported project: exported project did not exist');
    }

    return null;
  }

  async packageRain(exportRainProjectDto: ExportRainProjectDto): Promise<Archiver> {
    const exportedProject = (await this.thunderApplicationApi.translateProject({
      code: exportRainProjectDto.code,
      exportTarget: ProjectUtils.buildZeusCompilerExportTarget(exportRainProjectDto.exportTarget)
    })).data;

    let archive = archiver('zip');

    for (const exportedClientDto of exportedProject.exportedClientDtos) {
      const clientArchivePath = `client-${exportedClientDto.name}/`;
      archive = ProjectUtils.buildExportProjectFramework(archive, ExportTarget.REACT_TYPESCRIPT, clientArchivePath);
      for (const exportedFileDto of exportedClientDto.exportedFileDtos) {
        archive.append(exportedFileDto.code, {name: clientArchivePath + 'src/' + exportedFileDto.filename});
      }
    }

    for (const exportedServerDto of exportedProject.exportedServerDtos) {
      const serverArchivePath = `server-${exportedServerDto.name}/`;
      archive = ProjectUtils.buildExportProjectFramework(archive, ExportTarget.EXPRESS_TYPESCRIPT, serverArchivePath)
      for (const exportedFileDto of exportedServerDto.exportedFileDtos) {
        archive.append(exportedFileDto.code, {name: serverArchivePath + exportedFileDto.filename});
      }

      archive = ProjectUtils.buildExportProjectMonitorAdapter(archive, Monitor.BOOTS, serverArchivePath + 'adapters/')
      archive = ProjectUtils.buildExportProjectMonitorAdapter(archive, Monitor.UMBRELLA, serverArchivePath + 'adapters/')

      for (const bootsGeneratorFile of exportedServerDto.exportedBootsMonitorFilesDto) {
        archive.append(bootsGeneratorFile.code, {name: `${serverArchivePath}boots-generators/${bootsGeneratorFile.filename}`})
      }
    }

    archive = ProjectUtils.buildExportProjectMonitor(archive, Monitor.BOOTS, 'monitors/boots/');
    archive = ProjectUtils.buildExportProjectMonitor(archive, Monitor.UMBRELLA, 'monitors/umbrella/');

    for (const umbrellaMonitorSpecification of exportedProject.exportedServerDtos.flatMap(
      server => server.umbrellaSpecifications
    )) {
      archive.append(umbrellaMonitorSpecification.code, {
        name : `monitors/umbrella/src/main/java/zeus/specification/${umbrellaMonitorSpecification.filename}`
      });

    }

    if (exportedProject.umbrellaSpecificationInitialization !== null) {
      archive.append(exportedProject.umbrellaSpecificationInitialization.code, {
        name: `monitors/umbrella/src/main/java/zeus/${exportedProject.umbrellaSpecificationInitialization.filename}`
      });
    }

    archive = ProjectUtils.buildExportProjectErrors(archive, exportedProject.errors)

    await archive.finalize();
    return archive;
  }
}
