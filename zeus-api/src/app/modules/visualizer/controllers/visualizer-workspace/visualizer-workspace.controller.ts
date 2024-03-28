import { Controller, Delete, Get, Param, ParseUUIDPipe, Req, UseGuards} from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiForbiddenResponse,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags
} from '@nestjs/swagger';
import {
  JwtAuthenticationGuard
} from '../../../authentication/guards/jwt-authentication-guard/jwt-authentication.guard';
import { PermissionGuard } from '../../../../guards/permission.guard';
import { VisualizerWorkspaceService } from '../../services/visualizer-workspace/visualizer-workspace.service';
import { IdentifierLocation } from '../../../designer/enums/identifier-location.enum';
import { WorkspaceVisualizerDto } from '../../dtos/workspace-visualizer.dto';
import { HasVisualizerPermission } from '../../../../guards/has-visualizer-permission.decorator';
import { VisualizerPermissionToken } from '../../enums/visualizer-permission-token.enum';

@ApiTags('VisualizerWorkspace')
@ApiBearerAuth()
@UseGuards(JwtAuthenticationGuard, PermissionGuard)
@Controller('visualizerWorkspace')
export class VisualizerWorkspaceController {

  constructor(
    private readonly workspaceService: VisualizerWorkspaceService
  ) {
  }

  @ApiOperation({
    summary: 'Finds a workspace by its component uuid',
    description: 'Finds a workspace by its component uuid and returns it including its code module instances'
  })
  @ApiOkResponse({
    type: WorkspaceVisualizerDto,
    description: 'The workspace was found successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no component with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to see the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter componentUuid was not a valid UUID' })
  @HasVisualizerPermission({
    token: VisualizerPermissionToken.READ,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid',
    relations: [
      'workspace',
      'workspace.codeModuleInstances',
      'workspace.codeModuleInstances.module'
    ]
  })
  @Get(':componentUuid')
  findWorkspaceByComponentUuid(
    @Req() req, @Param('componentUuid', ParseUUIDPipe) componentUuid: string
  ): Promise<WorkspaceVisualizerDto> {
    return this.workspaceService.findByComponentUuid(componentUuid);
  }

  @ApiOperation({
    summary: 'Deletes a workspace',
    description: 'Deletes a workspace and its code module instances'
  })
  @ApiOkResponse({ description: 'The workspace was deleted successfully' })
  @ApiNotFoundResponse({ description: 'There was no component with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasVisualizerPermission({
    token: VisualizerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid',
    relations: ['workspace']
  })
  @Delete(':componentUuid')
  deleteComponents(
    @Req() req, @Param('componentUuid', ParseUUIDPipe) componentUuid: string
  ): Promise<void> {
    return this.workspaceService.deleteByComponentUuid(componentUuid);
  }

}
