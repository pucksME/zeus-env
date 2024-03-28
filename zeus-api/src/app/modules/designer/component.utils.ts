import { Component } from './entities/component.entity';
import { ComponentDto } from './dtos/component.dto';
import { UpdatedComponentPositionDto } from './dtos/updated-component-position.dto';
import { ShapeType } from './enums/shape-type.enum';
import { InternalServerErrorException } from '@nestjs/common';
import { ComponentProperties } from './interfaces/component-properties.interface';
import { Shape } from './entities/shape.entity';
import { ShapeDto } from './dtos/shape.dto';
import { TextProperties } from './interfaces/shape-properties/text-properties.interface';
import { AppUtils } from '../../app.utils';
import { ShapeMutation } from './entities/shape-mutation.entity';
import { ShapeMutationUtils } from './shape-mutation.utils';
import { ScalableShape } from './interfaces/scalable-shape.interface';
import { ShapeMutationsDictKeyType } from './enums/shape-mutations-dict-key-type.enum';
import { BlueprintComponentUtils } from './blueprint-component.utils';
import { BlueprintComponent } from './entities/blueprint-component.entity';
import { ComponentDataService } from './data/component-data/component-data.service';
import { BlueprintComponentDataService } from './data/blueprint-component-data/blueprint-component-data.service';
import { ComponentMutation } from './entities/component-mutation.entity';
import { ComponentMutationUtils } from './component-mutation.utils';
import { ComponentMutationDataService } from './data/component-mutation-data/component-mutation-data.service';
import { Alignment } from './enums/alignment.enum';
import { AlignElementsDto } from './dtos/align-elements.dto';
import { ElementsPropertiesDto } from './dtos/elements-properties.dto';
import { SpecificShapeProperties } from './types/specific-shape-properties.type';
import { ElementDto } from './dtos/element.dto';
import { ElementType } from './enums/element-type.enum';

export abstract class ComponentUtils {

  static buildElementDtos(
    components: Component[],
    shapes: Shape[],
  ): ElementDto[] {
    return AppUtils.sortElementDtos<ElementDto>([
      ...components.map(component => ({
        element: ComponentUtils.buildComponentDto(component, ComponentUtils.buildComponentName(component)),
        type: ElementType.COMPONENT
      })),
      ...shapes.map(shape => ({
        element: ComponentUtils.buildShapeDto(
          shape,
          ComponentUtils.buildShapeName(shape)
        ),
        type: ElementType.SHAPE
      }))
    ]);
  }

  static buildComponentDto(componentEntity: Component, defaultName: string = null): ComponentDto {
    return (componentEntity.blueprintComponent !== null)
      ? BlueprintComponentUtils.buildComponentDto(
        componentEntity,
        componentEntity.blueprintComponent,
        ComponentMutationUtils.buildComponentMutationMap(componentEntity.componentMutations),
        ShapeMutationUtils.buildShapeMutationsMap(
          componentEntity.shapeMutations,
          ShapeMutationsDictKeyType.SHAPE_UUIDS
        )
      )
      : {
        uuid: componentEntity.uuid,
        name: componentEntity.name === null && defaultName !== null ? defaultName : componentEntity.name,
        positionX: componentEntity.positionX,
        positionY: componentEntity.positionY,
        sorting: componentEntity.sorting,
        isBlueprintComponentInstance: false,
        elements: ComponentUtils.buildElementDtos(
          (!componentEntity.children) ? [] : componentEntity.children,
          componentEntity.shapes
        )
    };
  }



  static buildShapeDtos(
    shapeEntities: Shape[],
    shapeMutationsMap: Map<string, ShapeMutation> = new Map<string, ShapeMutation>()
  ): ShapeDto[] {

    return shapeEntities
      ? AppUtils.sort<Shape>(shapeEntities)
        .map(shape => ComponentUtils.buildShapeDto(
          shape,
          ComponentUtils.buildShapeName(shape),
          shapeMutationsMap.get(shape.uuid)
        ))
      : null
  }

