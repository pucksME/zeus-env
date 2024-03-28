import Konva from 'konva';
import { useStore } from '../../../store';
import { DesignerUtils } from '../designer.utils';
import React from 'react';
import { StageEventService } from '../../../services/stage-event.service';
import { ToolType } from '../../../enums/tool-type.enum';
import { ScaleOrigin, UpdatedElementsDto } from '../../../../gen/api-client';
import { UseMutateFunction } from 'react-query';

export abstract class DesignerStageBlueprintComponentEventService {

  static handleMouseUp(
    event: Konva.KonvaEventObject<MouseEvent>,
    transformerRef: React.MutableRefObject<Konva.Transformer | null>,
    selectionRectangleRef: React.MutableRefObject<Konva.Rect | null>
  ) {
    event.evt.preventDefault();

    const activeTool = useStore.getState().activeDesignerTool;

    if (activeTool === ToolType.POINTER) {
      return DesignerStageBlueprintComponentEventService.handleMouseUpPointerTool(
        event,
        transformerRef,
        selectionRectangleRef
      );
    }
  }

  private static handleMouseUpPointerTool(
    event: Konva.KonvaEventObject<MouseEvent>,
    transformerRef: React.MutableRefObject<Konva.Transformer | null>,
    selectionRectangleRef: React.MutableRefObject<Konva.Rect | null>
  ) {
    if (StageEventService.handleMouseUpPointerTool(event, transformerRef)) {
      return;
    }

    const designerStageBlueprintComponentProperties = useStore.getState().designerStageBlueprintComponentProperties;
    const focusedComponentUuid = useStore.getState().focusedComponentUuid;
    const resetSelectionRectangleProperties = useStore.getState().resetSelectionRectangleProperties;
    const setSelectedElementUuids = useStore.getState().setSelectedComponentUuids;

    const stage = event.target.getStage();

    const currentlySelectedElementsNodes = DesignerUtils.getCurrentlySelectedElementsNodes(stage);

    if (currentlySelectedElementsNodes === null) {
      resetSelectionRectangleProperties();
      return;
    }

    const componentGroup = stage.findOne<Konva.Group>('#' + ((focusedComponentUuid === null)
      ? designerStageBlueprintComponentProperties.blueprintComponentUuid
      : focusedComponentUuid));

    if (componentGroup === undefined) {
      resetSelectionRectangleProperties();
      return;
    }

    setSelectedElementUuids(DesignerUtils.getSelectedElementUuids(
      [...componentGroup.getChildren(), ...currentlySelectedElementsNodes],
      selectionRectangleRef.current.getClientRect()
    ));
    resetSelectionRectangleProperties();
  }

  static handleTransformStart(
    event: Konva.KonvaEventObject<Event>,
    setScaleOrigin: (scaleOrigin: ScaleOrigin) => void,
    setPositionBeforeTransform: (position: { x: number, y: number }) => void
  ) {
    setScaleOrigin(DesignerUtils.mapToScaleOrigin((event.evt.target as unknown as Konva.Rect).name()));
    setPositionBeforeTransform(event.currentTarget.position());
  }

  static handleTransformEnd(
    event: Konva.KonvaEventObject<Event>,
    scaleOrigin: ScaleOrigin | null,
    scaleBlueprintElementsMutation: UseMutateFunction<UpdatedElementsDto, unknown, { x: number, y: number }, void>
  ) {
    if (scaleOrigin === null) {
      console.error('Could not scale blueprint elements: scale origin was not set');
      return;
    }

    const scaleAfterTransform = event.currentTarget.scale();
    scaleBlueprintElementsMutation({ x: scaleAfterTransform.x, y: scaleAfterTransform.y });
  }

  static handleDragEnd(
    event: Konva.KonvaEventObject<DragEvent>,
    translateBlueprintElementsMutation: UseMutateFunction<UpdatedElementsDto, unknown, { x: number, y: number }, void>
  ) {
    translateBlueprintElementsMutation({ x: event.currentTarget.x(), y: event.currentTarget.y() });
  }
}
