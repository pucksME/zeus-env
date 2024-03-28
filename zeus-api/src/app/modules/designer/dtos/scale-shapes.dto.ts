import { ApiProperty, PickType } from '@nestjs/swagger';
import { ScaleElementsDto } from './scale-elements.dto';
import { ShapeIdentifierDto } from './shape-identifier.dto';

export class ScaleShapesDto extends PickType(ScaleElementsDto, ['scaleOrigin', 'scaleX', 'scaleY'] as const) {

  @ApiProperty({type: [ShapeIdentifierDto]})
  shapeIdentifiers: ShapeIdentifierDto[];

}
