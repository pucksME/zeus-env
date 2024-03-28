import Konva from 'konva';
import React from 'react';
import { useStore } from '../../../store';
import { ToolType } from '../../../enums/tool-type.enum';
import { StageEventService } from '../../../services/stage-event.service';
import { VisualizerStageGroupName } from '../enums/visualizer-stage-group-name.enum';
import { UseMutateFunction } from 'react-query';
import { CodeModuleInstanceDto } from '../../../../gen/api-client';

export abstract class VisualizerStageEventService {
  static handleMouseDown(
    event: Konva.KonvaEventObject<MouseEvent>,
    transformerRef: React.MutableRefObject<Konva.Transformer | null>
  ) {
    event.evt.preventDefault();
    const activeTool = useStore.getState().activeVisualizerTool;
    const stageProperties = useStore.getState().visualizerStageProperties;

    if (activeTool === ToolType.POINTER) {
      StageEventService.handleMouseDownPointerTool(event, stageProperties, transformerRef);
      return;
    }
  }

  static handleMouseMove(event: Konva.KonvaEventObject<MouseEvent>) {
    event.evt.preventDefault();
    const activeTool = useStore.getState().activeVisualizerTool;
    const stageProperties = useStore.getState().visualizerStageProperties;

    if (activeTool === ToolType.POINTER) {
      StageEventService.handleMouseMovePointerTool(event, stageProperties);
      return;
    }
  }

  static handleMouseUp(
    event: Konva.KonvaEventObject<MouseEvent>,
    transformerRef: React.MutableRefObject<Konva.Transformer | null>,
    selectionRectangleRef: React.MutableRefObject<Konva.Rect | null>
  ) {
    event.evt.preventDefault();
    const activeTool = useStore.getState().activeVisualizerTool;

    if (activeTool === ToolType.POINTER) {
      return VisualizerStageEventService.handleMouseUpPointerTool(event, transformerRef, selectionRectangleRef);
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

    const resetSelectionRectangleProperties = useStore.getState().resetSelectionRectangleProperties;
    const setSelectedCodeModuleInstanceUuids = useStore.getState().setSelectedCodeModuleInstanceUuids;
    const stage = event.target.getStage();

    const codeModuleInstancesParentGroups = stage.find<Konva.Group>(
      node => node.name() === VisualizerStageGroupName.CODE_MODULE_INSTANCES ||
        node.name() === VisualizerStageGroupName.CODE_MODULE_INSTANCES_SELECTED
    );

    if (codeModuleInstancesParentGroups.length !== 2) {
      resetSelectionRectangleProperties();
      return;
    }

    const codeModuleInstanceGroups = [
      ...codeModuleInstancesParentGroups[0].getChildren(),
      ...codeModuleInstancesParentGroups[1].getChildren()
    ];

    setSelectedCodeModuleInstanceUuids(
      codeModuleInstanceGroups.filter(group => Konva.Util.haveIntersection(
        group.getClientRect(),
        selectionRectangleRef.current.getClientRect()
      )).map(group => group.id())
    );

    resetSelectionRectangleProperties();
  }

  static handleDragEnd(
    event: Konva.KonvaEventObject<DragEvent>,
    translateCodeModuleInstancesMutation: UseMutateFunction<CodeModuleInstanceDto[], unknown, {x: number, y: number}, unknown>
  ) {
    event.cancelBubble = true;
    translateCodeModuleInstancesMutation({x: event.currentTarget.x(), y: event.currentTarget.y()});
  }
}
