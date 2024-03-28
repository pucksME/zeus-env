import { ApiExtraModels, ApiProperty, getSchemaPath } from '@nestjs/swagger';
import { IsBoolean, IsNumber, IsString, IsUUID } from 'class-validator';
import { ShapeType } from '../enums/shape-type.enum';
import { RectanglePropertiesDto } from './shape-properties/rectangle-properties.dto';
import { CirclePropertiesDto } from './shape-properties/circle-properties.dto';
import { TextPropertiesDto } from './shape-properties/text-properties.dto';

@ApiExtraModels(
  RectanglePropertiesDto,
  CirclePropertiesDto,
  TextPropertiesDto
)
export class ShapeDto {

  @ApiProperty()
  @IsUUID()
  uuid: string;

  @ApiProperty()
  @IsString()
  name: string;

  @ApiProperty()
  @IsNumber()
  positionX: number;

  @ApiProperty()
  @IsNumber()
  positionY: number;

  @ApiProperty()
  @IsNumber()
  sorting: number;

  @ApiProperty({
    enum: ShapeType,
    enumName: 'ShapeType'
  })
  type: ShapeType;

  // TODO: validate properties (also in entity?)
  @ApiProperty({
    oneOf: [
      { $ref: getSchemaPath(RectanglePropertiesDto) },
      { $ref: getSchemaPath(CirclePropertiesDto) },
      { $ref: getSchemaPath(TextPropertiesDto) }
    ]
  })
  properties: RectanglePropertiesDto | CirclePropertiesDto | TextPropertiesDto;

  @ApiProperty()
  @IsBoolean()
  isMutated: boolean;
}
