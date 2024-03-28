import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsUUID } from 'class-validator';
import { CodeModuleEndpointDto } from './code-module-endpoint.dto';
import { ErrorDto } from './error.dto';
import { CodeModuleConfigDto } from './code-module-config.dto';

export class CodeModuleDto {

  @ApiProperty()
  @IsUUID()
  uuid: string;

  @ApiProperty()
  @IsString()
  name: string;

  @ApiProperty()
  @IsString()
  description: string;

  @ApiProperty()
  @IsString()
  code: string;

  @ApiProperty({type: [CodeModuleConfigDto]})
  configs: CodeModuleConfigDto[]

  @ApiProperty({type: [CodeModuleEndpointDto]})
  inputEndpoints: CodeModuleEndpointDto[];

  @ApiProperty({type: [CodeModuleEndpointDto]})
  outputEndpoints: CodeModuleEndpointDto[];

  @ApiProperty({type: [ErrorDto]})
  errors: ErrorDto[];

}
