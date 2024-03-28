import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsUUID } from 'class-validator';
import { Alignment } from '../enums/alignment.enum';
import { ShapeIdentifierDto } from './shape-identifier.dto';

export class AlignShapesDto {

  @ApiProperty({type: [ShapeIdentifierDto]})
  shapeIdentifiers: ShapeIdentifierDto[];

  @ApiPropertyOptional()
  @IsOptional()
  @IsUUID()
  anchorShapeIdentifier?: ShapeIdentifierDto;

  @ApiProperty({
    type: 'enum',
    enum: Alignment,
    enumName: 'Alignment'
  })
  alignment: Alignment;

}
