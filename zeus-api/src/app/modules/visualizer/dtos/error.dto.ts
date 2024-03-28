import { ApiProperty } from '@nestjs/swagger';
import { IsNumber, IsString } from 'class-validator';
import { Error } from '../../project/interfaces/error.interface';

export class ErrorDto implements Error {
  @ApiProperty()
  @IsNumber()
  line: number;

  @ApiProperty()
  @IsNumber()
  linePosition: number;

  @ApiProperty()
  @IsString()
  message: string;
}
