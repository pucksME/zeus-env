import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { PropertiesDto } from '../interfaces/shape-properties/properties.dto';
import { IndividualShapePropertiesDto } from './individual-shape-properties.dto';
import { ShapeIdentifierDto } from './shape-identifier.dto';

export class UpdateShapesPropertiesDto {

  @ApiProperty({type: [ShapeIdentifierDto]})
  shapeIdentifiers: ShapeIdentifierDto[];

  @ApiProperty()
  properties: PropertiesDto;

  @ApiPropertyOptional({type: [IndividualShapePropertiesDto]})
  individualProperties?: IndividualShapePropertiesDto[];

}
