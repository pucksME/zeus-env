import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNumber, IsOptional, IsUUID } from 'class-validator';

export class InstantiateBlueprintComponentDto {

  @ApiProperty()
  @IsUUID()
  viewUuid: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsUUID()
  parentComponentUuid?: string;

  @ApiProperty()
  @IsUUID()
  blueprintComponentUuid: string;

  @ApiProperty()
  @IsNumber()
  positionX: number;

  @ApiProperty()
  @IsNumber()
  positionY: number;

}