  static buildShapeDto(
    shapeEntity: Shape, defaultName: string = null,
    shapeMutationsEntity: ShapeMutation | undefined = undefined
  ): ShapeDto {
    return {
      uuid: (shapeMutationsEntity !== undefined) ? shapeMutationsEntity.uuid : shapeEntity.uuid,
      name: (shapeEntity.name === null && defaultName !== null) ? defaultName : shapeEntity.name,
      positionX: (shapeMutationsEntity !== undefined && shapeMutationsEntity.positionX !== null)
        ? shapeMutationsEntity.positionX
        : shapeEntity.positionX,
      positionY: (shapeMutationsEntity !== undefined && shapeMutationsEntity.positionY !== null)
        ? shapeMutationsEntity.positionY
        : shapeEntity.positionY,
      sorting: shapeEntity.sorting,
      type: shapeEntity.type,
      properties: (shapeMutationsEntity !== undefined && shapeMutationsEntity.properties !== null)
        ? {...shapeEntity.properties, ...shapeMutationsEntity.properties}
        : shapeEntity.properties,
      isMutated: shapeMutationsEntity !== undefined
    };
  }

  static buildComponentName(componentEntity: Component): string {
    if (componentEntity.name !== null) {
      return componentEntity.name;
    }

    if (componentEntity.blueprintComponent !== null) {
      return BlueprintComponentUtils.buildBlueprintComponentName(componentEntity.blueprintComponent);
    }

    if (!componentEntity.children ||
      componentEntity.children.length !== 0 ||
      !componentEntity.shapes ||
      componentEntity.shapes.length !== 1) {
      return 'Component';
    }

    return ComponentUtils.buildShapeName(componentEntity.shapes[0]);
  }

  static buildShapeName(shapeEntity: Shape): string {
    switch (shapeEntity.type) {
      case ShapeType.RECTANGLE:
        return 'Rectangle';
      case ShapeType.CIRCLE:
        return 'Circle';
      case ShapeType.TEXT:
        return AppUtils.buildPreviewText((shapeEntity.properties as TextProperties).text);
      default:
        return 'Unknown Shape';
    }
  }

  static buildUpdatedComponentPositionDto(componentEntity: Component): UpdatedComponentPositionDto {
    return {
      uuid: componentEntity.uuid,
      positionX: componentEntity.positionX,
      positionY: componentEntity.positionY
    };
  }

  static fixNegativeDimensions(component: Component) {
    if (!component.shapes || component.shapes.length !== 1) {
      throw new InternalServerErrorException('Could not fix negative dimensions: component is required to have exactly one shape');
    }

    if ((component.shapes[0].properties).height < 0) {
      (component.shapes[0].properties).height *= -1;
      component.positionY -= (component.shapes[0].properties).height;
    }

    if ((component.shapes[0].properties).width < 0) {
      (component.shapes[0].properties).width *= -1;
      component.positionX -= (component.shapes[0].properties).width;
    }

    return component;
  }

  static scaleComponents(
    componentEntities: Component[],
    transformOrigin: { x: number, y: number },
    scaling: { x: number, y: number }
  ): Component[] {
    return componentEntities.map(component => ComponentUtils.scaleComponent(
      component,
      ComponentMutationUtils.buildComponentInstancesComponentMutationsMap(
        componentEntities
          .flatMap(component => ComponentMutationUtils.getComponentMutations(component))
          .flatMap(componentMutations => componentMutations)
      ),
      ShapeMutationUtils.buildComponentInstancesShapeMutationsMap(
        componentEntities
          .flatMap(component => ShapeMutationUtils.getShapeMutations(component))
          .flatMap(shapeMutations => shapeMutations)
      ),
      transformOrigin,
      scaling
    ));
  }

