import { ToolType } from '../../enums/tool-type.enum';
import { ScaleOrigin, ShapeType, ViewDto } from '../../../gen/api-client';
import { RGBColor } from 'react-color';
import { AppUtils } from '../../app.utils';
import { DomElementIds } from '../../../constants';
import { StageDimensions } from './interfaces/stage-dimensions.interface';
import SaveTitleIcon from '@material-ui/icons/Check';
import CancelEditTitleIcon from '@material-ui/icons/Clear';
import { IconButton } from '@material-ui/core';
import React from 'react';
import Konva from 'konva';
import { DesignerStageGroupName } from './enums/designer-stage-group-name.enum';
import { SelectedElementsProperties } from './interfaces/selected-elements-properties.interface';
import { StageProperties } from '../../interfaces/stage-properties.interface';
import { useStore } from '../../store';
import { ComponentTreeNode } from './component.utils';

export abstract class DesignerUtils {

  static isFormCreatorToolType(designerToolType: ToolType): boolean {
    return designerToolType === ToolType.TRIANGLE_FORM_CREATOR ||
      designerToolType === ToolType.RECTANGLE_FORM_CREATOR ||
      designerToolType === ToolType.CIRCLE_FORM_CREATOR ||
      designerToolType === ToolType.TEXT_FORM_CREATOR;
  }

  static mapToScaleOrigin(anchorName: string): ScaleOrigin | null {
    const anchorNameParts = anchorName.split(' ');
    if (anchorNameParts.length !== 2) {
      console.warn('Mapping anchor name to null: anchor name does not have the required format');
      return null;
    }

    switch (anchorNameParts[0]) {
      case 'top-left':
        return ScaleOrigin.TopLeft;
      case 'top-center':
        return ScaleOrigin.Top;
      case 'top-right':
        return ScaleOrigin.TopRight;
      case 'middle-left':
        return ScaleOrigin.Left;
      case 'middle-right':
        return ScaleOrigin.Right;
      case 'bottom-left':
        return ScaleOrigin.BottomLeft;
      case 'bottom-center':
        return ScaleOrigin.Bottom;
      case 'bottom-right':
        return ScaleOrigin.BottomRight;
      default:
        console.warn(`Mapping anchor name to null: anchor name "${anchorNameParts[0]}" is not supported`);
        return null;
    }
  }

  static convertRgbToString(rgbColor: RGBColor): string {
    return `rgba(${rgbColor.r}, ${rgbColor.g}, ${rgbColor.b}, ${rgbColor.a})`;
  }

  static getDocumentStageDimensions(): StageDimensions {
    const stage = document.querySelector(
      AppUtils.buildDomElementIdTagValue(DomElementIds.WORKSPACE_STAGE)
    );

    if (!stage) {
      return { height: 0, width: 0 };
    }

    return {
      height: stage['offsetHeight'],
      width: stage['offsetWidth']
    };
  }

  static mapToShapeType(designerToolType: ToolType): ShapeType {
    switch (designerToolType) {
      case ToolType.RECTANGLE_FORM_CREATOR: return ShapeType.Rectangle;
      case ToolType.CIRCLE_FORM_CREATOR: return ShapeType.Circle;
      default: return ShapeType.Rectangle;
    }
  }

  static buildNameEditingActions(onSaveButtonClick: () => void, onCancelButtonClick: () => void) {
    return <div style={{ paddingRight: 10 }}>
      <IconButton size={'small'} onClick={onCancelButtonClick}>
        <CancelEditTitleIcon fontSize={'small'} />
      </IconButton>
      <IconButton size={'small'} onClick={onSaveButtonClick}>
        <SaveTitleIcon fontSize={'small'} />
      </IconButton>
    </div>
  }

  static getCurrentlySelectedElementsNodes(container: Konva.Container): (Konva.Group | Konva.Shape)[] | null {
    const selectedComponentsGroup = container.findOne<Konva.Group>(
      '.' + DesignerStageGroupName.COMPONENTS_SELECTED
    );

    if (selectedComponentsGroup === undefined) {
      return null;
    }

    return (selectedComponentsGroup.getChildren() as Konva.Group[]).flatMap(child => {
        if (child.name() !== DesignerStageGroupName.SHAPES_SELECTED) {
          return child;
        }
        return child.getChildren();
      });
  }

