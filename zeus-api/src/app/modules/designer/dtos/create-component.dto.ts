import { ApiProperty, PickType } from '@nestjs/swagger';
import { ComponentDto } from './component.dto';
import { CreateShapeDto } from './create-shape.dto';

export class CreateComponentDto extends PickType(
  ComponentDto, ['positionX', 'positionY'] as const
) {

  @ApiProperty({ type: [CreateShapeDto] })
  shapes: CreateShapeDto[];

}
