import { CodeModuleEndpointDto } from './code-module-endpoint.dto';
import { ApiProperty, PickType } from '@nestjs/swagger';
import { IsUUID } from 'class-validator';

export class CodeModuleInstanceEndpointDto extends PickType(
  CodeModuleEndpointDto, ['name'] as const
) {

  @ApiProperty()
  @IsUUID()
  codeModuleInstanceUuid: string;

}
