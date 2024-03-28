import { ApiProperty, OmitType } from '@nestjs/swagger';
import { IsNumber } from 'class-validator';
import { TransformElementsDto } from './transform-elements.dto';

export class TranslateElementsDto extends OmitType(
  TransformElementsDto, ['elementsProperties'] as const
) {

  @ApiProperty()
  @IsNumber()
  translateX: number;

  @ApiProperty()
  @IsNumber()
  translateY: number;

}
