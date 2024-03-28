import { Body, Controller, Delete, Param, ParseUUIDPipe, Post, Put, UseGuards, ValidationPipe } from '@nestjs/common';
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
import { ShapeService } from '../../services/shape/shape.service';
import { HasDesignerPermission } from '../../../../guards/has-designer-permission.decorator';
import { DesignerPermissionToken } from '../../enums/designer-permission-token.enum';
import { IdentifierLocation } from '../../enums/identifier-location.enum';
import { ShapeDto } from '../../dtos/shape.dto';
import { CreateShapeDto } from '../../dtos/create-shape.dto';
import { DeleteShapesDto } from '../../dtos/delete-shapes.dto';
import { UpdateShapesPropertiesDto } from '../../dtos/update-shapes-properties.dto';
import { ScaleShapesDto } from '../../dtos/scale-shapes.dto';
import { ReshapeShapesDto } from '../../dtos/reshape-shapes.dto';
import { TranslateShapesDto } from '../../dtos/translate-shapes.dto';
import { PositionShapesDto } from '../../dtos/position-shapes.dto';
import { AlignShapesDto } from '../../dtos/align-shapes.dto';
import { UpdateShapeSortingDto } from '../../dtos/update-shape-sorting.dto';
import { UpdateShapeNameDto } from '../../dtos/update-shape-name.dto';

@ApiTags('Shape')
@ApiBearerAuth()
@UseGuards(JwtAuthenticationGuard, PermissionGuard)
@Controller('shape')
export class ShapeController {

  constructor(private readonly shapeService: ShapeService) {
  }

  @ApiOperation({
    summary: 'Creates a new shape within a component',
    description: 'Creates a new shape within a component'
  })
  @ApiOkResponse({
    description: 'The shape was created successfully',
    type: ShapeDto
  })
  @ApiNotFoundResponse({ description: 'There was no component with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter componentUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid',
    relations: ['shapes']
  })
  @Post(':componentUuid')
  save(
    @Param('componentUuid', ParseUUIDPipe) componentUuid: string,
    @Body(ValidationPipe) createShapeDto: CreateShapeDto
  ): Promise<ShapeDto> {
    return this.shapeService.save(componentUuid, createShapeDto);
  }

  @ApiOperation({
    summary: 'Deletes shapes',
    description: 'Deletes shapes'
  })
  @ApiOkResponse({ description: 'The shapes were deleted successfully' })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no shape' })
  @ApiForbiddenResponse({ description: 'Deleting the shapes would lead to a component without any shapes' })
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
  @Delete('deleteShapes')
  deleteShapes(
    @Body(ValidationPipe) deleteShapesDto: DeleteShapesDto
  ): Promise<void> {
    return this.shapeService.deleteShapes(deleteShapesDto.shapeUuids);
  }

  @ApiOperation({
    summary: 'Updates the properties of shapes',
    description: 'Updates the properties of shapes to the provided properties'
  })
  @ApiOkResponse({
    description: 'The shapes properties were updated successfully',
    type: [ShapeDto]
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no shape' })
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
  @Put('updateShapesProperties')
  updateShapesProperties(
    @Body(ValidationPipe) updateShapesPropertiesDto: UpdateShapesPropertiesDto
  ): Promise<ShapeDto[]> {
    return this.shapeService.updateProperties(updateShapesPropertiesDto);
  }

  @ApiOperation({
    summary: 'Scales shapes',
    description: 'Scales shapes by updating their position and dimensions'
  })
  @ApiOkResponse({
    description: 'The shapes were scaled successfully',
    type: [ShapeDto]
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no shape' })
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
  @Put('scaleShapes')
  scaleShapes(
    @Body(ValidationPipe) scaleShapesDto: ScaleShapesDto
  ): Promise<ShapeDto[]> {
    return this.shapeService.scaleShapes(scaleShapesDto);
  }

  @ApiOperation({
    summary: 'Reshapes shapes',
    description: 'Reshapes shapes by reshaping their width and height'
  })
  @ApiOkResponse({
    description: 'The shapes were reshaped successfully',
    type: [ShapeDto]
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no shape' })
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
  @Put('reshapeShapes')
  reshapeShapes(
    @Body(ValidationPipe) reshapeShapesDto: ReshapeShapesDto
  ): Promise<ShapeDto[]> {
    return this.shapeService.reshapeShapes(reshapeShapesDto);
  }

  @ApiOperation({
    summary: 'Translates shapes',
    description: 'Translates shapes by adding an offset to their position'
  })
  @ApiOkResponse({
    description: 'The shapes were translated successfully',
    type: [ShapeDto]
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no shape' })
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
  @Put('translateShapes')
  translateShapes(
    @Body(ValidationPipe) translateShapesDto: TranslateShapesDto
  ): Promise<ShapeDto[]> {
    return this.shapeService.translateShapes(translateShapesDto);
  }

  @ApiOperation({
    summary: 'Updates the position of shapes',
    description: 'Updates the position of shapes to the provided coordinates'
  })
  @ApiOkResponse({
    description: 'The shapes were positioned successfully',
    type: [ShapeDto]
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no shape' })
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
  @Put('positionShapes')
  positionShapes(
    @Body(ValidationPipe) positionShapesDto: PositionShapesDto
  ): Promise<ShapeDto[]> {
    return this.shapeService.positionShapes(positionShapesDto);
  }

  @ApiOperation({
    summary: 'Aligns shapes',
    description: 'Aligns shapes horizontally or vertically'
  })
  @ApiOkResponse({
    description: 'The shapes were aligned successfully',
    type: [ShapeDto]
  })
  @ApiNotFoundResponse({ description: 'For at least one uuid, there was no shape' })
  @ApiBadRequestResponse({ description: 'The anchor shape uuid was not part of the shape uuids' })
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
  @Put('alignShapes')
  alignShapes(
    @Body(ValidationPipe) alignShapesDto: AlignShapesDto
  ): Promise<ShapeDto[]> {
    return this.shapeService.alignShapes(alignShapesDto);
  }

  @ApiOperation({
    summary: 'Updates a shape\'s sorting',
    description: 'Updates a shape\'s sorting and also adapts the other affected components.'
  })
  @ApiOkResponse({
    description: 'The shape\'s sorting was updated successfully',
    type: ShapeDto
  })
  @ApiNotFoundResponse({ description: 'The shape did not exist' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the shape' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'shapeUuid',
    relations: ['component.shapes'] // TODO: use set in guard to also allow setting relations multiple times (e.g. 'view')
  })
  @Put('updateSorting/:shapeUuid')
  updateSorting(
    @Param('shapeUuid', ParseUUIDPipe) shapeUuid: string,
    @Body(ValidationPipe) updateShapeSortingDto: UpdateShapeSortingDto
  ): Promise<ShapeDto> {
    return this.shapeService.updateSorting(shapeUuid, updateShapeSortingDto);
  }

  @ApiOperation({
    summary: 'Updates a shape\'s name',
    description: 'Updates a shape\'s name.'
  })
  @ApiOkResponse({
    description: 'The shape\'s name was updated successfully',
    type: ShapeDto
  })
  @ApiNotFoundResponse({ description: 'The shape did not exist' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the shape' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasDesignerPermission({
    token: DesignerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'shapeUuid'
  })
  @Put('updateName/:shapeUuid')
  updateName(
    @Param('shapeUuid', ParseUUIDPipe) shapeUuid: string,
    @Body(ValidationPipe) updateShapeNameDto: UpdateShapeNameDto
  ): Promise<ShapeDto> {
    return this.shapeService.updateName(shapeUuid, updateShapeNameDto);
  }

}
