import { PickType } from '@nestjs/swagger';
import { ShapeDto } from './shape.dto';

export class CreateShapeDto extends PickType(ShapeDto, ['positionX', 'positionY', 'type', 'properties']) {
}
