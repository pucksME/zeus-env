import {ApiProperty, PickType} from "@nestjs/swagger";
import {ExportProjectDto} from "./export-project.dto";

export class ExportRainProjectDto extends PickType(ExportProjectDto, ['exportTarget'] as const) {
  @ApiProperty()
  code: string;
}
