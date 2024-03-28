import { PartialType, PickType } from '@nestjs/swagger';
import { CodeModuleDto } from './code-module.dto';

export class UpdateCodeModuleDto extends PartialType(
  PickType(CodeModuleDto, ['code'] as const)
){
}
