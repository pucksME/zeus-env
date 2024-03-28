import { PickType } from '@nestjs/swagger';
import { ComponentDto } from './component.dto';

export class UpdatedComponentPositionDto extends PickType(ComponentDto, ['uuid', 'positionX', 'positionY']) {
}
