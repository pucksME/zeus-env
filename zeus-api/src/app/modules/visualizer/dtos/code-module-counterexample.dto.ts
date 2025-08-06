import { ApiProperty } from "@nestjs/swagger";
import { LocationDto } from "./location.dto";

export class CodeModuleCounterexampleDto {
  @ApiProperty({type: [LocationDto]})
  locations: LocationDto[];
}