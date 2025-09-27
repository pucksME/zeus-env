import { ApiProperty } from "@nestjs/swagger";
import { LocationDto } from "./location.dto";
import { CodeModuleVariableAssignmentDto } from "./code-module-variable-assignment.dto";

export class CodeModuleCounterexampleDto {
  @ApiProperty({type: [LocationDto]})
  locations: LocationDto[];

  @ApiProperty({type: [CodeModuleVariableAssignmentDto]})
  variableAssignments: CodeModuleVariableAssignmentDto[];
}