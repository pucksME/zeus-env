import { ApiProperty } from '@nestjs/swagger';
import { IsOptional, IsString } from 'class-validator';

export class UpdateComponentNameDto {

  @ApiProperty()
  @IsOptional()
  @IsString()
  name: string | null;

}