  static scaleComponent(
    component: Component,
    componentInstancesComponentMutationsMap: Map<string, Map<string, ComponentMutation>>,
    componentInstancesShapeMutationsMap: Map<string, Map<string, ShapeMutation>>,
    transformOrigin: {x: number, y: number},
    scaling: {x: number, y: number},
  ): Component {
    component = ComponentUtils.scaleComponentPosition<Component>(component, transformOrigin, scaling);
    component = ComponentUtils.scaleComponentShapes(component, transformOrigin, scaling);

    component.children = component.children.map(child => ComponentUtils.scaleComponent(
      child,
      componentInstancesComponentMutationsMap,
      componentInstancesShapeMutationsMap,
      { x: 0, y: 0 },
      scaling,
    ));

    if (component.blueprintComponent === null) {
      return component;
    }

    component.blueprintComponent = BlueprintComponentUtils.scaleBlueprintComponentInstance(
      component.blueprintComponent,
      componentInstancesComponentMutationsMap.get(component.uuid),
      componentInstancesShapeMutationsMap.get(component.uuid),
      transformOrigin,
      scaling
    );

    return component;
  }

  static scaleComponentPosition<T extends {positionX: number, positionY: number}>(
    component: T,
    transformOrigin: { x: number, y: number },
    scaling: { x: number, y: number }
  ): T {
    component.positionX = ((component.positionX - transformOrigin.x) * scaling.x) + transformOrigin.x;
    component.positionY = ((component.positionY - transformOrigin.y) * scaling.y) + transformOrigin.y;
    return component;
  }

  static scaleComponentShapes<T extends Component | BlueprintComponent>(
    componentEntity: T,
    transformOrigin: {x: number, y: number},
    scaling: {x: number, y: number}
  ): T {
    for (const shape of componentEntity.shapes) {
      this.scaleScalableShape<Shape>(
        shape,
        scaling,
        shape.type !== ShapeType.TEXT
      );
    }
    return componentEntity;
  }

  static scaleScalableShape<T extends ScalableShape>(
    scalableShapeEntity: T,
    scaling: {x: number, y: number},
    dimensionsScalable: boolean
  ): T {
    scalableShapeEntity.positionX *= scaling.x;
    scalableShapeEntity.positionY *= scaling.y;

    if (!dimensionsScalable) {
      return scalableShapeEntity;
    }

    scalableShapeEntity.properties.height *= scaling.y;
    scalableShapeEntity.properties.width *= scaling.x;

    return scalableShapeEntity;
  }

  static calculateMinPosition(componentEntities: Component[]): { x: number, y: number } {
    if (componentEntities.length === 0) {
      throw new InternalServerErrorException('Could not calculate min position: no components were provided');
    }

    const minPosition = { x: Number.MAX_VALUE, y: Number.MAX_VALUE };
    componentEntities.forEach(component => component.shapes.forEach(shape => {
      const shapeX = component.positionX + shape.positionX;
      const shapeY = component.positionY + shape.positionY;

      if (shapeX < minPosition.x) {
        minPosition.x = shapeX;
      }

      if (shapeY < minPosition.y) {
        minPosition.y = shapeY;
      }
    }));

    return minPosition;
  }

  static calculateOrigin(componentEntity: Component): { x: number, y: number } {
    const origin = { x: Number.MAX_VALUE, y: Number.MAX_VALUE };

    if (componentEntity.shapes.length === 0) {
      throw new InternalServerErrorException('Could not calculate origin: component had no shapes');
    }

    componentEntity.shapes.forEach(shape => {
      const shapeX = componentEntity.positionX + shape.positionX;
      const shapeY = componentEntity.positionY + shape.positionY;

      if (shapeX < origin.x) {
        origin.x = shapeX;
      }

      if (shapeY < origin.y) {
        origin.y = shapeY;
      }
    });

    return origin;
  }

