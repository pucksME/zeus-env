import { ToolType } from '../../../enums/tool-type.enum';

export interface CreateFormPreviewProperties {
  height: number;
  width: number;
  positionX: number;
  positionY: number;
  toolUsed: ToolType | null;
}
