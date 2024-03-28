import { ApiProperty, PickType } from '@nestjs/swagger';
import { IsNumber, IsUUID, Min } from 'class-validator';
import { TransformElementsDto } from './transform-elements.dto';

export class UpdateElementSortingDto extends PickType(TransformElementsDto, ['parentComponentUuid'] as const) {

  @ApiProperty()
  @IsUUID()
  elementUuid: string;

  @ApiProperty()
  @Min(0)
  @IsNumber()
  sorting: number;

}
