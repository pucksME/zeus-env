import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsUUID } from 'class-validator';

export class CodeModuleInstanceConnectionDto {
  @ApiProperty()
  @IsUUID()
  uuid: string;

  @ApiProperty()
  @IsString()
  inputCodeModuleInstanceName: string;

  @ApiProperty()
  @IsString()
  inputCodeModuleInstancePortName: string;

  @ApiProperty()
  @IsString()
  outputCodeModuleInstanceName: string;

  @ApiProperty()
  @IsString()
  outputCodeModuleInstancePortName: string;
}
