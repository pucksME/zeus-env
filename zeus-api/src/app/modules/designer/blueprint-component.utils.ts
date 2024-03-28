import { BlueprintComponent } from './entities/blueprint-component.entity';
import { BlueprintComponentDto } from './dtos/blueprint-component.dto';
import { ComponentUtils } from './component.utils';
import { ComponentDto } from './dtos/component.dto';
import { ShapeMutation } from './entities/shape-mutation.entity';
import { Component } from './entities/component.entity';
import { ComponentMutation } from './entities/component-mutation.entity';
import { ComponentMutationUtils } from './component-mutation.utils';
import { Shape } from './entities/shape.entity';
import { InternalServerErrorException } from '@nestjs/common';
import { ShapeType } from './enums/shape-type.enum';
import { ShapeMutationUtils } from './shape-mutation.utils';
import { BlueprintComponentDataService } from './data/blueprint-component-data/blueprint-component-data.service';
import { BlueprintElementDto } from './dtos/blueprint-element.dto';
import { AppUtils } from '../../app.utils';
import { ElementType } from './enums/element-type.enum';
import { ElementDto } from './dtos/element.dto';

export abstract class BlueprintComponentUtils {

  static buildBlueprintElementDtos(
    blueprintComponents: BlueprintComponent[],
    shapes: Shape[]
  ): BlueprintElementDto[] {
    return AppUtils.sortElementDtos<BlueprintElementDto>([
      ...blueprintComponents.map(blueprintComponent => ({
        element: BlueprintComponentUtils.buildBlueprintComponentDto(blueprintComponent),
        type: ElementType.COMPONENT
      })),
      ...shapes.map(shape => ({
        element: ComponentUtils.buildShapeDto(shape, ComponentUtils.buildShapeName(shape)),
        type: ElementType.SHAPE
      }))
    ]);
  }

  static buildElementDtos(
    blueprintComponents: BlueprintComponent[],
    shapes: Shape[],
    componentMutationsMap: Map<string, ComponentMutation>,
    shapeMutationsMap: Map<string, ShapeMutation>
  ): ElementDto[] {
    return AppUtils.sortElementDtos<ElementDto>([
      ...blueprintComponents.map(blueprintComponent => ({
        element: BlueprintComponentUtils.buildComponentDto(
          null,
          blueprintComponent,
          componentMutationsMap,
          shapeMutationsMap
        ),
        type: ElementType.COMPONENT
      })),
      ...shapes.map(shape => ({
        element: ComponentUtils.buildShapeDto(
          shape,
          ComponentUtils.buildShapeName(shape),
          shapeMutationsMap.get(shape.uuid)),
        type: ElementType.SHAPE
      }))
    ])
  }

  static buildBlueprintComponentDto(blueprintComponentEntity: BlueprintComponent): BlueprintComponentDto {
    return {
      uuid: blueprintComponentEntity.uuid,
      name: BlueprintComponentUtils.buildBlueprintComponentName(blueprintComponentEntity),
      positionX: blueprintComponentEntity.positionX,
      positionY: blueprintComponentEntity.positionY,
      sorting: blueprintComponentEntity.sorting,
      referencingComponentUuids: (blueprintComponentEntity.referencingComponents)
        ? blueprintComponentEntity.referencingComponents.map(componentEntity => componentEntity.uuid)
        : null,
      elements: BlueprintComponentUtils.buildBlueprintElementDtos(
        (!blueprintComponentEntity.children)
          ? []
          : blueprintComponentEntity.children,
        blueprintComponentEntity.shapes)
    };
  }

  static buildBlueprintComponentName(blueprintComponentEntity: { name: string | null }): string {

    if (blueprintComponentEntity.name === null) {
      return 'Blueprint Component';
    }

    return blueprintComponentEntity.name;

  }

