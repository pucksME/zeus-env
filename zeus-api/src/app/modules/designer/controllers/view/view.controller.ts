import { Body, Controller, Delete, Param, ParseUUIDPipe, Post, Put, UseGuards, ValidationPipe } from '@nestjs/common';
import { ViewService } from '../../services/view/view.service';
import { CreateViewDto } from '../../dtos/create-view.dto';
import { ViewDto } from '../../dtos/view.dto';
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
import { HasDesignerPermission } from '../../../../guards/has-designer-permission.decorator';
import { DesignerPermissionToken } from '../../enums/designer-permission-token.enum';
import { IdentifierLocation } from '../../enums/identifier-location.enum';
import { PermissionGuard } from '../../../../guards/permission.guard';
import { UpdateViewNameDto } from '../../dtos/update-view-name.dto';
import { ScaleViewDto } from '../../dtos/scale-view.dto';
import { TranslateViewDto } from '../../dtos/translate-view.dto';
import { ReshapeViewDto } from '../../dtos/reshape-view.dto';
import { PositionViewDto } from '../../dtos/position-view.dto';

@ApiTags('View')
@ApiBearerAuth()
@UseGuards(JwtAuthenticationGuard, PermissionGuard)
@Controller('view')
export class ViewController {

  constructor(private readonly viewService: ViewService) {
  }

  @ApiOperation({
    summary: 'Creates a new view within a workspace',
    description: 'Creates a new view within a workspace'
  })
  @ApiOkResponse({
    description: 'The view was created successfully',
    type: ViewDto
  })
  @ApiNotFoundResponse({ description: 'There was no workspace with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The chosen dimensions are out of range' })
  @ApiBadRequestResponse({ description: 'The parameter workspaceUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'workspaceUuid'
  })
  @Post(':workspaceUuid')
  save(
    @Param('workspaceUuid', ParseUUIDPipe) workspaceUuid: string, @Body(ValidationPipe) createViewDto: CreateViewDto
  ): Promise<ViewDto> {
    return this.viewService.save(workspaceUuid, createViewDto);
  }

  @ApiOperation({
    summary: 'Updates a view\'s name',
    description: 'Updates a view\'s name.'
  })
  @ApiOkResponse({
    description: 'The view\'s name was updated successfully',
    type: ViewDto
  })
  @ApiNotFoundResponse({ description: 'The view did not exist' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the view' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'viewUuid'
  })
  @Put('updateName/:viewUuid')
  updateName(
    @Param('viewUuid', ParseUUIDPipe) viewUuid: string,
    @Body(ValidationPipe) updateViewNameDto: UpdateViewNameDto
  ): Promise<ViewDto> {
    return this.viewService.updateName(viewUuid, updateViewNameDto);
  }

  @ApiOperation({
    summary: 'Sets the root view',
    description: 'Sets the root view'
  })
  @ApiOkResponse({
    description: 'The root view was set successfully',
    type: [ViewDto]
  })
  @ApiNotFoundResponse({ description: 'The view did not exist' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the view' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'viewUuid',
    relations: ['workspace.views']
  })
  @Put('setRootView/:viewUuid')
  setRootView(@Param('viewUuid', ParseUUIDPipe) viewUuid: string): Promise<ViewDto[]> {
    return this.viewService.setRootView(viewUuid);
  }

  @ApiOperation({
    summary: 'Scales a view',
    description: 'Scales a view by updating its height and width'
  })
  @ApiOkResponse({
    description: 'The view was scaled successfully',
    type: ViewDto
  })
  @ApiNotFoundResponse({description: 'For the given uuid, there was no view'})
  @ApiForbiddenResponse({description: 'The user was not allowed to edit the workspace'})
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'viewUuid'
  })
  @Put(':viewUuid/scale')
  scaleView(
    @Param('viewUuid', ParseUUIDPipe) viewUuid: string,
    @Body(ValidationPipe) scaleViewDto: ScaleViewDto
  ): Promise<ViewDto> {
    return this.viewService.scaleView(viewUuid, scaleViewDto);
  }

  @ApiOperation({
    summary: 'Translates a view',
    description: 'Translates a view by updating its x and y coordinates'
  })
  @ApiOkResponse({
    description: 'The view was translated successfully',
    type: ViewDto
  })
  @ApiNotFoundResponse({description: 'For the given uuid, there was no view'})
  @ApiForbiddenResponse({description: 'The user was not allowed to edit the workspace'})
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'viewUuid'
  })
  @Put(':viewUuid/translate')
  translateView(
    @Param('viewUuid', ParseUUIDPipe) viewUuid: string,
    @Body(ValidationPipe) translateViewDto: TranslateViewDto
  ): Promise<ViewDto> {
    return this.viewService.translateView(viewUuid, translateViewDto);
  }

  @ApiOperation({
    summary: 'Reshapes a view',
    description: 'Reshapes a view by reshaping its width and height'
  })
  @ApiOkResponse({
    description: 'The view was reshaped successfully',
    type: ViewDto
  })
  @ApiNotFoundResponse({ description: 'For the given uuid, there was no view' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'viewUuid'
  })
  @Put(':viewUuid/reshape')
  reshapeView(
    @Param('viewUuid', ParseUUIDPipe) viewUuid: string,
    @Body(ValidationPipe) reshapeViewDto: ReshapeViewDto
  ): Promise<ViewDto> {
    return this.viewService.reshapeView(viewUuid, reshapeViewDto);
  }

  @ApiOperation({
    summary: 'Updates the position of a view',
    description: 'Updates the position of a view to the provided coordinates'
  })
  @ApiOkResponse({
    description: 'The view was positioned successfully',
    type: ViewDto
  })
  @ApiNotFoundResponse({ description: 'For the given uuid, there was no view' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'viewUuid'
  })
  @Put(':viewUuid/position')
  positionView(
    @Param('viewUuid', ParseUUIDPipe) viewUuid: string,
    @Body(ValidationPipe) positionViewDto: PositionViewDto
  ): Promise<ViewDto> {
    return this.viewService.positionView(viewUuid, positionViewDto);
  }

  @ApiOperation({
    summary: 'Deletes a view',
    description: 'Deletes a view and its components'
  })
  @ApiOkResponse({
    description: 'The view was deleted successfully'
  })
  @ApiNotFoundResponse({ description: 'For the given uuid, there was no view' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'viewUuid'
  })
  @Delete(':viewUuid')
  deleteView(
    @Param('viewUuid', ParseUUIDPipe) viewUuid: string
  ): Promise<void> {
    return this.viewService.deleteView(viewUuid);
  }

}
