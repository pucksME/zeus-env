import { DomElementIds } from '../constants';
import { BlueprintElementDto, Configuration, ElementDto, ElementType } from '../gen/api-client';
import Konva from 'konva';
import { StorageKey } from './enums/storage-key.enum';
import { IRect } from 'konva/lib/types';
import { ComponentTreeNode } from './modules/designer/component.utils';

export abstract class AppUtils {

  private static apiConfiguration = AppUtils.buildApiConfiguration();

  private static buildApiConfiguration(): Configuration {

    const userSession = JSON.parse(localStorage.getItem(StorageKey.USER_SESSION));

    return new Configuration({
      basePath: 'http://localhost:3333',
      accessToken: userSession === null ? '' : userSession.token
    });
  }

  static getApiConfiguration() {
    return AppUtils.apiConfiguration;
  }

  static setApiToken(token) {
    AppUtils.apiConfiguration.accessToken = token;
  }

  static debounce<T>(handler: (...args: unknown[]) => T, delay = 500) {

    let timeout = null;

    return (...args: unknown[]) => {
      if (timeout !== null) {
        clearTimeout(timeout);
        timeout = null;
      }

      return new Promise<T>((resolve, reject) => {
        timeout = setTimeout(() => resolve(handler(...args)), delay);
      });
    };

  }

  static buildDomElementIdTagValue(domElementId: DomElementIds): string {
    return `#${domElementId}`;
  }

  static includesMousePointer(clientRect: IRect, pointerPosition: Konva.Vector2d): boolean {
    return Konva.Util.haveIntersection(clientRect, { ...pointerPosition, height: 0, width: 0 });
  }

  static findInTrees<T extends ComponentTreeNode<T>>(
    trees: T[],
    treeNodeUuid: string
  ): { node: T | undefined, pathCoordinates: {x: number, y: number} } {
    for (const tree of trees) {
      const result = AppUtils.findInTree<T>(tree, treeNodeUuid);

      if (result.node === undefined) {
        continue;
      }

      return result;
    }

    return {node: undefined, pathCoordinates: {x: 0, y: 0}};
  }

  static findInTree<T extends ComponentTreeNode<T>>(
    tree: T,
    treeNodeUuid: string,
    pathCoordinates: {x: number, y: number} = {x: 0, y: 0}
  ): { node: T | undefined, pathCoordinates: {x: number, y: number} } {
    if (tree.uuid === treeNodeUuid) {
      return { node: tree, pathCoordinates };
    }

    for (const element of tree.elements) {
      if (element.type === ElementType.Shape) {
        continue;
      }

      const result = AppUtils.findInTree<T>(
        element.element as T,
        treeNodeUuid,
        {
          x: pathCoordinates.x + tree.positionX,
          y: pathCoordinates.y + tree.positionY
        }
      );

      if (result.node === undefined) {
        continue;
      }

      return result;
    }

    return {node: undefined, pathCoordinates: {x: 0, y: 0}};
  }

  static findParentInTrees<T extends ComponentTreeNode<T>>(
    trees: T[],
    treeNodeUuid: string
  ): T | undefined {
    for (const tree of trees) {
      const parent = AppUtils.findParentInTree<T>(tree, treeNodeUuid);

      if (parent !== undefined) {
        return parent;
      }
    }

    return undefined;
  }

  static findParentInTree<T extends ComponentTreeNode<T>>(
    tree: T,
    treeNodeUuid: string
  ): T | undefined {
    for (const element of tree.elements) {
      if (element.type === ElementType.Shape) {
        continue;
      }

      if (element.element.uuid === treeNodeUuid) {
        return tree;
      }

      const parent = AppUtils.findParentInTree<T>(element.element as T, treeNodeUuid);

      if (parent !== undefined) {
        return parent;
      }
    }

    return undefined;
  }

  static calculateContainerPropertiesToFitElements(
    containerDimensions: { height: number, width: number },
    elementsProperties: { height: number, width: number, x: number, y: number },
    padding: { horizontal: number, vertical: number } = { horizontal: 0, vertical: 0 }
  ): { x: number, y: number, scale: number} {
    const scale = Math.min(
      (containerDimensions.height - (2 * padding.vertical)) / elementsProperties.height,
      (containerDimensions.width - (2 * padding.horizontal)) / elementsProperties.width
    );
    return {
      x: -(elementsProperties.x * scale - ((containerDimensions.width - (elementsProperties.width * scale)) / 2)),
      y: -(elementsProperties.y * scale - ((containerDimensions.height - (elementsProperties.height * scale)) / 2)),
      scale
    }
  }

  static sortItems<T extends { sorting: number }>(items: T[]): T[] {
    return items.sort((itemA, itemB) => itemA.sorting - itemB.sorting);
  }

  static sortElementDtos<T extends ElementDto | BlueprintElementDto>(elements: T[]): T[] {
    return elements.sort((elementA, elementB) => elementA.element.sorting - elementB.element.sorting);
  }

}