  static buildComponentDto(
    component: Component | null,
    blueprintComponent: BlueprintComponent,
    componentMutationsMap: Map<string, ComponentMutation>,
    shapeMutationsMap: Map<string, ShapeMutation>
  ): ComponentDto {

    const position = (component !== null)
      ? { positionX: component.positionX, positionY: component.positionY }
      : ComponentMutationUtils.buildMutatedPosition(
        blueprintComponent, componentMutationsMap.get(blueprintComponent.uuid)
      );

    return {
      uuid: (component === null) ? componentMutationsMap.get(blueprintComponent.uuid).uuid : component.uuid,
      name: (component === null)
        ? BlueprintComponentUtils.buildBlueprintComponentName(blueprintComponent)
        : ComponentUtils.buildComponentName(component),
      positionX: position.positionX,
      positionY: position.positionY,
      sorting: (component === null) ? blueprintComponent.sorting : component.sorting,
      isBlueprintComponentInstance: true,
      elements: BlueprintComponentUtils.buildElementDtos(
        blueprintComponent.children,
        blueprintComponent.shapes,
        componentMutationsMap,
        shapeMutationsMap
      )
    }
  }

  static buildBlueprintComponent(
    component: Component,
    viewComponent: Component,
    rootComponent = component
  ): BlueprintComponent {
    const isRootComponent = component === rootComponent;

    const blueprintComponent = new BlueprintComponent();
    blueprintComponent.name = component.name;
    blueprintComponent.positionX = (isRootComponent) ? 0 : component.positionX;
    blueprintComponent.positionY = (isRootComponent) ? 0 : component.positionY;
    blueprintComponent.sorting = (isRootComponent) ? 0 : component.sorting;
    blueprintComponent.workspace = (rootComponent.view !== null)
      ? rootComponent.view.workspace
      : viewComponent.view.workspace;
    blueprintComponent.shapes = component.shapes;

    for (const shape of blueprintComponent.shapes) {
      shape.component = null;
      shape.blueprintComponent = blueprintComponent;
    }

    component.shapes = [];

    if (!rootComponent.blueprintComponent) {
      rootComponent.blueprintComponent = blueprintComponent;
    }

    if (isRootComponent) {
      rootComponent.componentMutations = [];
      rootComponent.shapeMutations = [];
    }

    blueprintComponent.children = component.children.map(
      child => BlueprintComponentUtils.buildBlueprintComponent(
        child,
        viewComponent,
        rootComponent
      )
    );

    return blueprintComponent;
  }

  static getShapesOfBlueprintComponents(blueprintComponents: BlueprintComponent[]): Shape[] {
    return blueprintComponents.flatMap(
      blueprintComponent => BlueprintComponentUtils.getShapes(blueprintComponent)
    );
  }

  static getShapes(blueprintComponent: BlueprintComponent): Shape[] {
    return [
      ...blueprintComponent.shapes,
      ...blueprintComponent.children.flatMap(child => BlueprintComponentUtils.getShapes(child))
    ];
  }

  static findBlueprintComponents(
    blueprintComponent: BlueprintComponent,
    blueprintComponentUuids: string[]
  ): BlueprintComponent[] {
    return [
      ...((blueprintComponentUuids.includes(blueprintComponent.uuid)) ? [blueprintComponent] : []),
      ...blueprintComponent.children.flatMap(child => BlueprintComponentUtils.findBlueprintComponents(
        child,
        blueprintComponentUuids
      ))
    ];
  }

  static scaleBlueprintComponentInstances(
    blueprintComponents: BlueprintComponent[],
    componentMutationsMap: Map<string, ComponentMutation>,
    shapeMutationsMap: Map<string, ShapeMutation>,
    transformOrigin: { x: number, y: number },
    scaling: { x: number, y: number }
  ): BlueprintComponent[] {
    return blueprintComponents.map(blueprintComponent => BlueprintComponentUtils.scaleBlueprintComponentInstance(
      blueprintComponent,
      componentMutationsMap,
      shapeMutationsMap,
      transformOrigin,
      scaling
    ))
  }