  static getSelectedElementUuids(
    nodes: Konva.Node[],
    selectionRectangle: { width: number, height: number, x: number, y: number }
  ): string[] {
    const selectedElementUuids: string[] = [];
    for (const node of nodes) {
      if (!Konva.Util.haveIntersection(node.getClientRect(), selectionRectangle)) {
        continue;
      }
      selectedElementUuids.push(node.id());
    }
    return selectedElementUuids;
  }

  static updateSelectedElementsProperties<T extends ComponentTreeNode<T>>(
    stageProperties: StageProperties,
    nodesInTransformer: Konva.Node[],
    components: T[],
    view: ViewDto | null = null
  ) {
    const focusedComponentUuid = useStore.getState().focusedComponentUuid;
    const setSelectedElementsProperties = useStore.getState().setSelectedElementsProperties;
    if (nodesInTransformer.length !== 1) {
      return;
    }

    const nodesInTransformerRect = nodesInTransformer[0].getClientRect({
      skipShadow: true,
      skipStroke: true
    });

    if (nodesInTransformerRect.height === 0 || nodesInTransformerRect.width === 0) {
      return;
    }

    const calculatePositionRelativeToView = (position: { x: number, y: number }) => ({
      x: ((position.x - stageProperties.x) / stageProperties.scale) - ((view !== null) ? view.positionX : 0),
      y: ((position.y - stageProperties.y) / stageProperties.scale) - ((view !== null) ? view.positionY : 0)
    });

    const selectedElementsPosition = calculatePositionRelativeToView({
      x: nodesInTransformerRect.x,
      y: nodesInTransformerRect.y
    });

    const elementsInSelection: {elementUuid: string, height: number, width: number, x: number, y: number}[] = [];

    for (const node of (nodesInTransformer[0] as Konva.Group).getChildren()) {

      if (node.name() !== DesignerStageGroupName.SHAPES_SELECTED) {
        const nodeClientRect = node.getClientRect({skipShadow: true, skipStroke: true});
        elementsInSelection.push({
          elementUuid: node.id(),
          height: nodeClientRect.height / stageProperties.scale,
          width: nodeClientRect.width / stageProperties.scale,
          ...calculatePositionRelativeToView({ x: nodeClientRect.x, y: nodeClientRect.y })
        });
        continue;
      }

      elementsInSelection.push(...(node as Konva.Group).getChildren().map(child => {
        const childClientRect = child.getClientRect({skipShadow: true, skipStroke: true});
        return {
          elementUuid: child.id(),
          height: childClientRect.height / stageProperties.scale,
          width: childClientRect.width / stageProperties.scale,
          ...calculatePositionRelativeToView({ x: childClientRect.x, y: childClientRect.y })
        };
      }));
    }

    const selectedElementsProperties: SelectedElementsProperties = {
      ...selectedElementsPosition,
      positionRelativeToView: selectedElementsPosition,
      height: nodesInTransformerRect.height / stageProperties.scale,
      width: nodesInTransformerRect.width / stageProperties.scale,
      elements: elementsInSelection
    };

    if (focusedComponentUuid === null) {
      setSelectedElementsProperties(selectedElementsProperties);
      return;
    }

    const {node, pathCoordinates} = AppUtils.findInTrees<T>(components, focusedComponentUuid);

    if (node === undefined) {
      return;
    }

    const parentCoordinates = {
      x: pathCoordinates.x + node.positionX,
      y: pathCoordinates.y + node.positionY
    };

    setSelectedElementsProperties({
      ...selectedElementsProperties,
      x: selectedElementsProperties.x - parentCoordinates.x,
      y: selectedElementsProperties.y - parentCoordinates.y,
      elements: elementsInSelection.map(element => ({
        ...element,
        x: element.x - parentCoordinates.x,
        y: element.y - parentCoordinates.y
      }))
    });
  }
}
