import { DesignerUtils } from '../designer.utils';
import Konva from 'konva';
import { UseMutateFunction } from 'react-query';
import {
  BlueprintElementDto,
  ElementDto,
  ElementType,
  ScaleOrigin,
  ShapeDto,
  UpdatedElementsDto,
  ViewDto
} from '../../../../gen/api-client';
import { ToolType } from '../../../enums/tool-type.enum';
import { useStore } from '../../../store';
import spacing from '../../../../assets/styling/spacing.json';
import { StageMode } from '../../../enums/stage-mode.enum';

export abstract class DesignerViewEventService {

  static handleDragEnd(
    event: Konva.KonvaEventObject<DragEvent>,
    translateMutation: UseMutateFunction<UpdatedElementsDto | ViewDto, unknown, { x: number, y: number }, void>
  ) {
    event.cancelBubble = true;
    translateMutation({ x: event.currentTarget.x(), y: event.currentTarget.y() });
  }

  static handleTransformStart(
    event: Konva.KonvaEventObject<Event>,
    setScaleOrigin: (scaleOrigin: ScaleOrigin) => void,
    setPositionBeforeTransform: (position: { x: number, y: number }) => void
  ) {
    setScaleOrigin(DesignerUtils.mapToScaleOrigin((event.evt.target as unknown as Konva.Rect).name()));
    setPositionBeforeTransform(event.currentTarget.getPosition());
  }

  static handleTransformEnd(
    event: Konva.KonvaEventObject<Event>,
    scaleOrigin: ScaleOrigin | null,
    scaleMutation: UseMutateFunction<UpdatedElementsDto | ViewDto, unknown, { x: number, y: number }, void>
  ) {
    if (scaleOrigin === null) {
      console.error('Could not scale elements: scale origin was not set');
      return;
    }

    const scaleAfterTransform = event.currentTarget.scale();
    scaleMutation({ x: scaleAfterTransform.x, y: scaleAfterTransform.y });
  }

  static handleDoubleClick(
    event: Konva.KonvaEventObject<MouseEvent>,
    viewDto: ViewDto | null,
    component: {
      uuid: string,
      positionX: number,
      positionY: number,
      elements: (ElementDto | BlueprintElementDto)[]
    },
    pathCoordinates: { x: number, y: number },
    parentComponents: {uuid: string, isFocused: boolean}[],
    stageMode: StageMode.DESIGNER | StageMode.DESIGNER_BLUEPRINT_COMPONENT
  ) {

    const focusedComponentUuid = useStore.getState().focusedComponentUuid;
    const activeDesignerTool = useStore.getState().activeDesignerTool;
    const focusComponent = useStore.getState().focusComponent;
    const transformActiveView = useStore.getState().transformActiveDesignerView;
    const setTransformActiveView = useStore.getState().setTransformActiveDesignerView;
    const designerStageBlueprintComponentProperties = useStore.getState().designerStageBlueprintComponentProperties;
    const resetDesignerStageBlueprintComponentProperties = useStore.getState().resetDesignerStageBlueprintComponentProperties;

    if (activeDesignerTool !== ToolType.POINTER) {
      return;
    }

    if (designerStageBlueprintComponentProperties.active && stageMode !== StageMode.DESIGNER_BLUEPRINT_COMPONENT) {
      resetDesignerStageBlueprintComponentProperties();
    }

    if (component.uuid === focusedComponentUuid) {
      return;
    }

    if (transformActiveView) {
      setTransformActiveView(false);
    }

    if (event.target instanceof Konva.Text) {
      const shapeElement = component.elements.find(
        element => element.type === ElementType.Shape && element.element.uuid === event.target.id()
      );

      if (shapeElement !== undefined) {
        DesignerViewEventService.handleDoubleClickText(
          event,
          shapeElement.element as ShapeDto,
          {
            x: ((viewDto === null) ? 0 : viewDto.positionX) + pathCoordinates.x + component.positionX,
            y: ((viewDto === null) ? 0 : viewDto.positionY) + pathCoordinates.y + component.positionY
          },
          stageMode
        )
        return;
      }
    }

    if (parentComponents.length === 0) {
      if (focusedComponentUuid !== null &&
        !designerStageBlueprintComponentProperties.active &&
        stageMode === StageMode.DESIGNER) {
        return;
      }
      return focusComponent(component.uuid);
    }

    const focusedParentIndex = parentComponents.findIndex(parent => parent.isFocused);

    if (focusedParentIndex === -1) {
      return focusComponent(parentComponents[parentComponents.length - 1].uuid);
    }

    if (focusedParentIndex === 0) {
      return focusComponent(component.uuid);
    }

    focusComponent(parentComponents[focusedParentIndex - 1].uuid);
  }

  static handleDoubleClickText(
    event: Konva.KonvaEventObject<MouseEvent>,
    shapeDto: ShapeDto,
    pathCoordinates: { x: number, y: number },
    stageMode: StageMode.DESIGNER | StageMode.DESIGNER_BLUEPRINT_COMPONENT
  ) {
    const stageProperties = useStore.getState().designerStageProperties;
    const stageBlueprintComponentProperties = useStore.getState().designerStageBlueprintComponentProperties;
    const setTextEditorState = useStore.getState().setTextEditorState;
    setTextEditorState({
      active: true,
      position: {
        x: (stageMode === StageMode.DESIGNER)
          ? ((shapeDto.positionX + pathCoordinates.x) * stageProperties.scale) +
          stageProperties.x + spacing.toolbox.width + spacing.toolbar.width
          : ((shapeDto.positionX + pathCoordinates.x) * stageBlueprintComponentProperties.scale) +
          stageBlueprintComponentProperties.x,
        y: (stageMode === StageMode.DESIGNER)
          ? ((shapeDto.positionY + pathCoordinates.y) * stageProperties.scale) +
          stageProperties.y + spacing.navigation.height
          : ((shapeDto.positionY + pathCoordinates.y) * stageBlueprintComponentProperties.scale) +
          stageBlueprintComponentProperties.y + spacing.window.topPanelHeight
      },
      shape: shapeDto
    });
  }
}