  static calculateProperties(componentEntities: Component[]): ComponentProperties {
    const minPosition = { x: Number.MAX_VALUE, y: Number.MAX_VALUE };
    const maxPosition = { x: Number.MIN_VALUE, y: Number.MIN_VALUE };

    if (componentEntities.length === 0) {
      throw new InternalServerErrorException('Could not calculate properties: no components were provided');
    }

    componentEntities.forEach(component =>
      ComponentUtils.getAllShapes(component).forEach(shape => {
        const shapeXStart = component.positionX + shape.positionX;
        const shapeYStart = component.positionY + shape.positionY;
        const shapeXEnd = component.positionX + shape.positionX + shape.properties.width;
        const shapeYEnd = component.positionY + shape.positionY + shape.properties.height;

        if (shapeXStart < minPosition.x) {
          minPosition.x = shapeXStart;
        }

        if (shapeYStart < minPosition.y) {
          minPosition.y = shapeYStart;
        }

        if (shapeXEnd > maxPosition.x) {
          maxPosition.x = shapeXEnd;
        }

        if (shapeYEnd > maxPosition.y) {
          maxPosition.y = shapeYEnd;
        }
      }));

    return {
      height: maxPosition.y - minPosition.y,
      width: maxPosition.x - minPosition.x,
      minX: minPosition.x,
      maxX: maxPosition.x,
      minY: minPosition.y,
      maxY: maxPosition.y
    };
  }

  static calculateComponentProperties(componentEntity: Component): ComponentProperties {
    const minPosition = { x: Number.MAX_VALUE, y: Number.MAX_VALUE };
    const maxPosition = { x: Number.MIN_VALUE, y: Number.MIN_VALUE };

    if (componentEntity.shapes.length === 0 && componentEntity.blueprintComponent !== null && componentEntity.blueprintComponent.shapes.length === 0) {
      throw new InternalServerErrorException('Could not calculate dimensions: component had no shapes');
    }

    ComponentUtils.getAllShapes(componentEntity).forEach(shape => {
      const shapeXStart = componentEntity.positionX + shape.positionX;
      const shapeYStart = componentEntity.positionY + shape.positionY;

      const shapeXEnd = componentEntity.positionX + shape.positionX + shape.properties.width;
      const shapeYEnd = componentEntity.positionY + shape.positionY + shape.properties.height;

      if (shapeXStart < minPosition.x) {
        minPosition.x = shapeXStart;
      }

      if (shapeYStart < minPosition.y) {
        minPosition.y = shapeYStart;
      }

      if (shapeXEnd > maxPosition.x) {
        maxPosition.x = shapeXEnd;
      }

      if (shapeYEnd > maxPosition.y) {
        maxPosition.y = shapeYEnd;
      }
    });

    return {
      height: maxPosition.y - minPosition.y,
      width: maxPosition.x - minPosition.x,
      minX: minPosition.x,
      maxX: maxPosition.x,
      minY: minPosition.y,
      maxY: maxPosition.y
    };
  }

  static getAllShapes(componentEntity: Component, applyShapeMutations = true): Shape[] {
    const shapes = [...componentEntity.shapes];

    if (componentEntity.blueprintComponent === null) {
      return shapes;
    }

    const blueprintComponentShapes = [...componentEntity.blueprintComponent.shapes];

    if (applyShapeMutations) {
      const shapeMutationsMap = ShapeMutationUtils.buildShapeMutationsMap(
        componentEntity.shapeMutations, ShapeMutationsDictKeyType.SHAPE_UUIDS
      );

      for (const shape of blueprintComponentShapes) {
        const shapeMutations = shapeMutationsMap.get(shape.uuid);
        if (shapeMutations === undefined) {
          continue;
        }
        shape.positionX = shapeMutations.positionX;
        shape.positionY = shapeMutations.positionY;
        shape.properties = {...shape.properties, ...shapeMutations.properties};
      }
    }

    shapes.push(...blueprintComponentShapes);
    return shapes;
  }

