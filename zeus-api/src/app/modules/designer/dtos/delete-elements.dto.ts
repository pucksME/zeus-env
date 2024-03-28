import { PickType } from '@nestjs/swagger';
import { TransformElementsDto } from './transform-elements.dto';

export class DeleteElementsDto extends PickType(
  TransformElementsDto, ['parentComponentUuid', 'elementUuids'] as const
) {
}
