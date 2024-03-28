import {
  BlueprintComponentDto,
  BlueprintElementDto,
  ComponentDto,
  ElementDto,
  ElementType,
  ShapeDto
} from '../../../gen/api-client';
import { v4 as generateUuid } from 'uuid';
import { AppUtils } from '../../app.utils';

export interface ComponentTreeNodeElement<T> {
  element: T | ShapeDto;
  type: ElementType;
}

export interface ComponentTreeNode<T> {
  uuid: string;
  positionX: number;
  positionY: number;
  sorting: number;
  elements: ComponentTreeNodeElement<T>[];
}

export abstract class ComponentUtils {

  static flattenComponentTrees<T extends ComponentTreeNode<T>>(
    componentTrees: T[],
    filterOperation: (element: ComponentTreeNodeElement<T>) => boolean
  ): ComponentTreeNodeElement<T>[] {
    return componentTrees.flatMap(
      componentTree => ComponentUtils.flattenComponentTree(
        componentTree,
        filterOperation
      )
    );
  }

  static flattenComponentTree<T extends ComponentTreeNode<T>>(
    componentTree: T,
    filterOperation: (element: ComponentTreeNodeElement<T>) => boolean,
    isRootNode = true
  ): ComponentTreeNodeElement<T>[] {

    const elements: ComponentTreeNodeElement<T>[] = [];
    if (isRootNode) {
      const componentElement = {
        element: componentTree,
        type: ElementType.Component
      };

      if (filterOperation(componentElement)) {
        elements.push(componentElement);
      }
    }

    for (const element of componentTree.elements) {
      if (filterOperation(element)) {
        elements.push(element);
      }

      if (element.type !== ElementType.Component) {
        continue;
      }

      elements.push(...ComponentUtils.flattenComponentTree<T>(element.element as T, filterOperation, false));
    }

    return elements;
  }

  static getShapesOfElements<T extends ComponentTreeNode<T>>(
    elements: ComponentTreeNodeElement<T>[]
  ): ShapeDto[] {
    return elements.flatMap(element => ComponentUtils.getShapesOfElement<T>(element));
  }

  static getShapesOfElement<T extends ComponentTreeNode<T>>(
    element: ComponentTreeNodeElement<T>
  ): ShapeDto[] {
    if (element.type === ElementType.Shape) {
      return [element.element as ShapeDto];
    }

    const component = element.element as T;
    const shapes: ShapeDto[] = [];

    for (const element of component.elements) {
      if (element.type === ElementType.Component) {
        shapes.push(...ComponentUtils.getShapesOfElement<T>(element));
        continue;
      }

      shapes.push(element.element as ShapeDto);
    }

    return shapes;
  }

  static mapComponentsToElements<T extends ComponentDto | BlueprintComponentDto>(
    componentDtos: T[]
  ): ComponentTreeNodeElement<T>[] {
    return componentDtos.map(component => ({element: component, type: ElementType.Component}));
  }

  static replaceShapesInComponentTrees<T extends ComponentTreeNode<T>>(
    components: T[],
    replaceOperation: (shape: ShapeDto) => ShapeDto
  ): T[] {
    return components.map(component => ComponentUtils.replaceShapesInComponentTree<T>(component, replaceOperation))
  }

  static replaceShapesInComponentTree<T extends ComponentTreeNode<T>>(
    component: T,
    replaceOperation: (shape: ShapeDto) => ShapeDto
  ): T {
    component.elements.map(element => (element.type === ElementType.Shape)
      ? replaceOperation(element.element as ShapeDto)
      : { element: ComponentUtils.replaceShapesInComponentTree(element.element as T, replaceOperation)});

    return component;
  }

  static traverseComponentTrees<T extends ComponentTreeNode<T>>(
    components: T[],
    operation: (component: T) => T
  ): T[] {
    return components.map(component => ComponentUtils.traverseComponentTree<T>(component, operation));
  }

  static traverseComponentTree<T extends ComponentTreeNode<T>>(
    component: T,
    operation: (component: T) => T
  ): T {
    component = operation(component);
    component.elements = component.elements.map(element => (element.type === ElementType.Shape)
      ? element
      : {
        element: ComponentUtils.traverseComponentTree<T>(element.element as T, operation),
        type: ElementType.Component
    });
    return component;
  }

  static sortElements<T extends ElementDto | BlueprintElementDto>(elements: T[]): T[] {
    return elements.sort(
      (elementA, elementB) => elementA.element.sorting - elementB.element.sorting
    );
  }

  static mapToBlueprintComponentDto(componentDto: ComponentDto): BlueprintComponentDto {
    return {
      uuid: generateUuid(),
      name: '...',
      positionX: 0,
      positionY: 0,
      sorting: -1,
      referencingComponentUuids: [],
      elements: componentDto.elements.map(element => (element.type === ElementType.Shape)
        ? {
          ...element,
          element: {
            ...element.element,
            uuid: generateUuid(),
            properties: { ...(element.element as ShapeDto).properties }
          }
      } as BlueprintElementDto
      : {
        element: ComponentUtils.mapToBlueprintComponentDto(element.element as ComponentDto),
        type: ElementType.Component
      })
    }
  }

  static getDirectChildComponents<T extends ComponentTreeNode<T>>(component: T): T[] {
    const components: T[] = [];
    for (const element of component.elements) {
      if (element.type === ElementType.Shape) {
        continue;
      }

      components.push(element.element as T);
    }

    return components;
  }

  private static calculateSorting(oldSorting: number, newSorting: number): number {
    return newSorting + ((newSorting < oldSorting) ? -0.1 : 0.1);
  }

  static sortComponents<T extends ComponentTreeNode<T>>(
    components: T[],
    componentUuid: string,
    oldSorting: number,
    newSorting: number
  ): T[] {
    const component = components.find(
      component => component.uuid === componentUuid
    );

    if (component === undefined) {
      return components;
    }

    component.sorting = ComponentUtils.calculateSorting(oldSorting, newSorting);

    return AppUtils.sortItems<T>(components);
  }

  static sortComponentElements<T extends ElementDto | BlueprintElementDto>(
    elements: T[],
    elementUuid: string,
    oldSorting: number,
    newSorting: number
  ) {
    const element = elements.find(
      element => element.element.uuid === elementUuid
    );

    if (element === undefined) {
      return elements;
    }

    element.element.sorting = ComponentUtils.calculateSorting(oldSorting, newSorting);

    return AppUtils.sortElementDtos(elements);
  }

}
