import { ApiProperty, PickType } from '@nestjs/swagger';
import { IsString } from 'class-validator';
import { ShapeDto } from './shape.dto';

export class ShapeIdentifierDto extends PickType(ShapeDto, ['isMutated' as const]){

  @ApiProperty()
  @IsString()
  shapeUuid: string;

}
