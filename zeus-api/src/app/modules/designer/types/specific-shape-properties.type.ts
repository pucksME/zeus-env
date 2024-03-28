import { RectangleProperties } from '../interfaces/shape-properties/rectangle-properties.interface';
import { CircleProperties } from '../interfaces/shape-properties/circle-properties.interface';
import { TextProperties } from '../interfaces/shape-properties/text-properties.interface';

export type SpecificShapeProperties = RectangleProperties | CircleProperties | TextProperties;
