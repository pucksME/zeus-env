import { ApiPropertyOptional } from '@nestjs/swagger';
import { TransformElementsDto } from './transform-elements.dto';

export class ReshapeElementsDto extends TransformElementsDto {

  @ApiPropertyOptional()
  height?: number;

  @ApiPropertyOptional()
  width?: number;

}
