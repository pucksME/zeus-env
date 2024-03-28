import { ExportedFile } from '../interfaces/exported-file.interface';
import { ApiProperty } from '@nestjs/swagger';
import { IsString } from 'class-validator';

export class ExportedFileDto implements ExportedFile {
  @ApiProperty()
  @IsString()
  code: string;

  @ApiProperty()
  @IsString()
  filename: string;
}
