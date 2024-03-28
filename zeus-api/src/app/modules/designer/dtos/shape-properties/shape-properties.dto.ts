import { ShapeProperties } from '../../interfaces/shape-properties/shape-properties.interface';
import { ApiProperty } from '@nestjs/swagger';
import { IsBoolean, IsNumber, IsString, Max, Min } from 'class-validator';

export class ShapePropertiesDto implements ShapeProperties {

  @ApiProperty()
  @IsNumber()
  height: number;

  @ApiProperty()
  @IsNumber()
  width: number;

  @ApiProperty()
  @IsBoolean()
  backgroundColorEnabled: boolean;

  @ApiProperty()
  @IsString()
  backgroundColor: string;

  @ApiProperty()
  @IsBoolean()
  borderEnabled: boolean;

  @ApiProperty()
  @IsString()
  borderColor: string;

  @ApiProperty()
  @IsNumber()
  @Min(0)
  borderWidth: number;

  @ApiProperty()
  @IsBoolean()
  shadowEnabled: boolean;

  @ApiProperty()
  @IsString()
  shadowColor: string;

  @ApiProperty()
  @IsNumber()
  shadowX: number;

  @ApiProperty()
  @IsNumber()
  shadowY: number;

  @ApiProperty()
  @IsNumber()
  @Min(0)
  shadowBlur: number;

  @ApiProperty()
  @IsNumber()
  @Min(0)
  @Max(1)
  opacity: number;

  @ApiProperty()
  @IsBoolean()
  visible: boolean;

}
