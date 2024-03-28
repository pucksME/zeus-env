import { Body, Controller, Get, Param, ParseUUIDPipe, Put, Req, UseGuards, ValidationPipe } from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiForbiddenResponse,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags
} from '@nestjs/swagger';
import { JwtAuthenticationGuard } from '../../../authentication/guards/jwt-authentication-guard/jwt-authentication.guard';
import { DesignerWorkspaceService } from '../../services/designer-workspace/designer-workspace.service';
import { WorkspaceDesignerDto } from '../../dtos/workspace-designer.dto';
import { HasDesignerPermission } from '../../../../guards/has-designer-permission.decorator';
import { PermissionGuard } from '../../../../guards/permission.guard';
import { DesignerPermissionToken } from '../../enums/designer-permission-token.enum';
import { IdentifierLocation } from '../../enums/identifier-location.enum';
import { RequestKeys } from '../../../../enums/request-keys.enum';
import { UpdateWorkspacePositionDto } from '../../dtos/update-workspace/update-workspace-position.dto';
import { UpdatedWorkspacePositionDto } from '../../dtos/update-workspace/updated-workspace-position.dto';
import { UpdatedWorkspaceScaleDto } from '../../dtos/update-workspace/updated-workspace-scale.dto';
import { UpdateWorkspaceScaleDto } from '../../dtos/update-workspace/update-workspace-scale.dto';
import { UpdateWorkspacePropertiesDto } from '../../dtos/update-workspace/update-workspace-properties.dto';
import { UpdatedWorkspacePropertiesDto } from '../../dtos/update-workspace/updated-workspace-properties.dto';

@ApiTags('DesignerWorkspace')
@ApiBearerAuth()
@UseGuards(JwtAuthenticationGuard, PermissionGuard)
@Controller('designerWorkspace')
export class DesignerWorkspaceController {

  constructor(private readonly workspaceService: DesignerWorkspaceService) {
  }

  @ApiOperation({
    summary: 'Finds a workspace by its uuid',
    description: 'Finds a workspace by its uuid and returns it including its components'
  })
  @ApiOkResponse({
    type: WorkspaceDesignerDto,
    description: 'The workspace was found successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no workspace related to the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to see the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter workspaceUuid was not a valid UUID' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.READ,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'workspaceUuid'
  })
  @Get(':workspaceUuid')
  findWorkspace(
    @Req() req, @Param('workspaceUuid', ParseUUIDPipe) workspaceUuid: string
  ): Promise<WorkspaceDesignerDto> {
    return this.workspaceService.find(workspaceUuid, req[RequestKeys.USER_PROJECT_ASSIGNMENT]);
  }

  @ApiOperation({
    summary: 'Updates the properties of a workspace',
    description: 'Updates the properties (position x, position y, scale) of a workspace'
  })
  @ApiOkResponse({
    type: UpdatedWorkspacePropertiesDto,
    description: 'The properties of the workspace was updated successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no workspace related to the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter workspaceUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'workspaceUuid'
  })
  @Put(':workspaceUuid/properties')
  updateProperties(
    @Req() req,
    @Param('workspaceUuid', ParseUUIDPipe) workspaceUuid: string,
    @Body(ValidationPipe) updateWorkspacePropertiesDto: UpdateWorkspacePropertiesDto
  ): Promise<UpdatedWorkspacePropertiesDto> {
    return this.workspaceService.updateProperties(
      updateWorkspacePropertiesDto, req[RequestKeys.USER_PROJECT_ASSIGNMENT]
    );
  }

  @ApiOperation({
    summary: 'Updates the position of a workspace',
    description: 'Updates the position (x, y) of a workspace'
  })
  @ApiOkResponse({
    type: UpdatedWorkspacePositionDto,
    description: 'The position of the workspace was updated successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no workspace related to the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter workspaceUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'workspaceUuid'
  })
  @Put(':workspaceUuid/position')
  updatePosition(
    @Req() req,
    @Param('workspaceUuid', ParseUUIDPipe) workspaceUuid: string,
    @Body(ValidationPipe) updateWorkspacePositionDto: UpdateWorkspacePositionDto
  ): Promise<UpdatedWorkspacePositionDto> {
    return this.workspaceService.updatePosition(updateWorkspacePositionDto, req[RequestKeys.USER_PROJECT_ASSIGNMENT]);
  }

  @ApiOperation({
    summary: 'Updates the scale of a workspace',
    description: 'Updates the scale of a workspace'
  })
  @ApiOkResponse({
    type: UpdatedWorkspaceScaleDto,
    description: 'The scale of the workspace was updated successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no workspace related to the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter workspaceUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'workspaceUuid'
  })
  @Put(':workspaceUuid/scale')
  updateScale(
    @Req() req,
    @Param('workspaceUuid', ParseUUIDPipe) workspaceUuid: string,
    @Body(ValidationPipe) updateWorkspaceScaleDto: UpdateWorkspaceScaleDto
  ): Promise<UpdatedWorkspaceScaleDto> {
    return this.workspaceService.updateScale(updateWorkspaceScaleDto, req[RequestKeys.USER_PROJECT_ASSIGNMENT]);
  }


}