  static injectBlueprintComponents(
    componentEntities: Component[],
    blueprintComponentEntities: BlueprintComponent[]
  ): Component[] {
    const blueprintComponentsMap = new Map<string, BlueprintComponent>();

    for (const blueprintComponentTree of blueprintComponentEntities) {
      blueprintComponentsMap.set(blueprintComponentTree.uuid, blueprintComponentTree);
    }

    const componentOperation = (componentEntity: Component): Component => {
      if (componentEntity.blueprintComponent === undefined) {
        throw new InternalServerErrorException('Could not inject blueprint components: blueprint component was not injected');
      }

      if (componentEntity.blueprintComponent === null) {
        return componentEntity;
      }

      const blueprintComponent = blueprintComponentsMap.get(componentEntity.blueprintComponent.uuid);

      if (blueprintComponent === undefined) {
        componentEntity.blueprintComponent = null;
        return componentEntity;
      }

      componentEntity.blueprintComponent = blueprintComponent;
      return componentEntity;
    };

    return componentEntities.map(
      componentEntity => ComponentUtils.traverseComponentTree(componentEntity, componentOperation)
    );
  }

  static traverseComponentTree(
    componentEntity: Component,
    operation: (currentComponent: Component) => Component
  ): Component {
    componentEntity = operation(componentEntity);
    componentEntity.children = componentEntity.children.map(
      component => ComponentUtils.traverseComponentTree(component, operation)
    );
    return componentEntity;
  }

  static async findDirectChildElements(
    parentComponentUuid: string | null,
    elementUuids: string[],
    componentDataService: ComponentDataService,
    blueprintComponentDataService: BlueprintComponentDataService,
    componentMutationDataService: ComponentMutationDataService,
    options: {
      componentRelations?: string[] | undefined,
      blueprintComponentRelations?: string[] | undefined,
      injectBlueprintComponents?: boolean | undefined
    } | null = null
  ): Promise<{
    components: Component[],
    shapes: Shape[],
    shapeMutations: ShapeMutation[],
    componentMutations: ComponentMutation[],
    parentComponent: Component | null,
    foundAllElements: boolean
  }> {
    if (options === null) {
      options = {
        componentRelations: [],
        blueprintComponentRelations: [],
        injectBlueprintComponents: false
      };
    }

    const componentRelations = (options.componentRelations === undefined)
      ? []
      : options.componentRelations;

    const blueprintComponents = (!options.injectBlueprintComponents)
      ? []
      : await blueprintComponentDataService.findTrees(
        (options.blueprintComponentRelations === undefined)
          ? []
          : options.blueprintComponentRelations
      );

    if (parentComponentUuid === null) {
      const components = (await componentDataService.findTrees(componentRelations)).filter(
        component => elementUuids.includes(component.uuid)
      );

      return {
        components: (!options.injectBlueprintComponents)
          ? components
          : ComponentUtils.injectBlueprintComponents(components, blueprintComponents),
        shapes: [],
        shapeMutations: [],
        componentMutations: [],
        parentComponent: null,
        foundAllElements: components.length === elementUuids.length
      };
    }

    let parentComponent = await componentDataService.find(parentComponentUuid, componentRelations);
    let parentComponentReferencesBlueprintComponent = false;

    if (parentComponent === undefined) {
      parentComponent = (await componentMutationDataService.find(
        parentComponentUuid,
        ['component', ...componentRelations.map(relation => 'component.' + relation)]
      )).component;
      parentComponentReferencesBlueprintComponent = true;
    }

    if (parentComponent === undefined) {
      return {
        components: [],
        shapes: [],
        shapeMutations: [],
        componentMutations: [],
        parentComponent: null,
        foundAllElements: false
      };
    }

    if (parentComponent.blueprintComponent === undefined) {
      throw new InternalServerErrorException('Could not find direct child elements: blueprint component relation was not loaded');
    }

    if (parentComponent.blueprintComponent !== null) {
      parentComponentReferencesBlueprintComponent = true;
    }

    if (!parentComponentReferencesBlueprintComponent) {
      parentComponent = await componentDataService.findDescendants(parentComponent, componentRelations);
    }

    if (options.injectBlueprintComponents) {
      if (!parentComponentReferencesBlueprintComponent) {
        parentComponent.children = ComponentUtils.injectBlueprintComponents(
          parentComponent.children,
          blueprintComponents
        );
      }

      if (parentComponent.blueprintComponent) {
        parentComponent.blueprintComponent = blueprintComponents.find(
          blueprintComponent => blueprintComponent.uuid === parentComponent.blueprintComponent.uuid
        );
      }
    }

    const components = (parentComponentReferencesBlueprintComponent)
      ? []
      : parentComponent.children.filter(component => elementUuids.includes(component.uuid));

    const shapes = (parentComponentReferencesBlueprintComponent || parentComponent.shapes === undefined)
      ? []
      : parentComponent.shapes.filter(shape => elementUuids.includes(shape.uuid));

    if ((components.length + shapes.length) === elementUuids.length) {
      return {
        components,
        shapes,
        shapeMutations: [],
        componentMutations: [],
        parentComponent,
        foundAllElements: true
      };
    }

    const rootComponent = (parentComponentReferencesBlueprintComponent)
      ? parentComponent
      : await componentDataService.findRoot(parentComponent, componentRelations);

    const shapeMutations = (rootComponent === undefined || rootComponent.shapeMutations === undefined)
      ? []
      : rootComponent.shapeMutations.filter(shapeMutations => elementUuids.includes(shapeMutations.uuid));

    if ((components.length + shapes.length + shapeMutations.length) === elementUuids.length) {
      return {
        components,
        shapes,
        shapeMutations,
        componentMutations: [],
        parentComponent,
        foundAllElements: true
      };
    }

    const componentMutations = (rootComponent === undefined || rootComponent.componentMutations === undefined)
      ? []
      : rootComponent.componentMutations.filter(componentMutation => elementUuids.includes(componentMutation.uuid));

    if ((components.length + shapes.length + shapeMutations.length + componentMutations.length) === elementUuids.length) {
      return {
        components,
        shapes,
        shapeMutations,
        componentMutations,
        parentComponent,
        foundAllElements: true
      };
    }

    return {
      components,
      shapes,
      shapeMutations,
      componentMutations,
      parentComponent,
      foundAllElements: false
    };
  }

