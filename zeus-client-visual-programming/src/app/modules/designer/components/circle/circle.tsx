import React from 'react';

import './circle.module.scss';
import { CirclePropertiesDto, ShapeDto } from '../../../../../gen/api-client';
import { Ellipse } from 'react-konva';

export interface CircleProps {
  shapeDto: ShapeDto;
  borderScale?: number;
}

export function Circle(props: CircleProps) {

  const borderScale = (props.borderScale === undefined) ? 1 : props.borderScale;
  const properties = props.shapeDto.properties as CirclePropertiesDto;

  return (
    <Ellipse
      id={props.shapeDto.uuid}
      radiusX={properties.width / 2}
      radiusY={properties.height / 2}
      x={props.shapeDto.positionX + (properties.width / 2)}
      y={props.shapeDto.positionY + (properties.height / 2)}
      fillEnabled={properties.backgroundColorEnabled}
      fill={properties.backgroundColor}
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

export default Circle;
