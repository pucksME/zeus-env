import { ApiProperty } from '@nestjs/swagger';
import { IsOptional, IsString } from 'class-validator';

export class UpdateShapeNameDto {

  @ApiProperty()
  @IsOptional()
  @IsString()
  name: string | null;

}
