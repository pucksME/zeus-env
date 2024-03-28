import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNumber, IsOptional } from 'class-validator';
import { ShapeIdentifierDto } from './shape-identifier.dto';

export class PositionShapesDto {

  @ApiProperty({type: [ShapeIdentifierDto]})
  shapeIdentifiers: ShapeIdentifierDto[];

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  positionX?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  positionY?: number;

}
