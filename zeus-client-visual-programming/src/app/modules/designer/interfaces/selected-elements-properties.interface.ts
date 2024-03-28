export interface SelectedElementsProperties {
  height: number;
  width: number;
  x: number;
  y: number;
  positionRelativeToView: {
    x: number,
    y: number
  };
  elements: {
    elementUuid: string;
    height: number;
    width: number;
    x: number;
    y: number
  }[];
}
