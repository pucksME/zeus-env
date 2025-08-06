import { ApiProperty } from "@nestjs/swagger";

export class LocationDto {
  @ApiProperty()
  line: number;

  @ApiProperty()
  linePosition: number;
}