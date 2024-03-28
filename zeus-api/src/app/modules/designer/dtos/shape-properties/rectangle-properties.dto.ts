import { ShapePropertiesDto } from './shape-properties.dto';
import { RectangleProperties } from '../../interfaces/shape-properties/rectangle-properties.interface';
import { ApiProperty } from '@nestjs/swagger';

export class RectanglePropertiesDto extends ShapePropertiesDto implements RectangleProperties {

  @ApiProperty({type: [Number]})
    // TODO: validate - note: when updating properties, items are also allowed to be null
  borderRadius: number[];

}
