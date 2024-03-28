import { ApiProperty } from '@nestjs/swagger';
import { IsString } from 'class-validator';

export class CodeModuleEndpointDto {

  @ApiProperty()
  @IsString()
  name: string;

  @ApiProperty()
  @IsString()
  type: string;

}
