import { Group } from 'react-konva';
import { BlueprintElementDto, ElementDto, ElementType, ShapeDto, ShapeType, ViewDto } from '../../../gen/api-client';
import React from 'react';
import { DesignerViewEventService } from './services/designer-view-event.service';
import HelpIcon from '@material-ui/icons/Help';
import TitleIcon from '@material-ui/icons/Title';
import { useStore } from '../../store';
import Rectangle from './components/rectangle/rectangle';
import Circle from './components/circle/circle';
import Text from './components/text/text';
import { DesignerStageGroupName } from './enums/designer-stage-group-name.enum';
import { StageMode } from '../../enums/stage-mode.enum';

export abstract class DesignerStageUtils {

  static buildShapes(
    shapeDtos: ShapeDto[],
    shapesAreInFocus: boolean,
    borderScale = 1,
    hideTextShapesOnEdit = true,
    pathCoordinates: { x: number, y: number } = { x: 0, y: 0 },
    disableSelection = false,
    stageMode: StageMode.DESIGNER | StageMode.DESIGNER_BLUEPRINT_COMPONENT
  ): {shapes: JSX.Element[], selectedShapes: JSX.Element[] } {
    const selectedElementUuids = useStore.getState().selectedComponentUuids;

    const shapes: JSX.Element[] = [];
    const selectedShapes: JSX.Element[] = [];

    for (let i = shapeDtos.length - 1; i >= 0; i--) {
      let shapeDto = shapeDtos[i];
      shapeDto = {
        ...shapeDto,
        properties: {
          ...shapeDto.properties,
          opacity: (shapesAreInFocus)
            ? shapeDto.properties.opacity
            : shapeDto.properties.opacity * 0.5
        }
      };

      let shape = null;

      switch (shapeDto.type) {
        case ShapeType.Rectangle:
          shape = <Rectangle
            key={shapeDto.uuid}
            shapeDto={shapeDto}
            borderScale={borderScale}
          />
          break;
        case ShapeType.Circle:
          shape = <Circle
            key={shapeDto.uuid}
            shapeDto={shapeDto}
            borderScale={borderScale}
          />;
          break;
        case ShapeType.Text:
          shape = <Text
            key={shapeDto.uuid}
            shapeDto={shapeDto}
            hideOnEdit={hideTextShapesOnEdit}
            onDoubleClick={event => DesignerViewEventService.handleDoubleClickText(
              event,
              shapeDto,
              pathCoordinates,
              stageMode
            )}
          />;
          break;
        default: continue;
      }

      if (!disableSelection && selectedElementUuids.includes(shapeDto.uuid)) {
        selectedShapes.push(shape);
        continue;
      }
      shapes.push(shape);
    }

    return {shapes, selectedShapes};
  }

  static buildTextShapes(shapeDtos: ShapeDto[]): JSX.Element[] {
    const shapes = [];

    for (const shapeDto of shapeDtos) {
      if (shapeDto.type !== ShapeType.Text) {
        continue;
      }

      shapes.push(<Text key={shapeDto.uuid} shapeDto={shapeDto}/>);
    }

    return shapes;
  }

  static buildShape(
    shapeDto: ShapeDto,
    shapesAreInFocus: boolean,
    borderScale = 1,
    hideTextShapesOnEdit = true,
    pathCoordinates: { x: number, y: number } = { x: 0, y: 0 },
    stageMode: StageMode.DESIGNER | StageMode.DESIGNER_BLUEPRINT_COMPONENT
  ): { shape: JSX.Element, isInSelection: boolean } {
    const selectedElementUuids = useStore.getState().selectedComponentUuids;

    shapeDto = {
      ...shapeDto,
      properties: {
        ...shapeDto.properties,
        opacity: (shapesAreInFocus)
          ? shapeDto.properties.opacity
          : shapeDto.properties.opacity * 0.5
      }
    };

    let shape = null;

    switch (shapeDto.type) {
      case ShapeType.Rectangle:
        shape = <Rectangle
          key={shapeDto.uuid}
          shapeDto={shapeDto}
          borderScale={borderScale}
        />
        break;
      case ShapeType.Circle:
        shape = <Circle
          key={shapeDto.uuid}
          shapeDto={shapeDto}
          borderScale={borderScale}
        />;
        break;
      case ShapeType.Text:
        shape = <Text
          key={shapeDto.uuid}
          shapeDto={shapeDto}
          hideOnEdit={hideTextShapesOnEdit}
          onDoubleClick={event => DesignerViewEventService.handleDoubleClickText(
            event,
            shapeDto,
            pathCoordinates,
            stageMode
          )}
        />;
        break;
    }

    return { shape, isInSelection: selectedElementUuids.includes(shapeDto.uuid) };
  }

  static buildComponentTrees<T extends {
    uuid: string,
    positionX: number,
    positionY: number,
    elements: (ElementDto | BlueprintElementDto)[]
  }>(
    components: T[],
    shapesBorderScale = 1,
    viewDto: ViewDto | null = null,
    hideTextShapesOnEdit = true,
    stageMode: StageMode.DESIGNER | StageMode.DESIGNER_BLUEPRINT_COMPONENT = StageMode.DESIGNER
  ): { components: JSX.Element[], selectedComponents: JSX.Element[] } {

    const componentTrees: JSX.Element[] = [];
    const selectedComponentTrees: JSX.Element[] = [];

    for (let i = components.length - 1; i >= 0; i--) {
      const currentSelectedComponentTrees: JSX.Element[] = [];

      const componentTree = DesignerStageUtils.buildComponentTree<T>(
        components[i],
        shapesBorderScale,
        false,
        viewDto,
        currentSelectedComponentTrees,
        hideTextShapesOnEdit,
        stageMode
      );

      selectedComponentTrees.push(...currentSelectedComponentTrees);

      if (componentTree === undefined) {
        continue;
      }

      componentTrees.push(componentTree);
    }

    return {
      components: componentTrees,
      selectedComponents: selectedComponentTrees
    };
  }

