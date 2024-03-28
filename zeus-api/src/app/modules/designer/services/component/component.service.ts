import {
  BadRequestException,
  ForbiddenException,
  Inject,
  Injectable,
  InternalServerErrorException,
  NotFoundException
} from '@nestjs/common';
import { ComponentDataService } from '../../data/component-data/component-data.service';
import { CreateComponentDto } from '../../dtos/create-component.dto';
import { Component } from '../../entities/component.entity';
import { ComponentDto } from '../../dtos/component.dto';
import { Shape } from '../../entities/shape.entity';
import { ComponentUtils } from '../../component.utils';
import { UpdateComponentPositionDto } from '../../dtos/update-component-position.dto';
import { UpdatedComponentPositionDto } from '../../dtos/updated-component-position.dto';
import { REQUEST } from '@nestjs/core';
import { RequestKeys } from '../../../../enums/request-keys.enum';
import { ScaleElementsDto } from '../../dtos/scale-elements.dto';
import { TranslateElementsDto } from '../../dtos/translate-elements.dto';
import { PositionElementsDto } from '../../dtos/position-elements.dto';
import { ReshapeElementsDto } from '../../dtos/reshape-elements.dto';
import { UpdateElementsPropertiesDto } from '../../dtos/update-elements-properties.dto';
import { AlignElementsDto } from '../../dtos/align-elements.dto';
import { View } from '../../entities/view.entity';
import { UpdateElementSortingDto } from '../../dtos/update-element-sorting.dto';
import { UpdateComponentNameDto } from '../../dtos/update-component-name.dto';
import { ShapeUtils } from '../../shape.utils';
import { DesignerUtils } from '../../designer.utils';
import { AppUtils } from '../../../../app.utils';
import { ShapeMutationUtils } from '../../shape-mutation.utils';
import { ShapeDataService } from '../../data/shape-data/shape-data.service';
import { BlueprintComponentDataService } from '../../data/blueprint-component-data/blueprint-component-data.service';
import { UpdatedElementsDto } from '../../dtos/updated-elements.dto';
import { ShapeMutationDataService } from '../../data/shape-mutation-data/shape-mutation-data.service';
import { ShapeMutation } from '../../entities/shape-mutation.entity';
import { ScaleOrigin } from '../../enums/scale-origin.enum';
import { DeleteElementsDto } from '../../dtos/delete-elements.dto';
import { ComponentMutationDataService } from '../../data/component-mutation-data/component-mutation-data.service';
import { ComponentMutationUtils } from '../../component-mutation.utils';
import { BlueprintComponentUtils } from '../../blueprint-component.utils';
import { ShapeMutationsDictKeyType } from '../../enums/shape-mutations-dict-key-type.enum';
import { ComponentMutation } from '../../entities/component-mutation.entity';
import { ResetElementsMutationsDto } from '../../dtos/reset-elements-mutations.dto';

@Injectable()
export class ComponentService {

  constructor(
    @Inject(REQUEST)
    private readonly req,
    private readonly componentDataService: ComponentDataService,
    private readonly blueprintComponentDataService: BlueprintComponentDataService,
    private readonly shapeDataService: ShapeDataService,
    private readonly shapeMutationDataService: ShapeMutationDataService,
    private readonly componentMutationDataService: ComponentMutationDataService
  ) {
  }

  async save(viewUuid: string, createComponentDto: CreateComponentDto): Promise<ComponentDto> {

    const view: View | undefined = this.req[RequestKeys.VIEW];

    if (view === undefined) {
      throw new InternalServerErrorException('Could not save component: view was not injected');
    }

    if (view.components === undefined) {
      throw new InternalServerErrorException('Could not save component: view components were not injected');
    }

    const component = new Component();
    component.positionX = createComponentDto.positionX;
    component.positionY = createComponentDto.positionY;
    component.view = view;
    component.sorting = 0;
    component.blueprintComponent = null;
    component.shapes = createComponentDto.shapes.map(createComponentShapeDto => {
      const shape = new Shape();
      shape.sorting = 0;
      shape.type = createComponentShapeDto.type;
      shape.positionX = createComponentShapeDto.positionX;
      shape.positionY = createComponentShapeDto.positionY;
      // TODO: check if properties are correct for the chosen type (also validate e.g. height and width for rectangles)
      shape.properties = createComponentShapeDto.properties;
      return shape;
    });
    component.children = [];

    view.components.forEach(component => component.sorting++);
    await this.componentDataService.saveMany(view.components);

    return ComponentUtils.buildComponentDto(
      await this.componentDataService.save(ComponentUtils.fixNegativeDimensions(component))
    );

  }

