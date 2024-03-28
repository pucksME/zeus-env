import Konva from 'konva';
import { useStore } from '../store';
import { DesignerStageEventService } from '../modules/designer/services/designer-stage-event.service';
import React from 'react';
import { Box } from 'konva/lib/shapes/Transformer';
import { StageProperties } from '../interfaces/stage-properties.interface';
import { StageMode } from '../enums/stage-mode.enum';
import { AppUtils } from '../app.utils';

export abstract class StageEventService {

  // https://konvajs.org/docs/sandbox/Zooming_Relative_To_Pointer.html [accessed 26/5/2021, 18:14]
  // https://stackoverflow.com/questions/52054848/how-to-react-konva-zooming-on-scroll
  // (relevant answers:
  // - https://stackoverflow.com/a/52069438
  // - https://stackoverflow.com/a/67199093
  // ) [accessed 26/5/2021, 18:14]
  static handleWheelEvent(
    event: Konva.KonvaEventObject<WheelEvent>,
    workspaceUuid: string | null,
    stageProperties: StageProperties,
    setStageProperties: (stageProperties: StageProperties) => void,
    stageMode: StageMode
  ) {
    const zoomFactor = 1.1;
    event.evt.preventDefault();
    const stage = event.target.getStage();
    const pointerPosition = stage.getPointerPosition();

    const textEditorState = useStore.getState().textEditorState;
    const resetTextEditorState = useStore.getState().resetTextEditorState;

    if ((stageMode === StageMode.DESIGNER || stageMode === StageMode.DESIGNER_BLUEPRINT_COMPONENT) &&
      textEditorState.active) {
      resetTextEditorState();
    }

    const moveTo = {
      x: (pointerPosition.x - stageProperties.x) / stageProperties.scale,
      y: (pointerPosition.y - stageProperties.y) / stageProperties.scale
    };

    const stageScaleAfterZoom = event.evt.deltaY < 0
      ? stageProperties.scale * zoomFactor
      : stageProperties.scale / zoomFactor;

    const stagePropertiesAfterZoom = {
      x: pointerPosition.x - (moveTo.x * stageScaleAfterZoom),
      y: pointerPosition.y - (moveTo.y * stageScaleAfterZoom),
      scale: stageScaleAfterZoom
    };

    setStageProperties({ ...stageProperties, ...stagePropertiesAfterZoom });

    if (stageMode === StageMode.DESIGNER) {
      DesignerStageEventService.synchronizeWorkspaceProperties(workspaceUuid, {
        positionX: stagePropertiesAfterZoom.x,
        positionY: stagePropertiesAfterZoom.y,
        scale: stagePropertiesAfterZoom.scale
      });
    }
  }

  static handleDragEnd(
    event: Konva.KonvaEventObject<DragEvent>,
    workspaceUuid: string | null,
    stageProperties: StageProperties,
    setStageProperties: (stageProperties: StageProperties) => void,
    stageMode: StageMode
  ) {
    event.evt.preventDefault();
    const stage = event.target.getStage();
    const position = { x: stage.x(), y: stage.y() };

    setStageProperties({ ...stageProperties, ...position, scale: stageProperties.scale });
    stage.position(position);

    if (stageMode === StageMode.DESIGNER) {
      DesignerStageEventService.synchronizeWorkspacePosition(workspaceUuid, {
        positionX: position.x,
        positionY: position.y
      });
    }
  }

  static handleTransformBoundBox(
    transformerRef: React.MutableRefObject<Konva.Transformer | null>,
    oldBox: Box,
    newBox: Box
  ): Box {
    if (transformerRef.current === null || (newBox.height >= 1 && newBox.width >= 1)) {
      return newBox;
    }

    transformerRef.current.stopTransform();
    return oldBox;
  }

  static handleMouseDownPointerTool(
    event: Konva.KonvaEventObject<MouseEvent>,
    stageProperties: StageProperties,
    transformerRef: React.MutableRefObject<Konva.Transformer | null>
  ): boolean {
    const stage = event.target.getStage();
    const pointerPosition = stage.getPointerPosition();
    const setSelectionRectangleProperties = useStore.getState().setSelectionRectangleProperties;

    if (AppUtils.includesMousePointer(transformerRef.current.getClientRect(), pointerPosition)) {
      return true;
    }

    setSelectionRectangleProperties({
      height: 0,
      width: 0,
      positionX: (pointerPosition.x - stageProperties.x) / stageProperties.scale,
      positionY: (pointerPosition.y - stageProperties.y) / stageProperties.scale,
      active: true
    });
    return false;
  }

  static handleMouseMovePointerTool(
    event: Konva.KonvaEventObject<MouseEvent>,
    stageProperties: StageProperties
  ): boolean {
    const selectionRectangleProperties = useStore.getState().selectionRectangleProperties;
    const setSelectionRectangleProperties = useStore.getState().setSelectionRectangleProperties;

    if (!selectionRectangleProperties.active) {
      return true;
    }

    const stage = event.target.getStage();
    const pointerPosition = stage.getPointerPosition();

    setSelectionRectangleProperties({
      ...selectionRectangleProperties,
      height: ((pointerPosition.y - stageProperties.y) / stageProperties.scale) - selectionRectangleProperties.positionY,
      width: ((pointerPosition.x - stageProperties.x) / stageProperties.scale) - selectionRectangleProperties.positionX
    });
    return false;
  }

  static handleMouseUpPointerTool(
    event: Konva.KonvaEventObject<MouseEvent>,
    transformerRef: React.MutableRefObject<Konva.Transformer | null>
  ): boolean {
    const selectionRectangleProperties = useStore.getState().selectionRectangleProperties;
    // To avoid trying to re-select on mouse up when transforming.
    // If cursor is not inside the selection when transforming,
    // the selected shapes would be removed from the selection
    if (!selectionRectangleProperties.active) {
      return true;
    }

    const stage = event.target.getStage();
    const pointerPosition = stage.getPointerPosition();
    const resetSelectionRectangleProperties = useStore.getState().resetSelectionRectangleProperties;

    // stop when clicking into the current selection
    if (AppUtils.includesMousePointer(transformerRef.current.getClientRect(), pointerPosition) &&
      selectionRectangleProperties.height === 0 &&
      selectionRectangleProperties.width === 0) {
      resetSelectionRectangleProperties();
      return true;
    }

    return false;
  }

}
