import { ApiProperty } from '@nestjs/swagger';
import { IsUUID } from 'class-validator';
import { CodeModuleInstanceDto } from './code-module-instance.dto';

export class WorkspaceVisualizerDto {

  @ApiProperty()
  @IsUUID()
  uuid: string;

  @ApiProperty({type: [CodeModuleInstanceDto]})
  codeModuleInstances: CodeModuleInstanceDto[];

}
