import { Body, Controller, Delete, Param, ParseUUIDPipe, Post, Put, UseGuards, ValidationPipe } from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBearerAuth, ApiForbiddenResponse,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags
} from '@nestjs/swagger';
import { JwtAuthenticationGuard } from '../../../authentication/guards/jwt-authentication-guard/jwt-authentication.guard';
import { CreateComponentDto } from '../../dtos/create-component.dto';
import { ComponentService } from '../../services/component/component.service';
import { ComponentDto } from '../../dtos/component.dto';
import { UpdateComponentPositionDto } from '../../dtos/update-component-position.dto';
import { UpdatedComponentPositionDto } from '../../dtos/updated-component-position.dto';
import { PermissionGuard } from '../../../../guards/permission.guard';
import { HasDesignerPermission } from '../../../../guards/has-designer-permission.decorator';
import { DesignerPermissionToken } from '../../enums/designer-permission-token.enum';
import { IdentifierLocation } from '../../enums/identifier-location.enum';
import { ScaleElementsDto } from '../../dtos/scale-elements.dto';
import { TranslateElementsDto } from '../../dtos/translate-elements.dto';
import { PositionElementsDto } from '../../dtos/position-elements.dto';
import { ReshapeElementsDto } from '../../dtos/reshape-elements.dto';
import { UpdateElementsPropertiesDto } from '../../dtos/update-elements-properties.dto';
import { DeleteElementsDto } from '../../dtos/delete-elements.dto';
import { AlignElementsDto } from '../../dtos/align-elements.dto';
import { UpdateElementSortingDto } from '../../dtos/update-element-sorting.dto';
import { UpdateComponentNameDto } from '../../dtos/update-component-name.dto';
import { UpdatedElementsDto } from '../../dtos/updated-elements.dto';
import { ResetElementsMutationsDto } from '../../dtos/reset-elements-mutations.dto';

@ApiTags('Component')
@ApiBearerAuth()
@UseGuards(JwtAuthenticationGuard, PermissionGuard)
@Controller('component')
export class ComponentController {

  constructor(private readonly componentService: ComponentService) {
  }

  @ApiOperation({
    summary: 'Creates a new component within a view',
    description: 'Creates a new component including its shapes within a view'
  })
  @ApiOkResponse({
    description: 'The component was created successfully',
    type: ComponentDto
  })
  @ApiNotFoundResponse({ description: 'There was no view with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter viewUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'viewUuid',
    relations: ['components']
  })
  @Post(':viewUuid')
  save(
    @Param('viewUuid', ParseUUIDPipe) viewUuid: string,
    @Body(ValidationPipe) createComponentDto: CreateComponentDto
  ): Promise<ComponentDto> {
    return this.componentService.save(viewUuid, createComponentDto);
  }

  @ApiOperation({
    summary: 'Creates a new component from a shape',
    description: 'Creates a new component from a given shape with the shape\'s parent as its parent'
  })
  @ApiOkResponse({
    description: 'The component was created successfully',
    type: ComponentDto
  })
  @ApiNotFoundResponse({ description: 'There was no shape with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace, the shape was part of a blueprint component or its parent had not enough shapes' })
  @ApiBadRequestResponse({ description: 'The parameter shapeUuid was not a valid UUID' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'shapeUuid',
    relations: [
      'component.blueprintComponent',
      'component.shapes'
    ]
  })
  @Post('/shape/:shapeUuid')
  saveWithShape(@Param('shapeUuid', ParseUUIDPipe) shapeUuid: string): Promise<ComponentDto> {
    return this.componentService.saveWithShape(shapeUuid);
  }

  @ApiOperation({
    summary: 'Updates the position of a component',
    description: 'Updates the position (x, y) of a component. This moves the component coordinate system including all shapes'
  })
  @ApiOkResponse({
    description: 'The position of the component was updated successfully',
    type: UpdatedComponentPositionDto
  })
  @ApiNotFoundResponse({ description: 'There was no component with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter componentUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid'
  })
  @Put(':componentUuid/position')
  updatePosition(
    @Param('componentUuid', ParseUUIDPipe) componentUuid: string,
    @Body(ValidationPipe) updateComponentPositionDto: UpdateComponentPositionDto
  ): Promise<UpdatedComponentPositionDto> {
    return this.componentService.updatePosition(componentUuid, updateComponentPositionDto);
  }

