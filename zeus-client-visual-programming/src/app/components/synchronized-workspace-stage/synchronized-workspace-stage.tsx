import React from 'react';

import './synchronized-workspace-stage.module.scss';
import { DesignerStageEventService } from '../../modules/designer/services/designer-stage-event.service';
import { Stage } from 'react-konva';
import { useSynchronizeStageDimensions } from '../../sections/workspace/workspace.hooks';
import { useStore } from '../../store';
import Konva from 'konva';
import { StageEventService } from '../../services/stage-event.service';
import { StageProperties } from '../../interfaces/stage-properties.interface';
import { StageMode } from '../../enums/stage-mode.enum';

export interface SynchronizedWorkspaceStageProps {
  workspace: {uuid: string, positionX: number, positionY: number, scale: number};
  children: React.ReactNode;
  draggable: boolean;
  stageMode: StageMode
  stageProperties: StageProperties;
  setStageProperties: (stageProperties: StageProperties) => void;
  onMouseDown: (event: Konva.KonvaEventObject<MouseEvent>) => void;
  onMouseMove: (event: Konva.KonvaEventObject<MouseEvent>) => void;
  onMouseUp: (event: Konva.KonvaEventObject<MouseEvent>) => void;
}

export function SynchronizedWorkspaceStage(
  props: SynchronizedWorkspaceStageProps,
  ref: React.MutableRefObject<Konva.Stage | null>
) {

  useSynchronizeStageDimensions(
    props.workspace,
    useStore.getState().setDesignerStageProperties,
    useStore.getState().setDesignerStageDimensions
  );

  const handleWheelEvent = (event: Konva.KonvaEventObject<WheelEvent>) =>
    StageEventService.handleWheelEvent(
      event,
      props.workspace.uuid,
      props.stageProperties,
      props.setStageProperties,
      props.stageMode
    );

  const handleDragEndEvent = (event: Konva.KonvaEventObject<DragEvent>) =>
    StageEventService.handleDragEnd(
      event,
      props.workspace.uuid,
      props.stageProperties,
      props.setStageProperties,
      props.stageMode
    );

  return (
    <Stage
      ref={ref}
      height={props.stageProperties.height}
      width={props.stageProperties.width}
      scaleX={props.stageProperties.scale}
      scaleY={props.stageProperties.scale}
      draggable={props.draggable}
      onDragStart={DesignerStageEventService.handleDragStart}
      onDragEnd={handleDragEndEvent}
      onWheel={handleWheelEvent}
      x={props.stageProperties.x}
      y={props.stageProperties.y}
      onMouseDown={props.onMouseDown}
      onMouseMove={props.onMouseMove}
      onMouseUp={props.onMouseUp}
    >
      {props.children}
    </Stage>
  );
}

export default React.forwardRef(SynchronizedWorkspaceStage);
