import { ApiProperty } from "@nestjs/swagger";
import { ErrorDto } from "../../visualizer/dtos/error.dto";

export class VerifiedCodeModuleDto {
  @ApiProperty()
  success: boolean;

  @ApiProperty({type: [ErrorDto]})
  errors: ErrorDto[];
}