import React from 'react';

import './workspace-stage.module.scss';
import { Stage } from 'react-konva';
import { useSynchronizeStageDimensions } from '../../sections/workspace/workspace.hooks';
import { StageProperties } from '../../interfaces/stage-properties.interface';
import { StageDimensions } from '../../modules/designer/interfaces/stage-dimensions.interface';
import Konva from 'konva';
import { StageEventService } from '../../services/stage-event.service';
import { StageMode } from '../../enums/stage-mode.enum';

export interface WorkspaceStageProps {
  stageProperties: StageProperties;
  setStageProperties: (stageProperties: StageProperties) => void;
  setStageDimensions?: (stageDimensions: StageDimensions) => void;
  draggable: boolean;
  stageMode: StageMode;
  children: React.ReactNode;
  onMouseDown: (event: Konva.KonvaEventObject<MouseEvent>) => void;
  onMouseMove: (event: Konva.KonvaEventObject<MouseEvent>) => void;
  onMouseUp: (event: Konva.KonvaEventObject<MouseEvent>) => void;
}

export function WorkspaceStage(
  props: WorkspaceStageProps,
  ref: React.MutableRefObject<Konva.Stage | null>
) {

  useSynchronizeStageDimensions(
    {positionX: 0, positionY: 0, scale: 1},
    props.setStageProperties,
    props.setStageDimensions
  );

  const handleWheelEvent = (event: Konva.KonvaEventObject<WheelEvent>) =>
    StageEventService.handleWheelEvent(
      event,
      null,
      props.stageProperties,
      props.setStageProperties,
      props.stageMode
    );

  const handleDragEndEvent = (event: Konva.KonvaEventObject<DragEvent>) =>
    StageEventService.handleDragEnd(
      event,
      null,
      props.stageProperties,
      props.setStageProperties,
      props.stageMode
    )

  return (
    <Stage
      ref={ref}
      x={props.stageProperties.x}
      y={props.stageProperties.y}
      height={props.stageProperties.height}
      width={props.stageProperties.width}
      scaleX={props.stageProperties.scale}
      scaleY={props.stageProperties.scale}
      draggable={props.draggable}
      onDragEnd={handleDragEndEvent}
      onWheel={handleWheelEvent}
      onMouseDown={props.onMouseDown}
      onMouseMove={props.onMouseMove}
      onMouseUp={props.onMouseUp}
    >
      {props.children}
    </Stage>
  );
}

export default React.forwardRef(WorkspaceStage);
