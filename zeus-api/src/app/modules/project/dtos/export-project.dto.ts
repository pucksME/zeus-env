import { ApiProperty } from '@nestjs/swagger';
import { ExportTarget } from '../enums/export-target.enum';

export class ExportProjectDto {
  @ApiProperty({
    enum: ExportTarget,
    enumName: 'ExportTarget'
  })
  exportTarget: ExportTarget;
}
