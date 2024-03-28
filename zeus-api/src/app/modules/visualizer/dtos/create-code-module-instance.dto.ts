import { ApiProperty, PickType } from '@nestjs/swagger';
import { CodeModuleInstanceDto } from './code-module-instance.dto';
import { IsUUID } from 'class-validator';

export class CreateCodeModuleInstanceDto extends PickType(
  CodeModuleInstanceDto, ['positionX', 'positionY'] as const
) {

  @ApiProperty()
  @IsUUID()
  codeModuleUuid: string;

}
