import { ApiProperty } from '@nestjs/swagger';

export class DeleteShapesDto {

  @ApiProperty()
  shapeUuids: string[];

}
