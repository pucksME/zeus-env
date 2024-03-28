import { ApiProperty, OmitType } from '@nestjs/swagger';
import { PropertiesDto } from '../interfaces/shape-properties/properties.dto';
import { TransformElementsDto } from './transform-elements.dto';

export class UpdateElementsPropertiesDto extends OmitType(
  TransformElementsDto, ['elementsProperties'] as const
) {

  @ApiProperty()
  properties: PropertiesDto;

}