  static calculateElementsOrigin(elements: (Component | Shape)[]): {x: number, y: number} {
    const origin = {x: Number.MAX_VALUE, y: Number.MAX_VALUE};

    for (const element of elements) {
      const elementOrigin = ComponentUtils.calculateElementOrigin(element);

      if (elementOrigin.x < origin.x) {
        origin.x = elementOrigin.x;
      }

      if (elementOrigin.y < origin.y) {
        origin.y = elementOrigin.y;
      }
    }

    return origin;
  }

  static calculateElementOrigin(element: Component | Shape): {x: number, y: number} {
    return (element instanceof Shape)
      ? ComponentUtils.calculateShapeOrigin(element)
      : ComponentUtils.calculateComponentTreeOrigin(element);
  }

  static calculateShapeOrigin(shape: Shape): {x: number, y: number} {
    return {x: shape.positionX, y: shape.positionY};
  }

  static calculateComponentTreeOrigin(
    component: Component,
    pathCoordinates: {x: number, y: number} = {x: 0, y: 0},
    origin: {x: number, y: number} = {x: Number.MAX_VALUE, y: Number.MAX_VALUE}
  ): {x: number, y: number} {

    if (component.shapes === undefined) {
      return origin;
    }

    for (const shape of component.shapes) {
      const shapeX = pathCoordinates.x + component.positionX + shape.positionX;
      const shapeY = pathCoordinates.y + component.positionY + shape.positionY;

      if (shapeX < origin.x) {
        origin.x = shapeX;
      }

      if (shapeY < origin.y) {
        origin.y = shapeY;
      }
    }

    if (component.children === undefined) {
      return origin;
    }

    for (const child of component.children) {
      const childOrigin = ComponentUtils.calculateComponentTreeOrigin(
        child,
        {
          x: pathCoordinates.x + component.positionX,
          y: pathCoordinates.y + component.positionY
        },
        origin
      );
      if (childOrigin.x < origin.x) {
        origin.x = childOrigin.x;
      }

      if (childOrigin.y < origin.y) {
        origin.y = childOrigin.y;
      }
    }

    return origin;
  }

