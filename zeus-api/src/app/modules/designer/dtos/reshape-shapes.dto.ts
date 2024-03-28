import { ApiProperty, PickType } from '@nestjs/swagger';
import { ReshapeElementsDto } from './reshape-elements.dto';
import { ShapeIdentifierDto } from './shape-identifier.dto';

export class ReshapeShapesDto extends PickType(ReshapeElementsDto, ['height', 'width'] as const) {

  @ApiProperty({type: [ShapeIdentifierDto]})
  shapeIdentifiers: ShapeIdentifierDto[];

}
