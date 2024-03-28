import { PickType } from '@nestjs/swagger';
import { ComponentDto } from './component.dto';

export class UpdateComponentPositionDto extends PickType(ComponentDto, ['positionX', 'positionY']) {
}
