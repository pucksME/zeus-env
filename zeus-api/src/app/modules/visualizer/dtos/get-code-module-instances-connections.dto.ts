import { ApiProperty } from '@nestjs/swagger';

export class GetCodeModuleInstancesConnectionsDto {
  @ApiProperty()
  codeModuleInstanceUuids: string[];
}
