import { ApiProperty } from '@nestjs/swagger';

export class UpdatedElementsDto {

  @ApiProperty()
  elementUuids: string[];

}
