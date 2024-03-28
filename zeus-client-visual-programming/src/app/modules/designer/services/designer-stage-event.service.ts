import Konva from 'konva';
import { AppUtils } from '../../../app.utils';
import { ToolType } from '../../../enums/tool-type.enum';
import { DesignerUtils } from '../designer.utils';
import { CreateFormPreviewProperties } from '../interfaces/create-form-preview-properties.interface';
import { DesignerStageGroupName } from '../enums/designer-stage-group-name.enum';
import { ComponentDto, RectanglePropertiesDto, ShapeDto, WorkspaceDesignerDto } from '../../../../gen/api-client';
import colors from '../../../../assets/styling/colors.json';
import spacing from '../../../../assets/styling/spacing.json';
import { UseMutateFunction } from 'react-query';
import { SaveComponentMutation } from '../data/component-data.hooks';
import { DesignerWorkspaceService } from './designer-workspace.service';
import { SaveShapeMutation } from '../data/shape-data.hooks';
import React from 'react';
import { useStore } from '../../../store';
import { StageEventService } from '../../../services/stage-event.service';
import { StageMode } from '../../../enums/stage-mode.enum';

export abstract class DesignerStageEventService {

  static synchronizeWorkspaceProperties = AppUtils.debounce(DesignerWorkspaceService.updateProperties, 1000);
  static synchronizeWorkspacePosition = AppUtils.debounce(DesignerWorkspaceService.updatePosition, 1000);

  static handleMouseDown(
    event: Konva.KonvaEventObject<MouseEvent>,
    transformerRef: React.MutableRefObject<Konva.Transformer | null>,
    stageMode: StageMode
  ) {
    event.evt.preventDefault();

    const activeTool = useStore.getState().activeDesignerTool;
    const textEditorState = useStore.getState().textEditorState;
    const resetTextEditorState = useStore.getState().resetTextEditorState;
    const setCreateFormPreviewProperties = useStore.getState().setCreateFormPreviewProperties;
    const designerStageBlueprintComponentProperties = useStore.getState().designerStageBlueprintComponentProperties;
    const setDesignerStageBlueprintComponentProperties = useStore.getState().setDesignerStageBlueprintComponentProperties;
    const setSelectedElementUuids = useStore.getState().setSelectedComponentUuids;
    const focusedComponentUuid = useStore.getState().focusedComponentUuid;

    if (((stageMode === StageMode.DESIGNER && designerStageBlueprintComponentProperties.active) ||
      (stageMode === StageMode.DESIGNER_BLUEPRINT_COMPONENT && !designerStageBlueprintComponentProperties.active)) &&
      (!designerStageBlueprintComponentProperties.active || focusedComponentUuid === null)) {
      setSelectedElementUuids([]);
      setDesignerStageBlueprintComponentProperties({
        ...designerStageBlueprintComponentProperties,
        active: stageMode === StageMode.DESIGNER_BLUEPRINT_COMPONENT
      });
    }

    if (textEditorState.active) {
      resetTextEditorState();
    }

    if (activeTool === ToolType.POINTER) {
      return DesignerStageEventService.handleMouseDownPointerTool(event, transformerRef);
    }

    if (stageMode === StageMode.DESIGNER && DesignerUtils.isFormCreatorToolType(activeTool)) {
      return DesignerStageEventService.handleMouseDownFormCreatorTool(event, setCreateFormPreviewProperties);
    }
  }

  private static handleMouseDownPointerTool(
    event: Konva.KonvaEventObject<MouseEvent>,
    transformerRef: React.MutableRefObject<Konva.Transformer | null>
  ) {
    const designerStageProperties = useStore.getState().designerStageProperties;
    const designerStageBlueprintComponentProperties = useStore.getState().designerStageBlueprintComponentProperties;
    const transformActiveView = useStore.getState().transformActiveDesignerView;

    if (transformActiveView) {
      return;
    }

    StageEventService.handleMouseDownPointerTool(
      event,
      (designerStageBlueprintComponentProperties.active)
        ? designerStageBlueprintComponentProperties
        : designerStageProperties,
      transformerRef
    );
  }

  private static handleMouseDownFormCreatorTool(
    event: Konva.KonvaEventObject<MouseEvent>,
    setCreateFormPreviewProperties: (createFormPreviewProperties: CreateFormPreviewProperties) => void
  ) {

    if (useStore.getState().activeDesignerViewUuid === null) {
      return;
    }

    const stageProperties = useStore.getState().designerStageProperties;
    const transformActiveView = useStore.getState().transformActiveDesignerView;
    const setTransformActiveView = useStore.getState().setTransformActiveDesignerView;

    if (transformActiveView) {
      setTransformActiveView(false);
    }

    const stage = event.target.getStage();
    // const transform = viewRef.current.getAbsoluteTransform().copy().invert();
    // const pointerPosition = transform.point(stage.getPointerPosition());
    const pointerPosition = stage.getPointerPosition();

    setCreateFormPreviewProperties({
      height: 0,
      width: 0,
      positionX: (pointerPosition.x - stageProperties.x) / stageProperties.scale,
      positionY: (pointerPosition.y - stageProperties.y) / stageProperties.scale,
      toolUsed: useStore.getState().activeDesignerTool
    });
  }



