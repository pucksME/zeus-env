import { ApiProperty } from "@nestjs/swagger";

export class CodeModuleVariableAssignmentDto {
  @ApiProperty()
  variable: string;

  @ApiProperty()
  value: string;
}