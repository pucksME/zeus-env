import React, { useRef, useState } from 'react';

import './stage-view.module.scss';
import { Group, Rect, Text } from 'react-konva';
import { ComponentDto, ScaleOrigin, ViewDto } from '../../../../../gen/api-client';
import Konva from 'konva';
import { DesignerStageGroupName } from '../../enums/designer-stage-group-name.enum';
import { ToolType } from '../../../../enums/tool-type.enum';
import { useQueryClient } from 'react-query';
import { useStore } from '../../../../store';
import { DesignerStageUtils } from '../../designer-stage.utils';
import { useScaleElements, useTranslateElements } from '../../data/component-data.hooks';
import { DesignerViewEventService } from '../../services/designer-view-event.service';
import { useScaleView, useTranslateView } from '../../data/view-data.hooks';
import colors from '../../../../../assets/styling/colors.json';
import { AppUtils } from '../../../../app.utils';

export interface StageViewProps {
  active: boolean;
  activeTool: ToolType;
  workspaceUuid: string;
  view: ViewDto;
}

export function StageView(props: StageViewProps) {
  const viewRef = useRef<Konva.Group | null>(null);
  const selectedComponentsRef = useRef<Konva.Group | null>(null);
  const viewBackgroundRef = useRef(null);

  const activeTool = useStore(state => state.activeDesignerTool);
  const stageProperties = useStore(state => state.designerStageProperties);
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const endFocusComponent = useStore(state => state.endFocusComponent);
  const transformActiveView = useStore(state => state.transformActiveDesignerView);
  const setTransformActiveView = useStore(state => state.setTransformActiveDesignerView);
  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const designerStageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);

  const [positionBeforeTransform, setPositionBeforeTransform] = useState<{ x: number, y: number }>(
    { x: 0, y: 0 }
  );
  const [scaleOrigin, setScaleOrigin] = useState<ScaleOrigin | null>(null);
  const queryClient = useQueryClient();

  const scaleComponents = useScaleElements(
    selectedComponentsRef,
    scaleOrigin,
    positionBeforeTransform,
    props.workspaceUuid
  );

  const scaleView = useScaleView(
    queryClient,
    viewBackgroundRef,
    props.view.uuid,
    scaleOrigin,
    positionBeforeTransform,
    props.workspaceUuid
  )

  const translateElements = useTranslateElements(selectedComponentsRef, props.workspaceUuid);

  const translateView = useTranslateView(
    queryClient,
    viewRef,
    props.view.uuid,
    props.workspaceUuid
  )

  const focusedComponent = (focusedComponentUuid === null)
    ? null
    : AppUtils.findInTrees<ComponentDto>(props.view.components, focusedComponentUuid).node;

  if (focusedComponent === undefined && !designerStageBlueprintComponentProperties.active) {
    return null;
  }

  const handleDragEnd = (event: Konva.KonvaEventObject<DragEvent>) =>
    DesignerViewEventService.handleDragEnd(
      event,
      (transformActiveView)
        ? translateView.mutate
        : translateElements.mutate
    );

  const handleTransformStart = (event: Konva.KonvaEventObject<Event>) =>
    DesignerViewEventService.handleTransformStart(event, setScaleOrigin, setPositionBeforeTransform);

  const handleTransformEnd = (event: Konva.KonvaEventObject<Event>) =>
    DesignerViewEventService.handleTransformEnd(
      event,
      scaleOrigin,
      (transformActiveView)
        ? scaleView.mutate
        : scaleComponents.mutate
    );

  const handleViewBackgroundDoubleClick = (event: Konva.KonvaEventObject<MouseEvent>) => {

    if (activeTool !== ToolType.POINTER || event.currentTarget.parent.id() !== props.view.uuid) {
      return;
    }

    if (focusedComponent !== null) {
      endFocusComponent();
    }

    setTransformActiveView(true);
  }

  const {
    components,
    selectedComponents
  } = DesignerStageUtils.buildComponentTrees(
    props.view.components,
    stageProperties.scale,
    props.view
  );

  return (
    <Group
      draggable={transformActiveView &&
        activeViewUuid !== null &&
        activeViewUuid === props.view.uuid &&
        activeTool === ToolType.POINTER}
      onDragEnd={handleDragEnd}
      ref={viewRef}>
      <Text
        text={props.view.name}
        fontSize={26}
        fill={colors.text.secondary}
        x={props.view.positionX}
        y={props.view.positionY - 35}
      />
    <Group
      key={props.view.uuid}
      id={props.view.uuid}
      x={props.view.positionX}
      y={props.view.positionY}
      clipX={props.active ? undefined : 0}
      clipY={props.active ? undefined : 0}
      clipHeight={props.active ? undefined : props.view.height}
      clipWidth={props.active ? undefined : props.view.width}
    >
      <Rect
        name={'view-background'}
        height={props.view.height}
        width={props.view.width}
        fill={'#ffffff'}
        onDblClick={handleViewBackgroundDoubleClick}
        onTransformStart={handleTransformStart}
        onTransformEnd={handleTransformEnd}
        ref={viewBackgroundRef}
      />
      <Group name={DesignerStageGroupName.COMPONENTS}>{components}</Group>
      <Group
        name={DesignerStageGroupName.COMPONENTS_SELECTED}
        draggable={props.activeTool === ToolType.POINTER}
        onDragEnd={handleDragEnd}
        onTransformStart={handleTransformStart}
        onTransformEnd={handleTransformEnd}
        ref={selectedComponentsRef}
      >
        {selectedComponents}
      </Group>
      {props.active && !transformActiveView
        ? <Rect
          height={props.view.height}
          width={props.view.width}
          fillAfterStrokeEnabled={true}
          stroke={'#dddddd'}
          strokeWidth={2}
          fillEnabled={false}
        />
        : null}
    </Group>
    </Group>
  );
}

export default StageView;
