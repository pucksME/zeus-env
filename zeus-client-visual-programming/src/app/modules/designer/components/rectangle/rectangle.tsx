import React from 'react';

import './rectangle.module.scss';
import { Rect } from 'react-konva';
import { RectanglePropertiesDto, ShapeDto, ShapeType } from '../../../../../gen/api-client';

export interface RectangleProps {
  shapeDto: ShapeDto;
  borderScale?: number;
}

export function Rectangle(props: RectangleProps) {

  const borderScale = (props.borderScale === undefined) ? 1 : props.borderScale;
  const properties = props.shapeDto.properties as RectanglePropertiesDto;

  return (props.shapeDto.type !== ShapeType.Rectangle) ? null : (
    <Rect
      id={props.shapeDto.uuid}
      height={properties.height}
      width={properties.width}
      x={props.shapeDto.positionX}
      y={props.shapeDto.positionY}
      fillEnabled={properties.backgroundColorEnabled}
      fill={properties.backgroundColor}
      cornerRadius={properties.borderRadius}
      strokeEnabled={properties.borderEnabled && properties.borderWidth !== 0}
      stroke={(properties.borderWidth === 0) ? undefined : properties.borderColor}
      strokeWidth={(properties.borderWidth === 0) ? undefined : (properties.borderWidth * borderScale)}
      fillAfterStrokeEnabled={true}
      strokeScaleEnabled={false}
      shadowForStrokeEnabled={false}
      shadowEnabled={properties.shadowEnabled}
      shadowColor={properties.shadowColor}
      shadowOffsetX={properties.shadowX}
      shadowOffsetY={properties.shadowY}
      shadowBlur={properties.shadowBlur}
      opacity={properties.opacity}
      visible={properties.visible}
    />
  );
}

export default Rectangle;
