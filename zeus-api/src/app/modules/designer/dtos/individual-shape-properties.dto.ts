import { ApiProperty } from '@nestjs/swagger';
import { PropertiesDto } from '../interfaces/shape-properties/properties.dto';

export class IndividualShapePropertiesDto {

  @ApiProperty()
  shapeUuid: string;

  @ApiProperty()
  properties: PropertiesDto;

}
