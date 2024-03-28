import {
  Body,
  Controller,
  Delete,
  Get, Header,
  Param,
  ParseUUIDPipe,
  Post, Put,
  Req, Res,
  UseGuards,
  ValidationPipe
} from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiForbiddenResponse,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags
} from '@nestjs/swagger';
import { JwtAuthenticationGuard } from '../../authentication/guards/jwt-authentication-guard/jwt-authentication.guard';
import { CreateProjectDto } from '../dtos/create-project.dto';
import { ProjectService } from '../services/project.service';
import { ProjectDto } from '../dtos/project.dto';
import { WorkspaceDesignerDto } from '../../designer/dtos/workspace-designer.dto';
import { PermissionGuard } from '../../../guards/permission.guard';
import { HasDesignerPermission } from '../../../guards/has-designer-permission.decorator';
import { DesignerPermissionToken } from '../../designer/enums/designer-permission-token.enum';
import { IdentifierLocation } from '../../designer/enums/identifier-location.enum';
import { RequestKeys } from '../../../enums/request-keys.enum';
import { HasProjectPermission } from '../../../guards/has-project-permission.decorator';
import { ProjectPermissionToken } from '../enums/project-permission-token.enum';
import { ProjectGalleryDto } from '../dtos/project-gallery.dto';
import { UpdateProjectDto } from '../dtos/update-project.dto';
import { UpdatedProjectDto } from '../dtos/updated-project.dto';
import { ExportProjectDto } from '../dtos/export-project.dto';
import { ExportedProjectDto } from '../dtos/exported-project.dto';
import { Public } from '../../../guards/public.decorator';
import {ExportRainProjectDto} from "../dtos/export-rain-project.dto";

@ApiTags('Project')
@ApiBearerAuth()
@UseGuards(JwtAuthenticationGuard, PermissionGuard)
@Controller('project')
export class ProjectController {
  constructor(private readonly projectService: ProjectService) {
  }

  @ApiOperation({
    summary: 'Creates a new project',
    description: 'Creates a new project and initializes a workspace including a view'
  })
  @ApiOkResponse({
    type: ProjectDto,
    description: 'The project was saved successfully'
  })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Post()
  save(@Req() req, @Body(ValidationPipe) createProjectDto: CreateProjectDto): Promise<ProjectDto> {
    return this.projectService.save(createProjectDto, req.user.sub);
  }

  @ApiOperation({
    summary: 'Finds the user\'s projects',
    description: 'Finds the authenticated user\'s projects'
  })
  @ApiOkResponse({
    type: [ProjectGalleryDto],
    description: 'The user\'s projects were found successfully'
  })
  @Get('projects')
  findProjects(): Promise<ProjectGalleryDto[]> {
    return this.projectService.findProjects();
  }

  @ApiOperation({
    summary: 'Finds exported projects',
    description: 'Finds all exported projects of an user'
  })
  @ApiOkResponse({
    type: [ExportedProjectDto],
    description: 'The exported projects were found successfully'
  })
  @Get('exportedProjects')
  async findExportedProjects(): Promise<ExportedProjectDto[]> {
    return this.projectService.findExportedProjects();
  }

  @ApiOperation({
    summary: 'Finds a project by its uuid',
    description: 'Finds a project by its uuid and returns it including its workspace'
  })
  @ApiOkResponse({
    type: ProjectDto,
    description: 'The project was found successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no project with given project uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to see the project' })
  @ApiBadRequestResponse({ description: 'The parameter projectUuid was not a valid UUID' })
  @HasProjectPermission({
    token: ProjectPermissionToken.READ,
    keyName: 'projectUuid',
    extractFrom: IdentifierLocation.PARAMS
  })
  @Get(':projectUuid')
  find(@Param('projectUuid', ParseUUIDPipe) projectUuid: string): Promise<ProjectDto> {
    return this.projectService.find(projectUuid);
  }

