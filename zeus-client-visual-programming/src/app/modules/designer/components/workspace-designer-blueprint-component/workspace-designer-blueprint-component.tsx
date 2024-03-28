import React, { useEffect, useRef, useState } from 'react';

import './workspace-designer-blueprint-component.module.scss';
import { useStore } from '../../../../store';
import {
  useBlueprintComponentsWorkspace,
  useDeleteBlueprintElements,
  useScaleBlueprintElements,
  useTranslateBlueprintElements,
  useUpdateBlueprintComponentName
} from '../../data/blueprint-component-data.hooks';
import spacing from '../../../../../assets/styling/spacing.json';
import Konva from 'konva';
import { DesignerStageUtils } from '../../designer-stage.utils';
import { BlueprintComponentDto, ScaleOrigin, WorkspaceDesignerDto } from '../../../../../gen/api-client';
import { AppUtils } from '../../../../app.utils';
import WorkspaceStage from '../../../../components/workspace-stage/workspace-stage';
import { StageProperties } from '../../../../interfaces/stage-properties.interface';
import { ToolType } from '../../../../enums/tool-type.enum';
import { StageMode } from '../../../../enums/stage-mode.enum';
import { DesignerStageEventService } from '../../services/designer-stage-event.service';
import { Group, Layer } from 'react-konva';
import { QueryClientProvider, useQueryClient } from 'react-query';
import SelectionRectangle from '../../../../components/selection-rectangle/selection-rectangle';
import SelectionTransformer from '../../../../components/selection-transformer/selection-transformer';
import { DesignerStageGroupName } from '../../enums/designer-stage-group-name.enum';
import {
  DesignerStageBlueprintComponentEventService
} from '../../services/designer-stage-blueprint-component-event.service';
import { useSynchronizeDesignerBlueprintComponentTransformer } from './workspace-designer-blueprint-component.hooks';
import TextEditor from '../text-editor/text-editor';
import TextStyleEditor from '../text-style-editor/text-style-editor';
import Window from '../../../../components/window/window';
import { InputChangeEvent } from '../../../../components/input/input';
import DeleteIcon from '@material-ui/icons/DeleteForever';
import { IconButton } from '@material-ui/core';

export interface WorkspaceDesignerBlueprintComponentProps {
  workspaceDto: WorkspaceDesignerDto;
}

