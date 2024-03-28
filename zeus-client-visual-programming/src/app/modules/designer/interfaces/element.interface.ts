import { ShapeDto } from '../../../../gen/api-client';
import { ElementType } from '../enums/element-type.enum';

export interface Element<T> {
  element: T | ShapeDto;
  type: ElementType;
}
