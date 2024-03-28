import { ApiProperty } from '@nestjs/swagger';
import { IsUUID } from 'class-validator';
import { ElementsPropertiesDto } from './elements-properties.dto';

export class ElementIdentifierWithPropertiesDto {

  @ApiProperty()
  @IsUUID()
  elementUuid: string;

  @ApiProperty({type: ElementsPropertiesDto})
  elementProperties: ElementsPropertiesDto;

}
