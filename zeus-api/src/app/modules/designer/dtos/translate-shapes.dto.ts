import { ApiProperty, PickType } from '@nestjs/swagger';
import { TranslateElementsDto } from './translate-elements.dto';
import { ShapeIdentifierDto } from './shape-identifier.dto';

export class TranslateShapesDto extends PickType(TranslateElementsDto, ['translateX', 'translateY'] as const) {

  @ApiProperty({type: [ShapeIdentifierDto]})
  shapeIdentifiers: ShapeIdentifierDto[];

}
