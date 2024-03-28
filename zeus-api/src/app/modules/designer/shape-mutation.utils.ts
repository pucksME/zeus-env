import { Shape } from './entities/shape.entity';
import { ShapeMutation } from './entities/shape-mutation.entity';
import { v4 as generateUuid } from 'uuid';
import { Component } from './entities/component.entity';
import { ShapeMutationsDictKeyType } from './enums/shape-mutations-dict-key-type.enum';
import { SpecificShapeProperties } from './types/specific-shape-properties.type';
import { Position } from './enums/position.enum';
import { Dimension } from './enums/dimension.enum';
import { BlueprintComponent } from './entities/blueprint-component.entity';

export abstract class ShapeMutationUtils {

  static buildShapeMutation(componentEntity: Component, shapeEntity: Shape): ShapeMutation {
    const shapeMutations = new ShapeMutation();
    shapeMutations.uuid = generateUuid();
    shapeMutations.positionX = null;
    shapeMutations.positionY = null;
    shapeMutations.properties = {};
    shapeMutations.component = componentEntity;
    shapeMutations.shape = shapeEntity;
    return shapeMutations;
  }

  static extractShapeMutationsFromComponents(components: Component[]): Map<string, Map<string, ShapeMutation>> {
    const componentsShapeMutationsMap = new Map<string, Map<string, ShapeMutation>>();
    for (const component of components) {
      componentsShapeMutationsMap.set(
        component.uuid,
        ShapeMutationUtils.buildShapeMutationsMap(component.shapeMutations, ShapeMutationsDictKeyType.SHAPE_UUIDS)
      );
    }
    return componentsShapeMutationsMap;
  }

  static buildShapeMutationsMap(
    shapeMutationsEntities: ShapeMutation[],
    keyType: ShapeMutationsDictKeyType = ShapeMutationsDictKeyType.SHAPE_MUTATIONS_UUIDS
  ): Map<string, ShapeMutation> {
    const shapeMutationsMap = new Map<string, ShapeMutation>();

    for (const shapeMutations of shapeMutationsEntities) {
      shapeMutationsMap.set(
        (keyType === ShapeMutationsDictKeyType.SHAPE_MUTATIONS_UUIDS)
          ? shapeMutations.uuid
          : shapeMutations.shape.uuid,
        shapeMutations
      )
    }

    return shapeMutationsMap;
  }

  static buildComponentInstancesShapeMutationsMap(
    shapeMutations: ShapeMutation[]
  ): Map<string, Map<string, ShapeMutation>> {
    const componentInstancesShapeMutationsMap = new Map<string, Map<string, ShapeMutation>>();

    for (const shapeMutation of shapeMutations) {
      const shapeMutationsMap = componentInstancesShapeMutationsMap.get(shapeMutation.component.uuid);
      if (shapeMutationsMap === undefined) {
        componentInstancesShapeMutationsMap.set(
          shapeMutation.component.uuid,
          new Map<string, ShapeMutation>([[shapeMutation.shape.uuid, shapeMutation]])
        );
        continue;
      }
      shapeMutationsMap.set(shapeMutation.shape.uuid, shapeMutation);
    }

    return componentInstancesShapeMutationsMap;
  }

  static updateShapeMutations(
    shape: Shape,
    shapeMutationsDict: {[shapeUuid: string]: ShapeMutation},
    newMutations: {
      positionX?: number | null,
      positionY?: number | null,
      properties?: Partial<SpecificShapeProperties> | null
    }
  ): ShapeMutation {
    let shapeMutations = shapeMutationsDict[shape.uuid];

    if (shapeMutations === undefined) {
      shapeMutations = ShapeMutationUtils.buildShapeMutation(null, shape);
      shapeMutationsDict[shape.uuid] = shapeMutations;
    }

    if (newMutations.positionX !== undefined) {
      shapeMutations.positionX = newMutations.positionX;
    }

    if (newMutations.positionY !== undefined) {
      shapeMutations.positionY = newMutations.positionY;
    }

    if (newMutations.properties !== undefined) {
      shapeMutations.properties = (newMutations.properties !== null)
        ? {...shapeMutations, ...newMutations.properties}
        : null;
    }

    return shapeMutations;
  }

  static setupShapeMutations(
    component: Component,
    blueprintComponent: BlueprintComponent
  ): Component {
    component.shapeMutations.push(...blueprintComponent.shapes.map(shape => ShapeMutationUtils.buildShapeMutation(
      component,
      shape
    )));

    for (const child of blueprintComponent.children) {
      ShapeMutationUtils.setupShapeMutations(component, child);
    }

    return component;
  }

  static buildShapeWithMutations(
    componentEntity: Component | null,
    shapeEntity: Shape,
    shapeMutationsEntity: ShapeMutation
  ): Shape {
    const shape = new Shape();
    shape.uuid = generateUuid();
    shape.name = shapeEntity.name;
    shape.positionX = (shapeMutationsEntity !== undefined && shapeMutationsEntity.positionX !== null)
      ? shapeMutationsEntity.positionX
      : shapeEntity.positionX;
    shape.positionY = (shapeMutationsEntity !== undefined && shapeMutationsEntity.positionY !== null)
      ? shapeMutationsEntity.positionY
      : shapeEntity.positionY;
    shape.properties = (shapeMutationsEntity !== undefined && shapeMutationsEntity.properties !== null)
      ? { ...shapeEntity.properties, ...shapeMutationsEntity.properties }
      : shapeEntity.properties;
    shape.type = shapeEntity.type;
    shape.component = componentEntity;
    shape.sorting = shapeEntity.sorting;
    return shape;
  }

  static setupShapeMutationPositions(shapeMutationEntity: ShapeMutation, position: Position | null = null): ShapeMutation {
    if ((position === null || position === Position.POSITION_X) && shapeMutationEntity.positionX === null) {
      shapeMutationEntity.positionX = shapeMutationEntity.shape.positionX;
    }

    if ((position === null || position === Position.POSITION_Y) && shapeMutationEntity.positionY === null) {
      shapeMutationEntity.positionY = shapeMutationEntity.shape.positionY;
    }

    return shapeMutationEntity;
  }

  static setupShapeMutationDimensions(shapeMutationEntity: ShapeMutation, dimension: Dimension | null = null): ShapeMutation {
    if ((dimension === null || dimension === Dimension.HEIGHT) && shapeMutationEntity.properties.height === undefined) {
      shapeMutationEntity.properties.height = shapeMutationEntity.shape.properties.height;
    }

    if ((dimension === null || dimension === Dimension.WIDTH) && shapeMutationEntity.properties.width === undefined) {
      shapeMutationEntity.properties.width = shapeMutationEntity.shape.properties.width;
    }

    return shapeMutationEntity;
  }

  static getShapeMutations(component: Component): ShapeMutation[] {
    return [
      ...component.shapeMutations,
      ...component.children
        .map(child => ShapeMutationUtils.getShapeMutations(child))
        .flatMap(shapeMutations => shapeMutations)
    ];
  }

}