  @ApiOperation({
    summary: 'Scales components',
    description: 'Scales components by updating their position and scaling their shapes'
  })
  @ApiOkResponse({
    description: 'The components were scaled successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  // TODO: support permission checks on multiple resources
  /*
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid'
  })
   */
  @Put('scaleComponents')
  scaleComponents(
    @Body(ValidationPipe) scaleElementsDto: ScaleElementsDto
  ): Promise<UpdatedElementsDto> {
    return this.componentService.scaleComponents(scaleElementsDto);
  }

  @ApiOperation({
    summary: 'Reshapes components',
    description: 'Reshapes components by reshaping their width and height'
  })
  @ApiOkResponse({
    description: 'The components were reshaped successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  // TODO: support permission checks on multiple resources
  /*
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid'
  })
   */
  @Put('reshapeComponents')
  reshapeComponents(
    @Body(ValidationPipe) reshapeElementsDto: ReshapeElementsDto
  ): Promise<UpdatedElementsDto> {
    return this.componentService.reshapeComponents(reshapeElementsDto);
  }

  @ApiOperation({
    summary: 'Translates components',
    description: 'Translates components by adding an offset to their position'
  })
  @ApiOkResponse({
    description: 'The components were translated successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  // TODO: support permission checks on multiple resources
  /*
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid'
  })
   */
  @Put('translateComponents')
  translateComponents(
    @Body(ValidationPipe) translateElementsDto: TranslateElementsDto
  ): Promise<UpdatedElementsDto> {
    return this.componentService.translateComponents(translateElementsDto);
  }

  @ApiOperation({
    summary: 'Updates the position of components',
    description: 'Updates the position of components to the provided coordinates'
  })
  @ApiOkResponse({
    description: 'The components were positioned successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'Both x and y were not set' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  // TODO: support permission checks on multiple resources
  /*
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid'
  })
   */
  @Put('positionComponents')
  positionComponents(
    @Body(ValidationPipe) positionElementsDto: PositionElementsDto
  ): Promise<UpdatedElementsDto> {
    return this.componentService.positionComponents(positionElementsDto);
  }

  @ApiOperation({
    summary: 'Updates the properties of components',
    description: 'Updates the properties of components to the provided properties'
  })
  @ApiOkResponse({
    description: 'The components properties were updated successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  // TODO: support permission checks on multiple resources
  /*
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid'
  })
   */
  @Put('updateComponentsProperties')
  updateComponentsProperties(
    @Body(ValidationPipe) updateElementsPropertiesDto: UpdateElementsPropertiesDto
  ): Promise<UpdatedElementsDto> {
    return this.componentService.updateComponentsProperties(updateElementsPropertiesDto);
  }

  @ApiOperation({
    summary: 'Deletes components',
    description: 'Deletes components and their shapes'
  })
  @ApiOkResponse({ description: 'The components were deleted successfully' })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiForbiddenResponse({ description: 'After the deletion, the parent component would have no shapes left' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  // TODO: support permission checks on multiple resources
  /*
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid'
  })
   */
  @Delete('deleteComponents')
  deleteComponents(
    @Body(ValidationPipe) deleteElementsDto: DeleteElementsDto
  ): Promise<void> {
    return this.componentService.deleteComponents(deleteElementsDto);
  }

  @ApiOperation({
    summary: 'Aligns components',
    description: 'Aligns components horizontally or vertically'
  })
  @ApiOkResponse({
    description: 'The components were aligned successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  @ApiBadRequestResponse({ description: 'The anchor component uuid was not part of the component uuids' })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  // TODO: support permission checks on multiple resources
  /*
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid'
  })
   */
  @Put('alignComponents')
  alignComponents(
    @Body(ValidationPipe) alignElementsDto: AlignElementsDto
  ): Promise<UpdatedElementsDto> {
    return this.componentService.alignComponents(alignElementsDto);
  }

  @ApiOperation({
    summary: 'Updates a component\'s sorting',
    description: 'Updates a component\'s sorting and also adapts the other affected elements.'
  })
  @ApiOkResponse({
    description: 'The component\'s sorting was updated successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'The element did not exist' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Put('updateSorting')
  updateSorting(
    @Body(ValidationPipe) updateElementSortingDto: UpdateElementSortingDto
  ): Promise<UpdatedElementsDto> {
    return this.componentService.updateComponentSorting(updateElementSortingDto);
  }

  @ApiOperation({
    summary: 'Updates a component\'s name',
    description: 'Updates a component\'s name.'
  })
  @ApiOkResponse({
    description: 'The component\'s name was updated successfully',
    type: ComponentDto
  })
  @ApiNotFoundResponse({ description: 'The component did not exist' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the component' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid',
    relations: [
      'shapes',
      'blueprintComponent',
      'blueprintComponent.shapes'
    ]
  })
  @Put('updateName/:componentUuid')
  updateName(
    @Param('componentUuid', ParseUUIDPipe) componentUuid: string,
    @Body(ValidationPipe) updateComponentNameDto: UpdateComponentNameDto
  ): Promise<ComponentDto> {
    return this.componentService.updateName(componentUuid, updateComponentNameDto);
  }

  @ApiOperation({
    summary: 'Resets mutations of components and shapes',
    description: 'Resets mutations of components and shapes'
  })
  @ApiOkResponse({ description: 'The mutations were deleted successfully' })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no mutation' })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  // TODO: support permission checks on multiple resources
  /*
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid'
  })
   */
  @Delete('resetMutations')
  resetMutations(
    @Body(ValidationPipe) resetElementsMutationsDto: ResetElementsMutationsDto
  ): Promise<void> {
    return this.componentService.resetMutations(resetElementsMutationsDto);
  }

}
