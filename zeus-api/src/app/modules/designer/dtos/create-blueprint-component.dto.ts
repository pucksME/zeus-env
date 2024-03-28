import { ApiProperty } from '@nestjs/swagger';
import { IsUUID } from 'class-validator';

export class CreateBlueprintComponentDto {

  @ApiProperty()
  @IsUUID()
  componentUuid: string;

}
