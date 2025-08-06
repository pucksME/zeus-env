import { ApiProperty } from "@nestjs/swagger";
import { ErrorDto } from "../../visualizer/dtos/error.dto";
import { CodeModuleCounterexampleDto } from "../../visualizer/dtos/code-module-counterexample.dto";

export class VerifiedCodeModuleDto {
  @ApiProperty({type: [CodeModuleCounterexampleDto]})
  counterexamples: CodeModuleCounterexampleDto[]

  @ApiProperty({type: [ErrorDto]})
  errors: ErrorDto[];
}