import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNumber, IsOptional } from 'class-validator';

export class TranslateCodeModuleInstancesDto {

  @ApiProperty()
  codeModuleInstanceUuids: string[];

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  translateX?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  translateY?: number;

}
