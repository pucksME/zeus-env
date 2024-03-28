import React from 'react';

import './selection-rectangle.module.scss';
import colors from '../../../assets/styling/colors.json';
import { ToolType } from '../../enums/tool-type.enum';
import { Rect } from 'react-konva';
import { useStore } from '../../store';
import Konva from 'konva';
import { StageProperties } from '../../interfaces/stage-properties.interface';

export interface SelectionRectangleProps {
  activeTool: ToolType;
  stageProperties: StageProperties;
}

export function SelectionRectangle(props: SelectionRectangleProps, ref: React.MutableRefObject<Konva.Rect | null>) {

  const selectionRectangleProperties = useStore(state => state.selectionRectangleProperties);

  return (
    <Rect
      height={selectionRectangleProperties.height}
      width={selectionRectangleProperties.width}
      x={selectionRectangleProperties.positionX}
      y={selectionRectangleProperties.positionY}
      fill={colors.background_light}
      ref={ref}
      stroke={colors.background_dark}
      strokeWidth={0.5 / props.stageProperties.scale}
      opacity={0.35}
      visible={props.activeTool === ToolType.POINTER &&
      selectionRectangleProperties.height !== 0 &&
      selectionRectangleProperties.width !== 0}
    />
  );
}

export default React.forwardRef(SelectionRectangle);
