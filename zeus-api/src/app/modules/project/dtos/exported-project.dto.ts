import { ApiProperty, PickType } from '@nestjs/swagger';
import { ErrorDto } from '../../visualizer/dtos/error.dto';
import { ExportedFileDto } from './exported-file.dto';
import { ExportProjectDto } from './export-project.dto';

export class ExportedProjectDto extends PickType(ExportProjectDto, ['exportTarget'] as const) {
  @ApiProperty()
  uuid: string;

  @ApiProperty({type: [ExportedFileDto]})
  exportedFileDtos: ExportedFileDto[];

  @ApiProperty({type: [ErrorDto]})
  errors: ErrorDto[];
}
