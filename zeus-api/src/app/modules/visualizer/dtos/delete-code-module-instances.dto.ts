import { ApiProperty } from '@nestjs/swagger';

export class DeleteCodeModuleInstancesDto {

  @ApiProperty()
  codeModuleInstanceUuids: string[];

}
