import { ApiProperty } from '@nestjs/swagger';
import { IsOptional, IsUUID } from 'class-validator';
import { ElementsPropertiesDto } from './elements-properties.dto';

export class TransformElementsDto {

  @ApiProperty()
  @IsOptional()
  @IsUUID()
  parentComponentUuid: string | null;

  @ApiProperty()
  elementUuids: string[];

  @ApiProperty({type: ElementsPropertiesDto})
  elementsProperties: ElementsPropertiesDto;

}