  static getAllShapesAndMutations(component: Component): {
    shapes: Shape[],
    componentMutations: ComponentMutation[],
    shapeMutations: ShapeMutation[]
  } {
    const shapesAndMutations = {
      shapes: [...component.shapes],
      componentMutations: [...component.componentMutations],
      shapeMutations: [...component.shapeMutations]
    };

    for (const child of component.children) {
      const childShapesAndMutations = ComponentUtils.getAllShapesAndMutations(child);
      shapesAndMutations.shapes.push(...childShapesAndMutations.shapes);
      shapesAndMutations.shapeMutations.push(...childShapesAndMutations.shapeMutations);
      shapesAndMutations.componentMutations.push(...childShapesAndMutations.componentMutations);
    }

    return shapesAndMutations;
  }

  static alignElements<T extends {
    uuid: string,
    positionX: number,
    positionY: number,
    properties?: Partial<SpecificShapeProperties>
  }>(
    elements: T[],
    alignElementsDto: AlignElementsDto
  ): T[] {

    const elementsPropertiesMap = new Map<string, ElementsPropertiesDto>();

    for (const element of alignElementsDto.elements) {
      elementsPropertiesMap.set(element.elementUuid, element.elementProperties)
    }

    const elementKeys = (
      alignElementsDto.alignment === Alignment.HORIZONTAL_LEFT ||
      alignElementsDto.alignment === Alignment.HORIZONTAL_CENTER ||
      alignElementsDto.alignment === Alignment.HORIZONTAL_RIGHT
    ) ? { positionKey: 'positionX', propertyKey: 'x', dimensionKey: 'width'}
      : { positionKey: 'positionY', propertyKey: 'y', dimensionKey: 'height'};

    for (const element of elements) {
      const properties = elementsPropertiesMap.get(element.uuid);

      if (properties === undefined) {
        continue;
      }

      if (alignElementsDto.alignment === Alignment.HORIZONTAL_LEFT ||
        alignElementsDto.alignment === Alignment.VERTICAL_TOP) {
        element[elementKeys.positionKey] -=
          properties[elementKeys.propertyKey] - alignElementsDto.elementsProperties[elementKeys.propertyKey];
        continue;
      }

      if (alignElementsDto.alignment === Alignment.HORIZONTAL_CENTER ||
        alignElementsDto.alignment === Alignment.VERTICAL_CENTER) {
        element[elementKeys.positionKey] -=
          properties[elementKeys.propertyKey] - alignElementsDto.elementsProperties[elementKeys.propertyKey] - (
            (alignElementsDto.elementsProperties[elementKeys.dimensionKey] - properties[elementKeys.dimensionKey]) / 2
          );
        continue;
      }

      if (alignElementsDto.alignment === Alignment.HORIZONTAL_RIGHT ||
        alignElementsDto.alignment === Alignment.VERTICAL_BOTTOM) {
        element[elementKeys.positionKey] -=
          (
            properties[elementKeys.propertyKey] +
            properties[elementKeys.dimensionKey]
          ) - (
            alignElementsDto.elementsProperties[elementKeys.propertyKey] +
            alignElementsDto.elementsProperties[elementKeys.dimensionKey]
          );
      }
    }

    return elements;
  }

}
