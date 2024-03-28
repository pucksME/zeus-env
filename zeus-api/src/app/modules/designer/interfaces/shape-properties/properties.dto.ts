import { IntersectionType, PartialType } from '@nestjs/swagger';
import { RectanglePropertiesDto } from '../../dtos/shape-properties/rectangle-properties.dto';
import { CirclePropertiesDto } from '../../dtos/shape-properties/circle-properties.dto';
import { TextPropertiesDto } from '../../dtos/shape-properties/text-properties.dto';

export class PropertiesDto extends PartialType(IntersectionType(
  IntersectionType(RectanglePropertiesDto, CirclePropertiesDto),
  TextPropertiesDto
)) {

}
