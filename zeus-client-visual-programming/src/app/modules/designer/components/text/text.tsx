import React from 'react';

import './text.module.scss';
import { ShapeDto, TextPropertiesDto, TextTransform } from '../../../../../gen/api-client';
import { Text as KonvaText } from 'react-konva';
import { ShapeUtils } from '../../shape.utils';
import { useStore } from '../../../../store';
import Konva from 'konva';

export interface TextProps {
  shapeDto: ShapeDto;
  hideOnEdit?: boolean;
  onDoubleClick?: (event: Konva.KonvaEventObject<MouseEvent>) => void;
}

export function Text(props: TextProps) {

  const hideOnEdit = (props.hideOnEdit === undefined) ? false : props.hideOnEdit;

  const properties = props.shapeDto.properties as TextPropertiesDto;
  const textEditorState = useStore(state => state.textEditorState);

  return (hideOnEdit && textEditorState.shape !== null && textEditorState.shape.uuid === props.shapeDto.uuid)
    ? null
    : (
    <KonvaText
      id={props.shapeDto.uuid}
      height={properties.height}
      width={properties.width}
      x={props.shapeDto.positionX}
      y={props.shapeDto.positionY}
      text={(properties.textTransform === TextTransform.Uppercase) ? properties.text.toUpperCase() : properties.text}
      {...ShapeUtils.buildTextConfig(props.shapeDto)}
      onDblClick={props.onDoubleClick}
    />
  );
}

export default Text;