export function WorkspaceDesignerBlueprintComponent(
  props: WorkspaceDesignerBlueprintComponentProps
) {

  const activeTool = useStore(state => state.activeDesignerTool);
  const textEditorState = useStore(state => state.textEditorState);
  const resetTextEditorState = useStore(state => state.resetTextEditorState);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);
  const setStageBlueprintComponentProperties = useStore(state => state.setDesignerStageBlueprintComponentProperties);
  const resetStageBlueprintComponentProperties = useStore(state => state.resetDesignerStageBlueprintComponentProperties);
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const selectedElementUuids = useStore(state => state.selectedComponentUuids);
  const setSelectedElementUuids = useStore(state => state.setSelectedComponentUuids);
  const { isLoading, isError, blueprintComponentDtos } = useBlueprintComponentsWorkspace(props.workspaceDto.uuid);

  const stageRef = useRef<Konva.Stage | null>(null);
  const transformerRef = useRef<Konva.Transformer | null>(null);
  const selectionRectangleRef = useRef<Konva.Rect | null>(null);
  const selectedBlueprintComponentsRef = useRef<Konva.Group | null>(null);

  const blueprintComponentDto = (!blueprintComponentDtos)
    ? null
    : blueprintComponentDtos.find(
      blueprintComponent => blueprintComponent.uuid === stageBlueprintComponentProperties.blueprintComponentUuid
    );

  useSynchronizeDesignerBlueprintComponentTransformer(blueprintComponentDto, stageRef, transformerRef);
  const queryClient = useQueryClient();

  const workspaceMargin = 25;
  const workspacePadding = 25;
  const initialX = spacing.toolbar.width + spacing.toolbox.width + workspaceMargin;

  const [position, setPosition] = useState<{ x: number, y: number }>({ x: 0, y: 0 });
  const [editedBlueprintComponentName, setEditedBlueprintComponentName] = useState<string | null>(null);

  const [positionBeforeTransform, setPositionBeforeTransform] = useState<{ x: number, y: number }>(
    { x: 0, y: 0}
  );
  const [scaleOrigin, setScaleOrigin] = useState<ScaleOrigin | null>(null);

  const updateBlueprintComponentName = useUpdateBlueprintComponentName(props.workspaceDto.uuid);

  const scaleBlueprintElements = useScaleBlueprintElements(
    selectedBlueprintComponentsRef,
    scaleOrigin,
    positionBeforeTransform,
    props.workspaceDto.uuid
  );

  const translateBlueprintElements = useTranslateBlueprintElements(
    selectedBlueprintComponentsRef,
    props.workspaceDto.uuid
  );

  const deleteBlueprintElements = useDeleteBlueprintElements(props.workspaceDto.uuid, StageMode.DESIGNER);

  const handleChangeTitle = (event: InputChangeEvent) => setEditedBlueprintComponentName(event.value as string);

  const handleSaveTitle = () => {
    if (editedBlueprintComponentName === null) {
      return;
    }

    updateBlueprintComponentName.mutate({
      blueprintComponentUuid: stageBlueprintComponentProperties.blueprintComponentUuid,
      updateBlueprintComponentNameDto: { name: editedBlueprintComponentName }
    });
  };

  const handleMouseDownMove = () => {
    if (textEditorState.active) {
      resetTextEditorState();
    }
  };

  const handleResize = (height: number, width: number) => setStageBlueprintComponentProperties({
    ...stageBlueprintComponentProperties,
    ...{ height, width }
  });


  useEffect(() => {
    setPosition({
      x: initialX,
      y: Math.max(
        stageBlueprintComponentProperties.initialContainerPosition.y - (
          (stageBlueprintComponentProperties.height + spacing.window.topPanelHeight) / 2
        ),
        workspaceMargin
      )
    });

    if (stageRef.current === null) {
      return;
    }

    const blueprintContainerChildren = stageRef.current.getChildren()[0].getChildren(
      node => node.name() === DesignerStageGroupName.COMPONENT_ROOT
    );

    if (blueprintContainerChildren.length !== 1) {
      return;
    }

    setStageBlueprintComponentProperties({
      ...stageBlueprintComponentProperties,
      ...AppUtils.calculateContainerPropertiesToFitElements(
        {
          height: stageBlueprintComponentProperties.height,
          width: stageBlueprintComponentProperties.width
        },
        blueprintContainerChildren[0].getClientRect({ skipTransform: true }),
        {
          horizontal: workspacePadding,
          vertical: workspacePadding
        }
      )
    });
  }, [stageBlueprintComponentProperties.blueprintComponentUuid]);

  if (stageBlueprintComponentProperties.blueprintComponentUuid === null || isLoading || isError) {
    return null;
  }

  if (blueprintComponentDto === undefined) {
    return null;
  }

  if (focusedComponentUuid !== null &&
    AppUtils.findInTree(blueprintComponentDto, focusedComponentUuid).node === undefined) {
    return null;
  }

  const setStageProperties = (stageProperties: StageProperties) =>
    setStageBlueprintComponentProperties({
      ...stageBlueprintComponentProperties,
      ...stageProperties
    });

  const handleMouseDownStage = (event: Konva.KonvaEventObject<MouseEvent>) => {
    event.evt.cancelBubble = true;
    DesignerStageEventService.handleMouseDown(event, transformerRef, StageMode.DESIGNER_BLUEPRINT_COMPONENT);
  }

  const handleMouseMoveStage = (event: Konva.KonvaEventObject<MouseEvent>) =>
    DesignerStageEventService.handleMouseMove(event);

  const handleMouseUpStage = (event: Konva.KonvaEventObject<MouseEvent>) =>
    DesignerStageBlueprintComponentEventService.handleMouseUp(event, transformerRef, selectionRectangleRef);

  const handleTransformStart = (event: Konva.KonvaEventObject<Event>) =>
    DesignerStageBlueprintComponentEventService.handleTransformStart(event, setScaleOrigin, setPositionBeforeTransform);

  const handleTransformEnd = (event: Konva.KonvaEventObject<Event>) =>
    DesignerStageBlueprintComponentEventService.handleTransformEnd(event, scaleOrigin, scaleBlueprintElements.mutate);

  const handleDragEnd = (event: Konva.KonvaEventObject<DragEvent>) =>
    DesignerStageBlueprintComponentEventService.handleDragEnd(event, translateBlueprintElements.mutate);

  const handleDeleteButtonClick = () => deleteBlueprintElements.mutate();

  const handleClose = () => {
    if (textEditorState.active) {
      resetTextEditorState();
    }

    if (selectedElementUuids.length !== 0) {
      setSelectedElementUuids([]);
    }

    resetStageBlueprintComponentProperties();
  };

  const handleChangePosition = (position: { x: number, y: number }) => setPosition(position);

  const buildWindowActions = () => (
    <div>
      <IconButton size={'small'} onClick={handleDeleteButtonClick}>
        <DeleteIcon fontSize={'small'}/>
      </IconButton>
    </div>
  );

  const selectedComponents: JSX.Element[] = [];

  return (
    <Window
      id={blueprintComponentDto.uuid}
      title={blueprintComponentDto.name}
      visible={true}
      position={position}
      onChangePosition={handleChangePosition}
      minContentHeight={spacing.designerBlueprintComponentWorkspace.minStageHeight}
      minContentWidth={spacing.designerBlueprintComponentWorkspace.minStageWidth}
      actions={buildWindowActions()}
      onClose={handleClose}
      onMouseDownMove={handleMouseDownMove}
      onResize={handleResize}
      onChangeTitle={handleChangeTitle}
      onSaveTitle={handleSaveTitle}
    >
      <WorkspaceStage
        ref={stageRef}
        stageProperties={stageBlueprintComponentProperties}
        setStageProperties={setStageProperties}
        draggable={activeTool === ToolType.NAVIGATOR}
        stageMode={StageMode.DESIGNER_BLUEPRINT_COMPONENT}
        onMouseDown={handleMouseDownStage}
        onMouseMove={handleMouseMoveStage}
        onMouseUp={handleMouseUpStage}
      >
        <Layer>
          <QueryClientProvider client={queryClient}>
            {DesignerStageUtils.buildComponentTree<BlueprintComponentDto>(
              blueprintComponentDto,
              stageBlueprintComponentProperties.scale,
              false,
              null,
              selectedComponents,
              true,
              StageMode.DESIGNER_BLUEPRINT_COMPONENT
            )}
            <Group
              ref={selectedBlueprintComponentsRef}
              name={DesignerStageGroupName.COMPONENTS_SELECTED}
              draggable={activeTool === ToolType.POINTER}
              onTransformStart={handleTransformStart}
              onTransformEnd={handleTransformEnd}
              onDragEnd={handleDragEnd}
            >
              {selectedComponents}
            </Group>
            <SelectionRectangle
              ref={selectionRectangleRef}
              activeTool={activeTool}
              stageProperties={stageBlueprintComponentProperties}
            />
            <SelectionTransformer
              ref={transformerRef}
              visible={stageBlueprintComponentProperties.active &&
                activeTool === ToolType.POINTER &&
                !textEditorState.active}
              resizeable={true}
            />
          </QueryClientProvider>
        </Layer>
      </WorkspaceStage>
      <TextEditor workspace={props.workspaceDto} stageMode={StageMode.DESIGNER_BLUEPRINT_COMPONENT}/>
      <TextStyleEditor workspace={props.workspaceDto}/>
    </Window>
  );
}

export default WorkspaceDesignerBlueprintComponent;
