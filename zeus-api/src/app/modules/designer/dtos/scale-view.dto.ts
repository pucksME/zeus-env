import { ApiProperty } from '@nestjs/swagger';
import { ScaleOrigin } from '../enums/scale-origin.enum';
import { IsNumber, Min } from 'class-validator';

export class ScaleViewDto {
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
