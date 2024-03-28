import { ApiProperty } from '@nestjs/swagger';
import { IsNumber } from 'class-validator';

export class ElementsPropertiesDto {

  @ApiProperty()
  @IsNumber()
  height: number;

  @ApiProperty()
  @IsNumber()
  width: number;

  @ApiProperty()
  @IsNumber()
  x: number;

  @ApiProperty()
  @IsNumber()
  y: number;

}
