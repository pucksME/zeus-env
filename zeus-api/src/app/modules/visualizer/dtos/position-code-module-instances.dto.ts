import { ApiProperty, PartialType, PickType } from '@nestjs/swagger';
import { CodeModuleInstanceDto } from './code-module-instance.dto';

export class PositionCodeModuleInstancesDto extends PartialType(
  PickType(CodeModuleInstanceDto, ['positionX', 'positionY'] as const)
) {

  @ApiProperty()
  codeModuleInstanceUuids: string[];

}
