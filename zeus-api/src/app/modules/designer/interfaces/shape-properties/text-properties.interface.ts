import { ShapeProperties } from './shape-properties.interface';

export enum FontFamily {
  ARIAL = 'ARIAL'
}

export enum FontStyle {
  NORMAL = 'NORMAL',
  BOLD = 'BOLD',
  ITALIC = 'ITALIC'
}

export enum TextDecoration {
  NONE = 'NONE',
  UNDERLINE = 'UNDERLINE',
  STRIKE_THROUGH = 'STRIKE_THROUGH'
}

export enum TextTransform {
  NONE = 'NONE',
  UPPERCASE = 'UPPERCASE'
}

export enum TextAlign {
  LEFT = 'LEFT',
  CENTER = 'CENTER',
  RIGHT = 'RIGHT'
}

export interface TextProperties extends ShapeProperties {
  fontFamily: FontFamily;
  fontSize: number;
  fontStyle: FontStyle;
  text: string;
  textDecoration: TextDecoration;
  textTransform: TextTransform;
  textAlign: TextAlign;
}