  static handleMouseMove(event: Konva.KonvaEventObject<MouseEvent>) {
    event.evt.preventDefault();
    const designerStageProperties = useStore.getState().designerStageProperties;
    const designerStageBlueprintComponentProperties = useStore.getState().designerStageBlueprintComponentProperties;
    const activeTool = useStore.getState().activeDesignerTool;

    if (activeTool === ToolType.POINTER) {
      StageEventService.handleMouseMovePointerTool(
        event,
        (designerStageBlueprintComponentProperties.active)
          ? designerStageBlueprintComponentProperties
          : designerStageProperties
      );
      return;
    }

    if (DesignerUtils.isFormCreatorToolType(activeTool)) {
      return DesignerStageEventService.handleMouseMoveFormCreatorTool(event);
    }
  }



  private static handleMouseMoveFormCreatorTool(
    event: Konva.KonvaEventObject<MouseEvent>
  ) {
    const stageProperties = useStore.getState().designerStageProperties;
    const createFormPreviewProperties = useStore.getState().createFormPreviewProperties;
    const setCreateFormPreviewProperties = useStore.getState().setCreateFormPreviewProperties;

    if (createFormPreviewProperties.toolUsed === null) {
      return;
    }

    const stage = event.target.getStage();
    // const transform = viewRef.current.getAbsoluteTransform().copy().invert();
    // const pointerPosition = transform.point(stage.getPointerPosition());
    const pointerPosition = stage.getPointerPosition();

    setCreateFormPreviewProperties({
      ...createFormPreviewProperties,
      height: ((pointerPosition.y - stageProperties.y) / stageProperties.scale) - createFormPreviewProperties.positionY,
      width: ((pointerPosition.x - stageProperties.x) / stageProperties.scale) - createFormPreviewProperties.positionX
    });
  }

