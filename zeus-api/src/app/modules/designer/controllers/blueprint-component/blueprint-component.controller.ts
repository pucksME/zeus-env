import {
  Body,
  Controller,
  Delete, Get,
  Param,
  ParseUUIDPipe,
  Post, Put, Req,
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
import { JwtAuthenticationGuard } from '../../../authentication/guards/jwt-authentication-guard/jwt-authentication.guard';
import { PermissionGuard } from '../../../../guards/permission.guard';
import { BlueprintComponentService } from '../../services/blueprint-component/blueprint-component.service';
import { HasDesignerPermission } from '../../../../guards/has-designer-permission.decorator';
import { DesignerPermissionToken } from '../../enums/designer-permission-token.enum';
import { IdentifierLocation } from '../../enums/identifier-location.enum';
import { BlueprintComponentDto } from '../../dtos/blueprint-component.dto';
import { CreateBlueprintComponentDto } from '../../dtos/create-blueprint-component.dto';
import { ComponentDto } from '../../dtos/component.dto';
import { InstantiateBlueprintComponentDto } from '../../dtos/instantiate-blueprint-component.dto';
import { UpdateBlueprintComponentNameDto } from '../../dtos/update-blueprint-component-name.dto';
import { UpdatedElementsDto } from '../../dtos/updated-elements.dto';
import { ScaleElementsDto } from '../../dtos/scale-elements.dto';
import { ReshapeElementsDto } from '../../dtos/reshape-elements.dto';
import { TranslateElementsDto } from '../../dtos/translate-elements.dto';
import { PositionElementsDto } from '../../dtos/position-elements.dto';
import { AlignElementsDto } from '../../dtos/align-elements.dto';
import { UpdateElementsPropertiesDto } from '../../dtos/update-elements-properties.dto';
import { DeleteElementsDto } from '../../dtos/delete-elements.dto';
import { UpdateElementSortingDto } from '../../dtos/update-element-sorting.dto';

@ApiTags('Blueprint Component')
@ApiBearerAuth()
@UseGuards(JwtAuthenticationGuard, PermissionGuard)
@Controller('blueprint-component')
export class BlueprintComponentController {

  constructor(
    private readonly blueprintComponentService: BlueprintComponentService
  ) {
  }

  @ApiOperation({
    summary: 'Creates a new blueprint component',
    description: 'Creates a new blueprint component with an existing component'
  })
  @ApiOkResponse({
    description: 'The blueprint component was created successfully',
    type: BlueprintComponentDto
  })
  @ApiNotFoundResponse({ description: 'There was no component with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter viewUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.BODY,
    keyName: 'componentUuid',
    relations: [
      'shapes',
      'blueprintComponent'
    ]
  })
  @Post()
  save(@Body(ValidationPipe) createBlueprintComponentDto: CreateBlueprintComponentDto): Promise<BlueprintComponentDto> {
    return this.blueprintComponentService.save(createBlueprintComponentDto);
  }

  @ApiOperation({
    summary: 'Creates a new blueprint component from a shape',
    description: 'Creates a new blueprint component from a given shape with the shape\'s parent as its parent'
  })
  @ApiOkResponse({
    description: 'The blueprint component was created successfully',
    type: BlueprintComponentDto
  })
  @ApiNotFoundResponse({ description: 'There was no shape with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace, the shape was not part of a blueprint component or its parent had not enough shapes' })
  @ApiBadRequestResponse({ description: 'The parameter shapeUuid was not a valid UUID' })
  @Post('/shape/:shapeUuid')
  saveWithShape(@Param('shapeUuid', ParseUUIDPipe) shapeUuid: string): Promise<BlueprintComponentDto> {
    return this.blueprintComponentService.saveWithShape(shapeUuid);
  }

  @ApiOperation({
    summary: 'Finds blueprint components with their workspace uuid',
    description: 'Finds all blueprint components belonging to a workspace and includes their shapes'
  })
  @ApiOkResponse({
    type: [BlueprintComponentDto],
    description: 'The blueprint components were found successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no workspace related to the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to see the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter workspaceUuid was not a valid UUID' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.READ,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'workspaceUuid',
    relations: ['blueprintComponents', 'blueprintComponents.shapes']
  })
  @Get(':workspaceUuid')
  findWorkspace(
    @Req() req, @Param('workspaceUuid', ParseUUIDPipe) workspaceUuid: string
  ): Promise<BlueprintComponentDto[]> {
    return this.blueprintComponentService.find(workspaceUuid);
  }

  @ApiOperation({
    summary: 'Instantiates a blueprint component',
    description: 'Creates a new component by instantiating a blueprint component'
  })
  @ApiOkResponse({
    description: 'The blueprint component was instantiated successfully',
    type: ComponentDto
  })
  @ApiNotFoundResponse({ description: 'There was no blueprint component with the given uuid' })
  @ApiForbiddenResponse({ description: 'The parent component was a blueprint component instance or the requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.READ,
    extractFrom: IdentifierLocation.BODY,
    keyName: 'viewUuid',
    relations: [
      'components'
    ]
  })
  @Post('instantiate')
  instantiate(@Body(ValidationPipe) instantiateBlueprintComponentDto: InstantiateBlueprintComponentDto): Promise<ComponentDto> {
    return this.blueprintComponentService.instantiate(instantiateBlueprintComponentDto);
  }

  @ApiOperation({
    summary: 'Updates a blueprint component\'s name',
    description: 'Updates a blueprint component\'s name.'
  })
  @ApiOkResponse({
    description: 'The component\'s name was updated successfully',
    type: BlueprintComponentDto
  })
  @ApiNotFoundResponse({ description: 'The blueprint component did not exist' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Put('updateName/:blueprintComponentUuid')
  updateName(
    @Param('blueprintComponentUuid', ParseUUIDPipe) blueprintComponentUuid: string,
    @Body(ValidationPipe) updateBlueprintComponentNameDto: UpdateBlueprintComponentNameDto
  ): Promise<BlueprintComponentDto> {
    return this.blueprintComponentService.updateName(blueprintComponentUuid, updateBlueprintComponentNameDto);
  }

  @ApiOperation({
    summary: 'Scales blueprint components',
    description: 'Scales blueprint components by updating their position and scaling their shapes'
  })
  @ApiOkResponse({
    description: 'The blueprint components were scaled successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Put('scaleBlueprintComponents')
  scaleBlueprintComponents(
    @Body(ValidationPipe) scaleElementsDto: ScaleElementsDto
  ): Promise<UpdatedElementsDto> {
    return this.blueprintComponentService.scaleBlueprintComponents(scaleElementsDto);
  }

  @ApiOperation({
    summary: 'Reshapes blueprint components',
    description: 'Reshapes blueprint components by reshaping their width and height'
  })
  @ApiOkResponse({
    description: 'The blueprint components were reshaped successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Put('reshapeBlueprintComponents')
  reshapeBlueprintComponents(
    @Body(ValidationPipe) reshapeElementsDto: ReshapeElementsDto
  ): Promise<UpdatedElementsDto> {
    return this.blueprintComponentService.reshapeBlueprintComponents(reshapeElementsDto);
  }

  @ApiOperation({
    summary: 'Translates blueprint components',
    description: 'Translates components by adding an offset to their position'
  })
  @ApiOkResponse({
    description: 'The blueprint components were translated successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Put('translateBlueprintComponents')
  translateBlueprintComponents(
    @Body(ValidationPipe) translateElementsDto: TranslateElementsDto
  ): Promise<UpdatedElementsDto> {
    return this.blueprintComponentService.translateBlueprintComponents(translateElementsDto);
  }

  @ApiOperation({
    summary: 'Updates the position of blueprint components',
    description: 'Updates the position of blueprint components to the provided coordinates'
  })
  @ApiOkResponse({
    description: 'The blueprint components were positioned successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  @ApiBadRequestResponse({ description: 'Both x and y were not set' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Put('positionBlueprintComponents')
  positionBlueprintComponents(
    @Body(ValidationPipe) positionElementsDto: PositionElementsDto
  ): Promise<UpdatedElementsDto> {
    return this.blueprintComponentService.positionBlueprintComponents(positionElementsDto);
  }

  @ApiOperation({
    summary: 'Aligns blueprint components',
    description: 'Aligns blueprint components horizontally or vertically'
  })
  @ApiOkResponse({
    description: 'The blueprint components were aligned successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  @ApiBadRequestResponse({ description: 'The anchor component uuid was not part of the component uuids' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Put('alignBlueprintComponents')
  alignBlueprintComponents(
    @Body(ValidationPipe) alignElementsDto: AlignElementsDto
  ): Promise<UpdatedElementsDto> {
    return this.blueprintComponentService.alignBlueprintComponents(alignElementsDto);
  }

  @ApiOperation({
    summary: 'Updates the properties of blueprint components',
    description: 'Updates the properties of blueprint components to the provided properties'
  })
  @ApiOkResponse({
    description: 'The blueprint components properties were updated successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Put('updateBlueprintComponentsProperties')
  updateBlueprintComponentsProperties(
    @Body(ValidationPipe) updateElementsPropertiesDto: UpdateElementsPropertiesDto
  ): Promise<UpdatedElementsDto> {
    return this.blueprintComponentService.updateBlueprintComponentProperties(updateElementsPropertiesDto);
  }

  @ApiOperation({
    summary: 'Deletes blueprint components',
    description: 'Deletes blueprint components and their shapes'
  })
  @ApiOkResponse({ description: 'The blueprint components were deleted successfully' })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no element' })
  @ApiForbiddenResponse({ description: 'After the deletion, the parent blueprint component would have no shapes left' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Delete('deleteBlueprintComponents')
  deleteBlueprintComponents(
    @Body(ValidationPipe) deleteElementsDto: DeleteElementsDto
  ): Promise<void> {
    return this.blueprintComponentService.deleteBlueprintComponents(deleteElementsDto);
  }

  @ApiOperation({
    summary: 'Updates a blueprint component\'s sorting',
    description: 'Updates a blueprint component\'s sorting and also adapts the other affected elements.'
  })
  @ApiOkResponse({
    description: 'The blueprint component\'s sorting was updated successfully',
    type: UpdatedElementsDto
  })
  @ApiNotFoundResponse({ description: 'The element did not exist' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Put('updateSorting')
  updateSorting(
    @Body(ValidationPipe) updateElementSortingDto: UpdateElementSortingDto
  ): Promise<UpdatedElementsDto> {
    return this.blueprintComponentService.updateBlueprintComponentSorting(updateElementSortingDto);
  }

}
