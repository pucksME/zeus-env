import { ApiProperty } from '@nestjs/swagger';
import { IsNumber } from 'class-validator';

export class TranslateViewDto {
  @ApiProperty()
  @IsNumber()
  translateX: number;

  @ApiProperty()
  @IsNumber()
  translateY: number;
}
