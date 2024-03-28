import { IntersectionType, PartialType, PickType } from '@nestjs/swagger';
import { ViewDto } from './view.dto';

export class CreateViewDto extends IntersectionType(
  PartialType(PickType(ViewDto, ['name', 'height', 'width'] as const)),
  PickType(ViewDto, ['type'])
) {
}