  static handleMouseUp(
    event: Konva.KonvaEventObject<MouseEvent>,
    workspaceDto: WorkspaceDesignerDto,
    transformerRef: React.MutableRefObject<Konva.Transformer | null>,
    selectionRectangleRef: React.MutableRefObject<Konva.Rect | null>,
    saveComponentMutation: UseMutateFunction<ComponentDto, unknown, SaveComponentMutation, WorkspaceDesignerDto> | null,
    saveShapeMutation: UseMutateFunction<ShapeDto, unknown, SaveShapeMutation, WorkspaceDesignerDto>
  ) {
    event.evt.preventDefault();

    const activeTool = useStore.getState().activeDesignerTool;
    if (activeTool === ToolType.POINTER) {
      return DesignerStageEventService.handleMouseUpPointerTool(
        event,
        transformerRef,
        selectionRectangleRef
      );
    }

    if (DesignerUtils.isFormCreatorToolType(activeTool)) {
      return DesignerStageEventService.handleMouseUpFormCreatorTool(
        event,
        workspaceDto,
        saveComponentMutation,
        saveShapeMutation
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

    const activeViewUuid = useStore.getState().activeDesignerViewUuid;
    const resetSelectionRectangleProperties = useStore.getState().resetSelectionRectangleProperties;
    const stage = event.target.getStage();

    if (activeViewUuid === null) {
      resetSelectionRectangleProperties();
      return;
    }

    const viewGroup = stage.findOne<Konva.Group>('#' + activeViewUuid);

    if (viewGroup === undefined) {
      resetSelectionRectangleProperties();
      return;
    }

    const currentlySelectedElementsNodes = DesignerUtils.getCurrentlySelectedElementsNodes(viewGroup);

    if (currentlySelectedElementsNodes === null) {
      resetSelectionRectangleProperties();
      return;
    }

    const focusedComponentUuid = useStore.getState().focusedComponentUuid;
    const setSelectedElementUuids = useStore.getState().setSelectedComponentUuids;

    const focusedComponentGroup = (focusedComponentUuid === null)
      ? null
      : stage.findOne<Konva.Group>('#' + focusedComponentUuid);

    if (focusedComponentGroup === undefined) {
      resetSelectionRectangleProperties();
      return;
    }

    const selectionRectangleClientRect = selectionRectangleRef.current.getClientRect();
    if (focusedComponentGroup === null) {

      const componentsGroup = viewGroup.findOne<Konva.Group>(
        '.' + DesignerStageGroupName.COMPONENTS
      );

      if (componentsGroup === undefined) {
        resetSelectionRectangleProperties();
        return;
      }

      setSelectedElementUuids(DesignerUtils.getSelectedElementUuids(
        [...componentsGroup.getChildren(), ...currentlySelectedElementsNodes],
        selectionRectangleClientRect
      ));

      resetSelectionRectangleProperties();
      return;
    }

    setSelectedElementUuids(DesignerUtils.getSelectedElementUuids(
      [...focusedComponentGroup.getChildren(), ...currentlySelectedElementsNodes],
      selectionRectangleClientRect
    ));
    resetSelectionRectangleProperties();
  }

  private static handleMouseUpFormCreatorTool(
    event: Konva.KonvaEventObject<MouseEvent>,
    workspaceDto: WorkspaceDesignerDto,
    saveComponentMutation: UseMutateFunction<ComponentDto, unknown, SaveComponentMutation, WorkspaceDesignerDto> | null,
    saveShapeMutation: UseMutateFunction<ShapeDto, unknown, SaveShapeMutation, WorkspaceDesignerDto>
  ) {
    const createFormPreviewProperties = useStore.getState().createFormPreviewProperties;
    const resetCreateFormPreviewProperties = useStore.getState().resetCreateFormPreviewProperties;

    if (createFormPreviewProperties.toolUsed === null) {
      return;
    }

    const activeViewUuid = useStore.getState().activeDesignerViewUuid;

    if (activeViewUuid === null) {
      return;
    }

    if (createFormPreviewProperties.toolUsed === ToolType.TEXT_FORM_CREATOR) {
      if (createFormPreviewProperties.height !== 0 || createFormPreviewProperties.width !== 0) {
        resetCreateFormPreviewProperties();
        return;
      }

      const pointerPosition = event.target.getStage().getPointerPosition();

      useStore.getState().setTextEditorState({
        active: true,
        position: {
          x: pointerPosition.x + spacing.toolbar.width + spacing.toolbox.width,
          y: pointerPosition.y + spacing.navigation.height
        },
        shape: null
      });

      resetCreateFormPreviewProperties()
      return;
    }

    // TODO: put these values into a .json
    if (Math.abs(createFormPreviewProperties.height) < 1 || Math.abs(createFormPreviewProperties.width) < 1) {
      resetCreateFormPreviewProperties();
      return;
    }

    const properties = {
      height: createFormPreviewProperties.height,
      width: createFormPreviewProperties.width,
      borderEnabled: false,
      borderColor: 'rgba(0, 0, 0, 0)',
      borderWidth: 0,
      backgroundColorEnabled: true,
      backgroundColor: colors.secondary.main,
      shadowEnabled: false,
      shadowColor: 'rgba(0, 0, 0, 0)',
      shadowX: 0,
      shadowY: 0,
      shadowBlur: 0,
      opacity: 1,
      visible: true
    };

    if (createFormPreviewProperties.toolUsed === ToolType.RECTANGLE_FORM_CREATOR) {
      (properties as RectanglePropertiesDto).borderRadius = [0, 0, 0, 0];
    }

    const shapeType = DesignerUtils.mapToShapeType(createFormPreviewProperties.toolUsed);
    const focusedComponentUuid = useStore.getState().focusedComponentUuid;

    const activeView = workspaceDto.views.find(view => view.uuid === activeViewUuid);

    if (activeView === undefined) {
      return;
    }

    if (focusedComponentUuid !== null) {
      const {node, pathCoordinates} = AppUtils.findInTrees<ComponentDto>(activeView.components, focusedComponentUuid);

      if (node === undefined) {
        return;
      }

      saveShapeMutation({
        componentUuid: node.uuid,
        createShapeDto: {
          positionX: createFormPreviewProperties.positionX - activeView.positionX - (pathCoordinates.x + node.positionX),
          positionY: createFormPreviewProperties.positionY - activeView.positionY - (pathCoordinates.y + node.positionY),
          type: shapeType,
          properties
        }
      });
      resetCreateFormPreviewProperties();
      return;
    }

    if (saveComponentMutation !== null) {
      saveComponentMutation({
        viewUuid: activeView.uuid,
        createComponentDto: {
          positionX: createFormPreviewProperties.positionX - activeView.positionX,
          positionY: createFormPreviewProperties.positionY - activeView.positionY,
          shapes: [{
            positionX: 0,
            positionY: 0,
            type: shapeType,
            properties
          }]
        }
      });
      resetCreateFormPreviewProperties();
    }
  }

  static handleDragStart(
    event: Konva.KonvaEventObject<DragEvent>
  ) {
    const textEditorState = useStore.getState().textEditorState;
    const resetTextEditorState = useStore.getState().resetTextEditorState;

    if (textEditorState.active) {
      resetTextEditorState();
    }
  }
}
