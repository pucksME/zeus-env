import React, { useRef } from 'react';

import './workspace-designer.module.scss';
import { Layer } from 'react-konva';
import { DesignerStageEventService } from '../../services/designer-stage-event.service';
import { QueryClientProvider, useQueryClient } from 'react-query';
import StageView from '../../components/stage-view/stage-view';
import TextEditor from '../../components/text-editor/text-editor';
import TextStyleEditor from '../../components/text-style-editor/text-style-editor';
import { useStore } from '../../../../store';
import Konva from 'konva';
import { useSaveComponent } from '../../data/component-data.hooks';
import { useSaveShape } from '../../data/shape-data.hooks';
import SelectionRectangle from '../../../../components/selection-rectangle/selection-rectangle';
import FormCreatorPreviewRectangle
  from '../../components/form-creator-preview-rectangle/form-creator-preview-rectangle';
import FormCreatorPreviewCircle from '../../components/form-creator-preview-circle/form-creator-preview-circle';
import SelectionTransformer from '../../../../components/selection-transformer/selection-transformer';
import SynchronizedWorkspaceStage
  from '../../../../components/synchronized-workspace-stage/synchronized-workspace-stage';
import { WorkspaceDesignerDto } from '../../../../../gen/api-client';
import { ToolType } from '../../../../enums/tool-type.enum';
import { StageMode } from '../../../../enums/stage-mode.enum';
import { useSynchronizeDesignerTransformer } from './workspace-designer.hooks';

export interface WorkspaceDesignerProps {
  workspace: WorkspaceDesignerDto;
}

export function WorkspaceDesigner(props: WorkspaceDesignerProps) {

  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const stageProperties = useStore(state => state.designerStageProperties);
  const setStageProperties = useStore(state => state.setDesignerStageProperties);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);
  const activeTool = useStore(state => state.activeDesignerTool);
  const textEditorState = useStore(state => state.textEditorState);
  const queryClient = useQueryClient();

  const stageRef = useRef<Konva.Stage | null>(null);
  const transformerRef = useRef<Konva.Transformer | null>(null);
  const selectionRectangleRef = useRef<Konva.Rect | null>(null);

  useSynchronizeDesignerTransformer(props.workspace, stageRef, transformerRef);

  const saveComponent = useSaveComponent(props.workspace);
  const saveShape = useSaveShape(props.workspace);

  const handleMouseDown = (event: Konva.KonvaEventObject<MouseEvent>) =>
    DesignerStageEventService.handleMouseDown(event, transformerRef, StageMode.DESIGNER);

  const handleMouseMove = (event: Konva.KonvaEventObject<MouseEvent>) =>
    DesignerStageEventService.handleMouseMove(event);

  const handleMouseUp = (event: Konva.KonvaEventObject<MouseEvent>) =>
    DesignerStageEventService.handleMouseUp(
      event,
      props.workspace,
      transformerRef,
      selectionRectangleRef,
      saveComponent.mutate,
      saveShape.mutate
  );

  // QueryClientProvider must be applied again:
  // https://github.com/konvajs/react-konva/issues/188 [accessed 4/6/2021, 00:17]
  return (
    <div>
      <SynchronizedWorkspaceStage
        ref={stageRef}
        workspace={props.workspace}
        draggable={activeTool === ToolType.NAVIGATOR}
        stageMode={StageMode.DESIGNER}
        stageProperties={stageProperties}
        setStageProperties={setStageProperties}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
      >
        <Layer>
          <QueryClientProvider client={queryClient}>
            {props.workspace.views.map(viewDto => <StageView
                key={viewDto.uuid}
                active={viewDto.uuid === activeViewUuid}
                activeTool={activeTool}
                workspaceUuid={props.workspace.uuid}
                view={viewDto}
              />
            )}
          </QueryClientProvider>
          <FormCreatorPreviewRectangle/>
          <FormCreatorPreviewCircle/>
          <SelectionRectangle
            ref={selectionRectangleRef}
            activeTool={activeTool}
            stageProperties={stageProperties}
          />
          <SelectionTransformer
            ref={transformerRef}
            visible={!stageBlueprintComponentProperties.active &&
              activeTool === ToolType.POINTER &&
              !textEditorState.active}
            resizeable={true}
          />
        </Layer>
      </SynchronizedWorkspaceStage>
      <TextEditor workspace={props.workspace} stageMode={StageMode.DESIGNER}/>
      <TextStyleEditor workspace={props.workspace}/>
    </div>
  );
}

export default WorkspaceDesigner;