  static buildComponentTree<T extends {
    uuid: string,
    positionX: number,
    positionY: number,
    elements: (ElementDto | BlueprintElementDto)[]
  }>(
    component: T,
    shapesBorderScale = 1,
    componentIsInFocus = false,
    viewDto: ViewDto | null = null,
    selectedComponentTrees: JSX.Element[] | null = null,
    hideTextShapesOnEdit = true,
    stageMode: StageMode.DESIGNER | StageMode.DESIGNER_BLUEPRINT_COMPONENT = StageMode.DESIGNER,
    componentIsInSelectionTree = false,
    parentComponents: {uuid: string, isFocused: boolean}[] = [],
    pathCoordinates: {x: number, y: number} = {x: 0, y: 0}
  ): JSX.Element | undefined {
    const selectedElementUuids = useStore.getState().selectedComponentUuids;
    const activeViewUuid = useStore.getState().activeDesignerViewUuid;
    const focusedComponentUuid = useStore.getState().focusedComponentUuid;

    const isASelectionTreeRoot = selectedComponentTrees !== null &&
      !componentIsInSelectionTree &&
      selectedElementUuids.includes(component.uuid);

    const isInSelectionTree = componentIsInSelectionTree || isASelectionTreeRoot;

    const isFocused = focusedComponentUuid === component.uuid;
    const isInFocus = componentIsInFocus || focusedComponentUuid === null || isFocused;

    const currentPathCoordinates = {
      x: pathCoordinates.x + component.positionX,
      y: pathCoordinates.y + component.positionY
    };

    const childrenSubtrees: JSX.Element[] = [];
    const selectedShapes: JSX.Element[] = [];

    for (const element of component.elements) {
      if (element.type === ElementType.Component) {
        childrenSubtrees.unshift(
          DesignerStageUtils.buildComponentTree<T>(
            element.element as unknown as T,
            shapesBorderScale,
            isInFocus,
            viewDto,
            selectedComponentTrees,
            hideTextShapesOnEdit,
            stageMode,
            isInSelectionTree,
            [{ uuid: element.element.uuid, isFocused }, ...parentComponents],
            currentPathCoordinates
          )
        );
        continue;
      }
      const { shape, isInSelection } = DesignerStageUtils.buildShape(
        element.element as ShapeDto,
        isInFocus,
        shapesBorderScale,
        hideTextShapesOnEdit,
        {
          x: ((viewDto === null) ? 0 : viewDto.positionX) + currentPathCoordinates.x,
          y: ((viewDto === null) ? 0 : viewDto.positionY) + currentPathCoordinates.y
        },
        stageMode
      );

      if (selectedComponentTrees !== null && isInSelection) {
        selectedShapes.push(shape);
        continue;
      }

      childrenSubtrees.unshift(shape);
    }

    const componentPosition = (isASelectionTreeRoot)
      ? currentPathCoordinates
      : {x: component.positionX, y: component.positionY};

    const componentGroup = <Group
      key={component.uuid}
      id={component.uuid}
      name={(parentComponents.length === 0) ? DesignerStageGroupName.COMPONENT_ROOT : undefined}
      // selection trees must be positioned relative to the view, since they are extracted out
      // of their component tree where every component is positioned relative to its parent
      x={componentPosition.x}
      y={componentPosition.y}
      onDblClick={
        (viewDto === null || viewDto.uuid === activeViewUuid)
          ? event => DesignerViewEventService.handleDoubleClick(
            event,
            viewDto,
            component,
            pathCoordinates,
            parentComponents,
            stageMode
          )
          : undefined
      }
    >
      {childrenSubtrees}
    </Group>;

    if (selectedShapes.length !== 0) {
      const identifier = component.uuid + '-shapes';
      selectedComponentTrees.push(<Group
        key={identifier}
        id={identifier}
        name={DesignerStageGroupName.SHAPES_SELECTED}
        x={currentPathCoordinates.x}
        y={currentPathCoordinates.y}
      >
        {selectedShapes}
      </Group>)
    }

    if (!isASelectionTreeRoot) {
      return componentGroup;
    }

    if (selectedComponentTrees !== null) {
      selectedComponentTrees.push(componentGroup);
    }
  }

  static buildShapeIcon(shapeDto: ShapeDto): JSX.Element {
    switch (shapeDto.type) {
      case ShapeType.Rectangle:
        return <div style={{
          backgroundColor: shapeDto.properties.backgroundColor,
          height: 15,
          width: 15
        }} />;
      case ShapeType.Circle:
        return <div style={{
          backgroundColor: shapeDto.properties.backgroundColor,
          borderRadius: 15,
          height: 15,
          width: 15
        }} />;
      case ShapeType.Text:
        return <TitleIcon style={{color: shapeDto.properties.backgroundColor}} fontSize={'small'} />;
      default:
        return <HelpIcon color={'primary'} fontSize={'small'} />;
    }
  }

}
