import { PickType } from '@nestjs/swagger';
import { CodeModuleDto } from './code-module.dto';

export class CreateCodeModuleDto extends PickType(
  CodeModuleDto, ['code'] as const
) {
}
