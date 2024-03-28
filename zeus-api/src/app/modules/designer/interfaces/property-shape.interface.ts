import { SpecificShapeProperties } from '../types/specific-shape-properties.type';

export interface PropertyShape {
  uuid: string;
  positionX: number | null;
  positionY: number | null;
  properties: Partial<SpecificShapeProperties>;
}
