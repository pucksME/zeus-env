import { ApiProperty } from '@nestjs/swagger';
import { IsString } from 'class-validator';

export class CodeModuleConfigTypeDto {
  @ApiProperty()
  @IsString()
  type: string;
}
