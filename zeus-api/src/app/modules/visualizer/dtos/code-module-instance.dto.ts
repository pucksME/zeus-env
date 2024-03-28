import { ApiProperty } from '@nestjs/swagger';
import { CodeModuleDto } from './code-module.dto';
import { IsNumber, IsOptional, IsString, IsUUID } from 'class-validator';

export class CodeModuleInstanceDto {

  @ApiProperty()
  @IsUUID()
  uuid: string;

  @ApiProperty()
  @IsOptional()
  @IsString()
  flowDescription: string | null;

  @ApiProperty()
  @IsNumber()
  positionX: number;

  @ApiProperty()
  @IsNumber()
  positionY: number;

  @ApiProperty({type: CodeModuleDto})
  codeModule: CodeModuleDto;

}