  async saveWithShape(shapeUuid: string): Promise<ComponentDto> {
    const shape: Shape | undefined = this.req[RequestKeys.SHAPE];

    if (shape === undefined) {
      throw new InternalServerErrorException('Could not save component with shape: shape was not injected');
    }

    if (shape.component === undefined) {
      throw new InternalServerErrorException('Could not save component with shape: component was not injected');
    }

    if (shape.component.view === undefined) {
      throw new InternalServerErrorException('Could not save component with shape: view was not injected');
    }

    if (shape.component.blueprintComponent === undefined) {
      throw new InternalServerErrorException('Could not save component with shape: blueprint component was not injected');
    }

    if (shape.component.blueprintComponent !== null) {
      throw new ForbiddenException('Could not create component with shape: shape is part of a blueprint component');
    }

    if (shape.component.shapes === undefined) {
      throw new InternalServerErrorException('Could not save component with shape: shapes were not injected');
    }

    if (shape.component.shapes.length < 2) {
      throw new ForbiddenException('The parent component has not enough shapes');
    }

    const component = new Component();
    component.parent = shape.component;
    component.name = shape.name;
    component.positionX = shape.positionX;
    component.positionY = shape.positionY;
    shape.positionX = 0;
    shape.positionY = 0;
    component.sorting = shape.sorting;
    component.blueprintComponent = null;
    component.shapes = [shape];

    return ComponentUtils.buildComponentDto(
      await this.componentDataService.save(component)
    );
  }

  async updatePosition(
    componentUuid: string, updateComponentPositionDto: UpdateComponentPositionDto
  ): Promise<UpdatedComponentPositionDto> {

    const component: Component = this.req[RequestKeys.COMPONENT];

    if (component === undefined) {
      throw new InternalServerErrorException('Could not update component position: component was not injected');
    }

    component.positionX = updateComponentPositionDto.positionX;
    component.positionY = updateComponentPositionDto.positionY;

    return ComponentUtils.buildUpdatedComponentPositionDto(await this.componentDataService.save(component));

  }

