import { BlueprintComponent } from './entities/blueprint-component.entity';
import { ComponentMutation } from './entities/component-mutation.entity';
import { v4 as generateUuid } from 'uuid';
import { Component } from './entities/component.entity';

export abstract class ComponentMutationUtils {

  static buildComponentMutation(
    component: Component,
    blueprintComponent: BlueprintComponent
  ): ComponentMutation {
    const componentMutation = new ComponentMutation();
    componentMutation.uuid = generateUuid();
    componentMutation.positionX = null;
    componentMutation.positionY = null;
    componentMutation.component = component;
    componentMutation.blueprintComponent = blueprintComponent;
    return componentMutation;
  }

  static setupComponentMutations(
    component: Component,
    blueprintComponent: BlueprintComponent,
    isRoot = true
  ) {

    if (!isRoot) {
      component.componentMutations.push(ComponentMutationUtils.buildComponentMutation(component, blueprintComponent));
    }

    for (const child of blueprintComponent.children) {
      ComponentMutationUtils.setupComponentMutations(component, child, false);
    }

    return component;
  }

  static buildComponentMutationMap(componentMutations: ComponentMutation[]): Map<string, ComponentMutation> {
    const componentMutationsMap = new Map<string, ComponentMutation>();

    for (const componentMutation of componentMutations) {
      componentMutationsMap.set(componentMutation.blueprintComponent.uuid, componentMutation);
    }

    return componentMutationsMap;
  }

  static buildMutatedPosition(
    blueprintComponent: BlueprintComponent,
    componentMutation: ComponentMutation
  ): { positionX: number, positionY: number } {
    return {
      positionX: (componentMutation.positionX === null) ? blueprintComponent.positionX : componentMutation.positionX,
      positionY: (componentMutation.positionY === null) ? blueprintComponent.positionY : componentMutation.positionY
    };
  }

  static setupComponentMutationPosition(componentMutation: ComponentMutation): ComponentMutation {
    if (componentMutation.positionX === null) {
      componentMutation.positionX = componentMutation.blueprintComponent.positionX;
    }

    if (componentMutation.positionY === null) {
      componentMutation.positionY = componentMutation.blueprintComponent.positionY;
    }

    return componentMutation;
  }

  static getComponentMutations(component: Component): ComponentMutation[] {
    return [
      ...component.componentMutations,
      ...component.children
        .map(child => ComponentMutationUtils.getComponentMutations(child))
        .flatMap(componentMutations => componentMutations)
    ];
  }

  static buildComponentInstancesComponentMutationsMap(
    componentMutations: ComponentMutation[]
  ): Map<string, Map<string, ComponentMutation>> {
    const componentInstancesComponentMutationMap = new Map<string, Map<string, ComponentMutation>>();

    for (const componentMutation of componentMutations) {
      const componentMutationsMap = componentInstancesComponentMutationMap.get(componentMutation.component.uuid);

      if (componentMutationsMap === undefined) {
        componentInstancesComponentMutationMap.set(
          componentMutation.component.uuid,
          new Map<string, ComponentMutation>([[componentMutation.blueprintComponent.uuid, componentMutation]])
        );
        continue;
      }
      componentMutationsMap.set(componentMutation.blueprintComponent.uuid, componentMutation);
    }

    return componentInstancesComponentMutationMap;
  }

}
