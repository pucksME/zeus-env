import {
  CreateProjectDto, ExportedProjectDto, ExportProjectDto,
  ProjectApi,
  ProjectDto,
  ProjectGalleryDto, UpdatedProjectDto,
  UpdateProjectDto
} from '../../../../gen/api-client';
import { AppUtils } from '../../../app.utils';

export abstract class ProjectService {
  private static projectApi = new ProjectApi(AppUtils.getApiConfiguration());

  static async getProject(projectUuid: string): Promise<ProjectDto> {
    return (await ProjectService.projectApi.projectControllerFind(projectUuid)).data;
  }

  static async getProjects(): Promise<ProjectGalleryDto[]> {
    return (await ProjectService.projectApi.projectControllerFindProjects()).data;
  }

  static async saveProject(createProjectDto: CreateProjectDto): Promise<ProjectDto> {
    return (await ProjectService.projectApi.projectControllerSave(createProjectDto)).data;
  }

  static async deleteProject(projectUuid: string): Promise<void> {
    await ProjectService.projectApi.projectControllerDelete(projectUuid);
  }

  static async updateProject(projectUuid: string, updateProjectDto: UpdateProjectDto): Promise<UpdatedProjectDto> {
    return (await ProjectService.projectApi.projectControllerUpdate(projectUuid, updateProjectDto)).data;
  }

  static async exportProject(projectUuid: string, exportProjectDto: ExportProjectDto): Promise<void> {
    await ProjectService.projectApi.projectControllerExport(projectUuid, exportProjectDto);
  }

  static async deleteExportedProject(projectUuid: string): Promise<void> {
    await ProjectService.projectApi.projectControllerDeleteExport(projectUuid);
  }

  static async findExportedProjects(): Promise<ExportedProjectDto[]> {
    return (await ProjectService.projectApi.projectControllerFindExportedProjects()).data;
  }
}