  async scaleComponents(scaleElementsDto: ScaleElementsDto): Promise<UpdatedElementsDto> {
    const {
      components,
      shapes,
      shapeMutations,
      componentMutations,
      parentComponent,
      foundAllElements
    } = await ComponentUtils.findDirectChildElements(
      scaleElementsDto.parentComponentUuid,
      scaleElementsDto.elementUuids,
      this.componentDataService,
      this.blueprintComponentDataService,
      this.componentMutationDataService,
      {
        componentRelations: [
          'shapes',
          'blueprintComponent',
          'blueprintComponent.shapes',
          'shapeMutations',
          'shapeMutations.shape',
          'shapeMutations.component',
          'componentMutations',
          'componentMutations.blueprintComponent',
          'componentMutations.component'
        ],
        injectBlueprintComponents: true,
        blueprintComponentRelations: ['shapes']
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not scale components: at least one element did not exist');
    }

    const transformOrigin = DesignerUtils.computeTransformOriginForScaling(
      scaleElementsDto.elementsProperties,
      scaleElementsDto.scaleOrigin
    );

    const scaling = { x: scaleElementsDto.scaleX, y: scaleElementsDto.scaleY };

    ComponentUtils.scaleComponents(
      components,
      transformOrigin,
      scaling
    );

    ShapeUtils.scaleShapes<Shape>(shapes, transformOrigin, scaling);
    ShapeUtils.scaleShapes<ShapeMutation>(shapeMutations, transformOrigin, scaling);

    if (componentMutations.length !== 0) {
      BlueprintComponentUtils.scaleBlueprintComponentInstances(
        BlueprintComponentUtils.findBlueprintComponents(
          parentComponent.blueprintComponent,
          componentMutations.map(shapeMutation => shapeMutation.blueprintComponent.uuid)
        ),
        ComponentMutationUtils.buildComponentMutationMap(parentComponent.componentMutations),
        ShapeMutationUtils.buildShapeMutationsMap(parentComponent.shapeMutations, ShapeMutationsDictKeyType.SHAPE_UUIDS),
        transformOrigin,
        scaling
      );
      shapeMutations.push(...parentComponent.shapeMutations);
    }

    await this.componentDataService.saveMany(components);
    await this.shapeDataService.saveMany(shapes);
    await this.componentMutationDataService.saveMany(componentMutations);
    await this.shapeMutationDataService.saveMany(shapeMutations);

    return { elementUuids: scaleElementsDto.elementUuids };
  }

  async reshapeComponents(reshapeElementsDto: ReshapeElementsDto): Promise<UpdatedElementsDto> {
    if (reshapeElementsDto.height === undefined && reshapeElementsDto.width === undefined) {
      throw new BadRequestException('Could not reshape components: both height and width were not set');
    }

    return this.scaleComponents({
      parentComponentUuid: reshapeElementsDto.parentComponentUuid,
      elementUuids: reshapeElementsDto.elementUuids,
      elementsProperties: reshapeElementsDto.elementsProperties,
      scaleX: (reshapeElementsDto.width === undefined)
        ? 1
        : reshapeElementsDto.width / reshapeElementsDto.elementsProperties.width,
      scaleY: (reshapeElementsDto.height === undefined)
        ? 1
        : reshapeElementsDto.height / reshapeElementsDto.elementsProperties.height,
      scaleOrigin: (reshapeElementsDto.height !== undefined && reshapeElementsDto.width !== undefined)
        ? ScaleOrigin.BOTTOM_RIGHT
        : (reshapeElementsDto.height !== undefined)
          ? ScaleOrigin.BOTTOM
          : ScaleOrigin.RIGHT
    });
  }

  async translateComponents(translateElementsDto: TranslateElementsDto): Promise<UpdatedElementsDto> {
    const {
      components,
      shapes,
      shapeMutations,
      componentMutations,
      foundAllElements
    } = await ComponentUtils.findDirectChildElements(
      translateElementsDto.parentComponentUuid,
      translateElementsDto.elementUuids,
      this.componentDataService,
      this.blueprintComponentDataService,
      this.componentMutationDataService,
      {
        componentRelations: [
          'blueprintComponent',
          'shapes',
          'shapeMutations',
          'shapeMutations.shape',
          'componentMutations',
          'componentMutations.blueprintComponent'
        ]
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not translate elements: at least one element did not exist');
    }

    for (const element of [
      ...components,
      ...shapes,
      ...shapeMutations.map(shapeMutation => ShapeMutationUtils.setupShapeMutationPositions(shapeMutation)),
      ...componentMutations.map(componentMutation => ComponentMutationUtils.setupComponentMutationPosition(componentMutation))
    ]) {
      element.positionX += translateElementsDto.translateX;
      element.positionY += translateElementsDto.translateY;
    }

    await this.componentDataService.saveMany(components);
    await this.shapeDataService.saveMany(shapes);
    await this.shapeMutationDataService.saveMany(shapeMutations);
    await this.componentMutationDataService.saveMany(componentMutations);

    return { elementUuids: translateElementsDto.elementUuids };
  }

  async positionComponents(positionElementsDto: PositionElementsDto): Promise<UpdatedElementsDto> {
    if (positionElementsDto.positionX === undefined && positionElementsDto.positionY === undefined) {
      throw new BadRequestException('Could not position components: either x or y position has to be set');
    }

    const {
      components,
      shapes,
      shapeMutations,
      componentMutations,
      foundAllElements
    } = await ComponentUtils.findDirectChildElements(
      positionElementsDto.parentComponentUuid,
      positionElementsDto.elementUuids,
      this.componentDataService,
      this.blueprintComponentDataService,
      this.componentMutationDataService,
      {
        componentRelations: [
          'blueprintComponent',
          'shapes',
          'shapeMutations',
          'shapeMutations.shape',
          'componentMutations',
          'componentMutations.blueprintComponent'
        ]
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not position elements: at least one element did not exist');
    }

    const translate = {
      x: (positionElementsDto.positionX === undefined)
        ? 0
        : positionElementsDto.positionX - positionElementsDto.elementsProperties.x,
      y: (positionElementsDto.positionY === undefined)
        ? 0
        : positionElementsDto.positionY - positionElementsDto.elementsProperties.y
    };

    for (const element of [
      ...components,
      ...shapes,
      ...componentMutations.map(componentMutation =>
        ComponentMutationUtils.setupComponentMutationPosition(componentMutation)),
      ...shapeMutations.map(shapeMutation => ShapeMutationUtils.setupShapeMutationPositions(shapeMutation))
    ]) {
      element.positionX += translate.x;
      element.positionY += translate.y;
    }

    await this.componentDataService.saveMany(components);
    await this.shapeDataService.saveMany(shapes);
    await this.componentMutationDataService.saveMany(componentMutations);
    await this.shapeMutationDataService.saveMany(shapeMutations);

    return { elementUuids: positionElementsDto.elementUuids };
  }

  async updateComponentsProperties(updateElementsPropertiesDto: UpdateElementsPropertiesDto): Promise<UpdatedElementsDto> {
    const {
      components,
      shapes,
      shapeMutations,
      componentMutations,
      parentComponent,
      foundAllElements
    } = await ComponentUtils.findDirectChildElements(
      updateElementsPropertiesDto.parentComponentUuid,
      updateElementsPropertiesDto.elementUuids,
      this.componentDataService,
      this.blueprintComponentDataService,
      this.componentMutationDataService,
      {
        componentRelations: [
          'shapes',
          'blueprintComponent',
          'blueprintComponent.shapes',
          'shapes.blueprintComponent',
          'shapeMutations',
          'shapeMutations.shape',
          'componentMutations',
          'componentMutations.blueprintComponent'
        ],
        injectBlueprintComponents: true,
        blueprintComponentRelations: ['shapes']
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not update components properties: at least one element did not exist');
    }

    for (const component of components) {
      const {shapes, shapeMutations} = ComponentUtils.getAllShapesAndMutations(component);
      ShapeUtils.updateShapesProperties<Shape>(shapes, updateElementsPropertiesDto.properties);
      ShapeUtils.updateShapesProperties<ShapeMutation>(shapeMutations, updateElementsPropertiesDto.properties);
    }

    await this.componentDataService.saveMany(components);

    await this.shapeDataService.saveMany(ShapeUtils.updateShapesProperties<Shape>(
      shapes, updateElementsPropertiesDto.properties
    ));

    if (componentMutations.length !== 0) {
      const blueprintComponents = BlueprintComponentUtils.findBlueprintComponents(
        parentComponent.blueprintComponent,
        componentMutations.map(componentMutation => componentMutation.blueprintComponent.uuid)
      );

      const shapeMutationsMap = ShapeMutationUtils.buildShapeMutationsMap(
        parentComponent.shapeMutations,
        ShapeMutationsDictKeyType.SHAPE_UUIDS
      );

      for (const shapeUuid of BlueprintComponentUtils.getShapesOfBlueprintComponents(
        blueprintComponents
      ).map(shape => shape.uuid)) {
        const shapeMutation = shapeMutationsMap.get(shapeUuid);

        if (shapeMutation === undefined) {
          continue;
        }

        shapeMutations.push(shapeMutation);
      }
    }

    await this.shapeMutationDataService.saveMany(ShapeUtils.updateShapesProperties<ShapeMutation>(
      shapeMutations, updateElementsPropertiesDto.properties
    ));

    return { elementUuids: updateElementsPropertiesDto.elementUuids };
  }

  async deleteComponents(deleteElementsDto: DeleteElementsDto): Promise<void> {
    if (deleteElementsDto.elementUuids.length === 0) {
      throw new NotFoundException('Could not delete components: no components found');
    }

    const {
      components,
      shapes,
      shapeMutations,
      componentMutations,
      parentComponent,
      foundAllElements
    } = await ComponentUtils.findDirectChildElements(
      deleteElementsDto.parentComponentUuid,
      deleteElementsDto.elementUuids,
      this.componentDataService,
      this.blueprintComponentDataService,
      this.componentMutationDataService,
      {
        componentRelations: [
          'blueprintComponent',
          'shapes'
        ]
      }
    );

    if (componentMutations.length !== 0 || shapeMutations.length !== 0) {
      throw new ForbiddenException('Could not delete components: at least one mutation was included');
    }

    if (!foundAllElements) {
      throw new NotFoundException('Could not delete components properties: at least one element did not exist');
    }

    if (components.length !== 0) {
      await this.componentDataService.deleteMany(components.map(component => component.uuid));
    }

    if (parentComponent !== null && (parentComponent.shapes.length - shapes.length) < 1) {
      throw new ForbiddenException('Could not delete shapes: the parent component would have not enough shapes left');
    }

    if (shapes.length !== 0) {
      await this.shapeDataService.deleteMany(shapes.map(shape => shape.uuid));
    }
  }

  async alignComponents(alignElementsDto: AlignElementsDto): Promise<UpdatedElementsDto> {
    const elementUuids = alignElementsDto.elements.map(element => element.elementUuid);

    const {
      components,
      shapes,
      shapeMutations,
      componentMutations,
      foundAllElements
    } = await ComponentUtils.findDirectChildElements(
      alignElementsDto.parentComponentUuid,
      elementUuids,
      this.componentDataService,
      this.blueprintComponentDataService,
      this.componentMutationDataService,
      {
        componentRelations: [
          'shapes',
          'blueprintComponent',
          'blueprintComponent.shapes',
          'shapes.blueprintComponent',
          'shapeMutations',
          'shapeMutations.shape',
          'componentMutations',
          'componentMutations.blueprintComponent'
        ]
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not align components: at least one element did not exist');
    }

    ComponentUtils.alignElements<Component | Shape | ComponentMutation | ShapeMutation>(
      [
        ...components,
        ...shapes,
        ...componentMutations.map(componentMutation =>
          ComponentMutationUtils.setupComponentMutationPosition(componentMutation)),
        ...shapeMutations.map(shapeMutation =>
          ShapeMutationUtils.setupShapeMutationPositions(shapeMutation))
      ],
      alignElementsDto
    );

    await this.componentDataService.saveMany(components);
    await this.shapeDataService.saveMany(shapes);
    await this.componentMutationDataService.saveMany(componentMutations);
    await this.shapeMutationDataService.saveMany(shapeMutations);

    return { elementUuids };
  }

  async updateComponentSorting(updateElementSortingDto: UpdateElementSortingDto): Promise<UpdatedElementsDto> {
    let element: Component | Shape | null = null;
    let candidateElements: (Component | Shape)[];

    if (updateElementSortingDto.parentComponentUuid === null) {
      element = await this.componentDataService.find(
        updateElementSortingDto.elementUuid,
        [
          'view',
          'view.components'
        ]
      );

      if (element === undefined) {
        throw new NotFoundException('Could not sort component: element did not exist');
      }

      candidateElements = element.view.components;
    } else {
      let parentComponent = await this.componentDataService.find(
        updateElementSortingDto.parentComponentUuid,
        ['shapes']
      );

      if (parentComponent === undefined) {
        throw new NotFoundException('Could not sort component: parent component did not exist');
      }

      parentComponent = await this.componentDataService.findDescendants(parentComponent);
      candidateElements = [...parentComponent.shapes, ...parentComponent.children];
      element = candidateElements.find(element => element.uuid === updateElementSortingDto.elementUuid);

      if (element === undefined) {
        throw new NotFoundException('Could not sort component: element did not exist');
      }
    }

    const elementsToUpdate = AppUtils.updateSorting<Component | Shape>(
      candidateElements,
      {
        itemUuid: element.uuid,
        oldSorting: element.sorting,
        newSorting: updateElementSortingDto.sorting
      });

    await this.componentDataService.saveMany(elementsToUpdate.filter(element => element instanceof Component) as Component[]);
    await this.shapeDataService.saveMany(elementsToUpdate.filter(element => element instanceof Shape) as Shape[]);

    return { elementUuids: elementsToUpdate.map(element => element.uuid) };
  }

  async updateName(componentUuid: string, updateComponentNameDto: UpdateComponentNameDto): Promise<ComponentDto> {

    const component: Component | undefined = this.req[RequestKeys.COMPONENT];

    if (component === undefined) {
      throw new InternalServerErrorException('Could not update component name: component was not injected');
    }

    const name = (updateComponentNameDto.name === undefined) ? null : updateComponentNameDto.name;

    component.name = name;

    return ComponentUtils.buildComponentDto(await this.componentDataService.save(component));

  }

  async resetMutations(resetElementsMutationsDto: ResetElementsMutationsDto): Promise<void> {
    if (resetElementsMutationsDto.elementUuids.length === 0) {
      throw new NotFoundException('Could not delete mutations: no mutations found');
    }

    const {
      components,
      componentMutations,
      shapeMutations,
      foundAllElements
    } = await ComponentUtils.findDirectChildElements(
      resetElementsMutationsDto.parentComponentUuid,
      resetElementsMutationsDto.elementUuids,
      this.componentDataService,
      this.blueprintComponentDataService,
      this.componentMutationDataService,
      {
        componentRelations: [
          'blueprintComponent',
          'componentMutations',
          'shapeMutations'
        ]
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not reset mutations: at least one element did not exist');
    }

    const reset = (componentMutations: ComponentMutation[], shapeMutations: ShapeMutation[]) => {
      for (const mutation of [...componentMutations, ...shapeMutations]) {
        mutation.positionX = null;
        mutation.positionY = null;
      }

      for (const shapeMutation of shapeMutations) {
        shapeMutation.properties = {};
      }
    }

    for (const component of components) {
      reset(component.componentMutations, component.shapeMutations);
    }

    reset(componentMutations, shapeMutations);

    await this.componentDataService.saveMany(components);
    await this.componentMutationDataService.saveMany(componentMutations);
    await this.shapeMutationDataService.saveMany(shapeMutations);
  }

}
