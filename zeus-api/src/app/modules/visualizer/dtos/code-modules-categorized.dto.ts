import { CodeModuleDto } from './code-module.dto';
import { ApiProperty } from '@nestjs/swagger';

export class CodeModulesCategorizedDto {

  @ApiProperty({type: [CodeModuleDto]})
  project: CodeModuleDto[];

  @ApiProperty({type: [CodeModuleDto]})
  system: CodeModuleDto[];

}