  static scaleBlueprintComponentInstance(
    blueprintComponent: BlueprintComponent,
    componentMutationsMap: Map<string, ComponentMutation> | undefined,
    shapeMutationsMap: Map<string, ShapeMutation>,
    transformOrigin: {x: number, y: number},
    scaling: {x: number, y: number}
  ): BlueprintComponent {

    const componentMutation = (componentMutationsMap === undefined)
      ? undefined
      : componentMutationsMap.get(blueprintComponent.uuid);

    if (componentMutation !== undefined) {
      ComponentMutationUtils.setupComponentMutationPosition(componentMutation);
      ComponentUtils.scaleComponentPosition<ComponentMutation>(componentMutation, transformOrigin, scaling);
    }

    for (const shape of blueprintComponent.shapes) {
      if (!shapeMutationsMap.has(shape.uuid)) {
        throw new InternalServerErrorException('Could not scale the shapes of a blueprint instance: a shape mutation did not exist');
      }

      const dimensionsScalable = shape.type !== ShapeType.TEXT;
      const shapeMutation = shapeMutationsMap.get(shape.uuid);

      if (dimensionsScalable) {
        ShapeMutationUtils.setupShapeMutationDimensions(shapeMutation);
      }

      ShapeMutationUtils.setupShapeMutationPositions(shapeMutation);

      ComponentUtils.scaleScalableShape<ShapeMutation>(
        shapeMutation,
        scaling,
        dimensionsScalable
      );
    }

    blueprintComponent.children = blueprintComponent.children.map(child =>
      BlueprintComponentUtils.scaleBlueprintComponentInstance(
        child,
        componentMutationsMap,
        shapeMutationsMap,
        { x: 0, y: 0 },
        scaling
      ));

    return blueprintComponent;
  }

  static async findDirectChildElements(
    parentBlueprintComponentUuid: string | null,
    elementUuids: string[],
    blueprintComponentDataService: BlueprintComponentDataService,
    options: { blueprintComponentRelations: string[] | undefined } | null = null
  ): Promise<{
    blueprintComponents: BlueprintComponent[],
    shapes: Shape[],
    parentBlueprintComponent: BlueprintComponent | null,
    foundAllElements: boolean
  }> {
    if (options === null) {
      options = { blueprintComponentRelations: [] };
    }

    const blueprintComponentRelations = (options.blueprintComponentRelations === undefined)
      ? []
      : options.blueprintComponentRelations;

    if (parentBlueprintComponentUuid === null) {
      const blueprintComponents = (await blueprintComponentDataService.findTrees(blueprintComponentRelations)).filter(
        blueprintComponent => elementUuids.includes(blueprintComponent.uuid)
      );

      return {
        blueprintComponents,
        shapes: [],
        parentBlueprintComponent: null,
        foundAllElements: true
      };
    }

    let parentBlueprintComponent = await blueprintComponentDataService.find(
      parentBlueprintComponentUuid,
      blueprintComponentRelations
    );

    if (parentBlueprintComponent === undefined) {
      return {
        blueprintComponents: [],
        shapes: [],
        parentBlueprintComponent: null,
        foundAllElements: false
      };
    }

    parentBlueprintComponent = await blueprintComponentDataService.findDescendants(
      parentBlueprintComponent,
      blueprintComponentRelations
    );

    const blueprintComponents = parentBlueprintComponent.children.filter(
      blueprintComponent => elementUuids.includes(blueprintComponent.uuid)
    );

    const shapes = (parentBlueprintComponent.shapes === undefined)
      ? []
      : parentBlueprintComponent.shapes.filter(shape => elementUuids.includes(shape.uuid));

    return {
      blueprintComponents,
      shapes,
      parentBlueprintComponent,
      foundAllElements: (blueprintComponents.length + shapes.length) === elementUuids.length
    };
  }

  static scaleBlueprintComponents(
    blueprintComponents: BlueprintComponent[],
    transformOrigin: { x: number, y: number },
    scaling: { x: number, y: number }
  ): BlueprintComponent[] {
    return blueprintComponents.map(blueprintComponent => BlueprintComponentUtils.scaleBlueprintComponent(
      blueprintComponent,
      transformOrigin,
      scaling
    ));
  }

  static scaleBlueprintComponent(
    blueprintComponent: BlueprintComponent,
    transformOrigin: { x: number, y: number },
    scaling: { x: number, y: number },
  ): BlueprintComponent {
    blueprintComponent = ComponentUtils.scaleComponentPosition<BlueprintComponent>(
      blueprintComponent,
      transformOrigin,
      scaling
    );
    blueprintComponent = ComponentUtils.scaleComponentShapes(blueprintComponent, transformOrigin, scaling);

    blueprintComponent.children = blueprintComponent.children.map(child =>
      BlueprintComponentUtils.scaleBlueprintComponent(child, { x: 0, y: 0}, scaling));

    return blueprintComponent;
  }

  static getAllShapes(blueprintComponent: BlueprintComponent): Shape[] {
    return [
      ...blueprintComponent.shapes,
      ...blueprintComponent.children.flatMap(child => BlueprintComponentUtils.getAllShapes(child))
    ];
  }

}
