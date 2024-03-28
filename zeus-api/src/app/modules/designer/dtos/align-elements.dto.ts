import { ApiProperty, OmitType } from '@nestjs/swagger';
import { Alignment } from '../enums/alignment.enum';
import { TransformElementsDto } from './transform-elements.dto';
import { ElementIdentifierWithPropertiesDto } from './element-identifier-with-properties.dto';

export class AlignElementsDto extends OmitType(
  TransformElementsDto, ['elementUuids'] as const
) {

  @ApiProperty({type: [ElementIdentifierWithPropertiesDto]})
  elements: ElementIdentifierWithPropertiesDto[];

  @ApiProperty({
    type: 'enum',
    enum: Alignment,
    enumName: 'Alignment'
  })
  alignment: Alignment;

}
