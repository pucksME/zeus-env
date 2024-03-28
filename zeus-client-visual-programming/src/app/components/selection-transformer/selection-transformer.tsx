import React from 'react';

import './selection-transformer.module.scss';
import colors from '../../../assets/styling/colors.json';
import { Transformer } from 'react-konva';
import Konva from 'konva';
import { Box } from 'konva/lib/shapes/Transformer';
import { StageEventService } from '../../services/stage-event.service';

export interface SelectionTransformerProps {
  visible: boolean;
  resizeable: boolean;
  onDragStart?: (event: Konva.KonvaEventObject<DragEvent>) => void;
  onDragMove?: (event: Konva.KonvaEventObject<DragEvent>) => void;
  onDragEnd?: (event: Konva.KonvaEventObject<DragEvent>) => void;
}

export function SelectionTransformer(
  props: SelectionTransformerProps,
  ref: React.MutableRefObject<Konva.Transformer | null>
) {

  const handleTransformBoundBox = (oldBox: Box, newBox: Box) => StageEventService.handleTransformBoundBox(
    ref, oldBox, newBox
  );

  return (
    <Transformer
      anchorCornerRadius={5}
      anchorSize={8}
      anchorStroke={colors.secondary.main}
      anchorStrokeWidth={1}
      borderStroke={colors.border_main}
      borderStrokeWidth={1}
      ref={ref}
      rotateEnabled={false}
      visible={props.visible}
      boundBoxFunc={handleTransformBoundBox}
      ignoreStroke={true}
      resizeEnabled={props.resizeable}
      onDragStart={props.onDragStart}
      onDragMove={props.onDragMove}
      onDragEnd={props.onDragEnd}
    />
  );
}

export default React.forwardRef(SelectionTransformer);