  @ApiOperation({
    summary: 'Updates a project',
    description: 'Updates the given project'
  })
  @ApiOkResponse({
    type: UpdatedProjectDto,
    description: 'The project was updated successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no project with given project uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to update the project' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasProjectPermission({
    token: ProjectPermissionToken.WRITE,
    keyName: 'projectUuid',
    extractFrom: IdentifierLocation.PARAMS
  })
  @Put(':projectUuid')
  update(
    @Param('projectUuid', ParseUUIDPipe) projectUuid: string,
    @Body(ValidationPipe) updateProjectDto: UpdateProjectDto
  ): Promise<UpdatedProjectDto> {
    return this.projectService.update(projectUuid, updateProjectDto);
  }

  @ApiOperation({
    summary: 'Deletes a project',
    description: 'Deletes the project with the given uuid'
  })
  @ApiOkResponse({ description: 'The project was deleted successfully' })
  @ApiNotFoundResponse({ description: 'There was no project with given project uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to delete the project' })
  @ApiBadRequestResponse({ description: 'The parameter projectUuid was not a valid UUID' })
  @HasProjectPermission({
    token: ProjectPermissionToken.WRITE,
    keyName: 'projectUuid',
    extractFrom: IdentifierLocation.PARAMS
  })
  @Delete(':projectUuid')
  delete(@Param('projectUuid', ParseUUIDPipe) projectUuid: string): Promise<void> {
    return this.projectService.delete(projectUuid);
  }

  @ApiOperation({
    summary: 'Finds a workspace by its project uuid',
    description: 'Finds a workspace by its project uuid and returns it including its components'
  })
  @ApiOkResponse({
    type: WorkspaceDesignerDto,
    description: 'The workspace was found successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no workspace related to the given project uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to see the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter projectUuid was not a valid UUID' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.READ,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'projectUuid'
  })
  @Get('/workspace/:projectUuid')
  findWorkspace(@Req() req, @Param('projectUuid', ParseUUIDPipe) projectUuid: string): Promise<WorkspaceDesignerDto> {
    return this.projectService.findWorkspace(projectUuid, req[RequestKeys.USER_PROJECT_ASSIGNMENT]);
  }

  @ApiOperation({
    summary: 'Exports a project',
    description: 'Exports a project'
  })
  @ApiOkResponse({
    type: ExportedProjectDto,
    description: 'The project was exported successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no project with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to see the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter projectUuid was not a valid UUID' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'projectUuid',
    relations: [
      'designerWorkspace',
      'designerWorkspace.views',
      'exportedProject'
    ]
  })
  @Post(':projectUuid/export')
  export(
    @Req() req,
    @Param('projectUuid', ParseUUIDPipe) projectUuid: string,
    @Body(ValidationPipe) exportProjectDto: ExportProjectDto
  ): Promise<ExportedProjectDto> {
    return this.projectService.export(projectUuid, exportProjectDto);
  }

  @ApiOperation({
    summary: 'Deletes the export of a project',
    description: 'Deletes the export of a project'
  })
  @ApiOkResponse({
    description: 'The project export was deleted successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no project with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to see the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter projectUuid was not a valid UUID' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'projectUuid',
    relations: ['exportedProject']
  })
  @Delete(':projectUuid/export')
  deleteExport(@Param('projectUuid', ParseUUIDPipe) projectUuid: string): Promise<void> {
    return this.projectService.deleteExport(projectUuid);
  }

  @ApiOperation({
    summary: 'Downloads a project',
    description: 'Downloads a previously exported project'
  })
  @ApiOkResponse({
    description: 'The project was downloaded successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no project with the given uuid or the project was not exported' })
  //@ApiForbiddenResponse({ description: 'The requesting user was not allowed to see the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter projectUuid was not a valid UUID' })
  //@HasDesignerPermission({
  //  token: DesignerPermissionToken.READ,
  //  extractFrom: IdentifierLocation.PARAMS,
  //  keyName: 'projectUuid',
  //  relations: ['exportedProject']
  //})
  @Public()
  @Get(':projectUuid/download')
  // https://docs.nestjs.com/techniques/streaming-files [accessed 19/9/2023, 10:08]
  // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types [accessed 19/9/2023, 10:07]
  @Header('Content-Type', 'application/zip')
  @Header('Content-Disposition', 'attachment; filename="application-export.zip"')
  async download(
    @Req() req,
    @Param('projectUuid', ParseUUIDPipe) projectUuid: string,
    @Res() res
  ): Promise<void> {
    (await this.projectService.download(projectUuid)).pipe(res);
  }

  @ApiOperation({
    summary: 'Exports a rain project',
    description: 'Exports a rain project'
  })
  @ApiOkResponse({
    type: ExportedProjectDto,
    description: 'The project was exported successfully'
  })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Post('exportRain')
  exportRain(
    @Body(ValidationPipe) exportRainProjectDto: ExportRainProjectDto
  ): Promise<ExportedProjectDto> {
    return this.projectService.exportRain(exportRainProjectDto);
  }

  @ApiOperation({
    summary: 'Downloads a project',
    description: 'Downloads a previously exported project'
  })
  @ApiOkResponse({
    description: 'The project was downloaded successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no project with the given uuid or the project was not exported' })
  @ApiBadRequestResponse({ description: 'The parameter exportedProjectUuid was not a valid UUID' })
  @Public()
  @Get(':projectUuid/download')
  @Header('Content-Type', 'application/zip')
  @Header('Content-Disposition', 'attachment; filename="application-export.zip"')
  async downloadRain(
    @Req() req,
    @Param('exportedProjectUuid', ParseUUIDPipe) exportedProjectUuid: string,
    @Res() res
  ): Promise<void> {
    (await this.projectService.downloadRain(exportedProjectUuid)).pipe(res);
  }
}
