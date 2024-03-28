import { ShapeDto } from '../../../../gen/api-client';

export interface TextEditorState {
  active: boolean;
  position: {x: number, y: number} | null;
  shape: ShapeDto | null;
}
