import { ApiProperty } from '@nestjs/swagger';
import { IsNumber, Min } from 'class-validator';
import { ScaleOrigin } from '../enums/scale-origin.enum';
import { TransformElementsDto } from './transform-elements.dto';

export class ScaleElementsDto extends TransformElementsDto {

  @ApiProperty({
    enum: ScaleOrigin,
    enumName: 'ScaleOrigin'
  })
  scaleOrigin: ScaleOrigin;

  @ApiProperty()
  @IsNumber()
  @Min(0)
  scaleX: number;

  @ApiProperty()
  @IsNumber()
  @Min(0)
  scaleY: number;

}
