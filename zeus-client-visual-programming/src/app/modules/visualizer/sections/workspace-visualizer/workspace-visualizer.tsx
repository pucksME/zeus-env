import React, { useRef } from 'react';

import './workspace-visualizer.module.scss';
import { Layer } from 'react-konva';
import { QueryClientProvider, useQueryClient } from 'react-query';
import VisualizerStage from '../../components/visualizer-stage/visualizer-stage';
import { WorkspaceVisualizerDto } from '../../../../../gen/api-client';
import WorkspaceStage from '../../../../components/workspace-stage/workspace-stage';
import { useStore } from '../../../../store';
import { StageMode } from '../../../../enums/stage-mode.enum';
import { ToolType } from '../../../../enums/tool-type.enum';
import SelectionTransformer from '../../../../components/selection-transformer/selection-transformer';
import Konva from 'konva';
import SelectionRectangle from '../../../../components/selection-rectangle/selection-rectangle';
import { VisualizerStageEventService } from '../../services/visualizer-stage-event.service';
import { useSynchronizeVisualizerTransformer } from './workspace-visualizer.hooks';
import CodeModuleInstancesConnectionEditor
  from '../../components/code-module-instances-connection-editor/code-module-instances-connection-editor';
import { useConnections } from '../../data/code-module-instance-data.hooks';
import CodeModuleInstancesConnection
  from '../../components/code-module-instances-connection/code-module-instances-connection';
import {
  VisualizerSelectionTransformerEventService
} from '../../services/visualizer-selection-transformer-event.service';

export interface WorkspaceVisualizerProps {
  componentUuid: string;
  workspaceVisualizerDto: WorkspaceVisualizerDto;
}

export function WorkspaceVisualizer(props: WorkspaceVisualizerProps) {
  const {isLoading, isError, codeModuleInstancesConnectionDtos, error} = useConnections(props.componentUuid);
  const queryClient = useQueryClient();
  const visualizerStageProperties = useStore(state => state.visualizerStageProperties);
  const setVisualizerStageProperties = useStore(state => state.setVisualizerStageProperties);
  const setVisualizerStageDimensions = useStore(state => state.setVisualizerStageDimensions);
  const activeTool = useStore(state => state.activeVisualizerTool);

  const stageRef = useRef<Konva.Stage | null>(null);
  const transformerRef = useRef<Konva.Transformer | null>(null);
  const selectionRectangleRef = useRef<Konva.Rect | null>(null);

  useSynchronizeVisualizerTransformer(stageRef, transformerRef);

  if (isLoading) {
    return <div>Loading ...</div>;
  }

  if (isError) {
    return <div>{error['message']}</div>;
  }

  const handleMouseDown = (event: Konva.KonvaEventObject<MouseEvent>) =>
    VisualizerStageEventService.handleMouseDown(
      event,
      transformerRef
    );

  const handleMouseMove = (event: Konva.KonvaEventObject<MouseEvent>) =>
    VisualizerStageEventService.handleMouseMove(event);

  const handleMouseUp = (event: Konva.KonvaEventObject<MouseEvent>) =>
    VisualizerStageEventService.handleMouseUp(
      event,
      transformerRef,
      selectionRectangleRef
    );

  const handleDragStart = (event: Konva.KonvaEventObject<DragEvent>) =>
    VisualizerSelectionTransformerEventService.handleDragStart(event);

  const handleDragMove = (event: Konva.KonvaEventObject<DragEvent>) =>
    VisualizerSelectionTransformerEventService.handleDragMove(event);

  return (
    <div>
      <WorkspaceStage
        ref={stageRef}
        stageProperties={visualizerStageProperties}
        setStageProperties={setVisualizerStageProperties}
        setStageDimensions={setVisualizerStageDimensions}
        draggable={activeTool === ToolType.NAVIGATOR}
        stageMode={StageMode.VISUALIZER}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
      >
        <Layer>
          {codeModuleInstancesConnectionDtos.map(
            (codeModuleInstancesConnectionDto, index) => <CodeModuleInstancesConnection
              key={index}
              codeModuleInstanceDtos={props.workspaceVisualizerDto.codeModuleInstances}
              codeModuleInstanceConnectionDto={codeModuleInstancesConnectionDto}
            />
          )}
          <QueryClientProvider client={queryClient}>
            <CodeModuleInstancesConnectionEditor
              componentUuid={props.componentUuid}
              codeModuleInstanceDtos={props.workspaceVisualizerDto.codeModuleInstances}
            />
            <VisualizerStage
              componentUuid={props.componentUuid}
              codeModuleInstanceDtos={props.workspaceVisualizerDto.codeModuleInstances}
            />
          </QueryClientProvider>
          <SelectionRectangle
            ref={selectionRectangleRef}
            activeTool={activeTool}
            stageProperties={visualizerStageProperties}
          />
          <SelectionTransformer
            ref={transformerRef}
            visible={activeTool === ToolType.POINTER}
            resizeable={false}
            onDragStart={handleDragStart}
            onDragMove={handleDragMove}
          />
        </Layer>
      </WorkspaceStage>
    </div>
  );
}

export default WorkspaceVisualizer;
