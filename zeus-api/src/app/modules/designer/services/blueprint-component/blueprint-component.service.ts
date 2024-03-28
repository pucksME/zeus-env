import {
  BadRequestException,
  ForbiddenException,
  Inject,
  Injectable,
  InternalServerErrorException,
  NotFoundException
} from '@nestjs/common';
import { REQUEST } from '@nestjs/core';
import { BlueprintComponentDataService } from '../../data/blueprint-component-data/blueprint-component-data.service';
import { CreateBlueprintComponentDto } from '../../dtos/create-blueprint-component.dto';
import { BlueprintComponentDto } from '../../dtos/blueprint-component.dto';
import { BlueprintComponent } from '../../entities/blueprint-component.entity';
import { ComponentDataService } from '../../data/component-data/component-data.service';
import { Component } from '../../entities/component.entity';
import { RequestKeys } from '../../../../enums/request-keys.enum';
import { BlueprintComponentUtils } from '../../blueprint-component.utils';
import { ShapeMutationUtils } from '../../shape-mutation.utils';
import { ShapeMutationsDictKeyType } from '../../enums/shape-mutations-dict-key-type.enum';
import { DesignerWorkspace } from '../../entities/designer-workspace.entity';
import { InstantiateBlueprintComponentDto } from '../../dtos/instantiate-blueprint-component.dto';
import { ComponentDto } from '../../dtos/component.dto';
import { View } from '../../entities/view.entity';
import { ComponentUtils } from '../../component.utils';
import { ShapeDataService } from '../../data/shape-data/shape-data.service';
import { ComponentMutationUtils } from '../../component-mutation.utils';
import { ViewDataService } from '../../data/view-data/view-data.service';
import { UpdateBlueprintComponentNameDto } from '../../dtos/update-blueprint-component-name.dto';
import { ScaleElementsDto } from '../../dtos/scale-elements.dto';
import { UpdatedElementsDto } from '../../dtos/updated-elements.dto';
import { DesignerUtils } from '../../designer.utils';
import { ShapeUtils } from '../../shape.utils';
import { Shape } from '../../entities/shape.entity';
import { ReshapeElementsDto } from '../../dtos/reshape-elements.dto';
import { ScaleOrigin } from '../../enums/scale-origin.enum';
import { TranslateElementsDto } from '../../dtos/translate-elements.dto';
import { PositionElementsDto } from '../../dtos/position-elements.dto';
import { AlignElementsDto } from '../../dtos/align-elements.dto';
import { UpdateElementsPropertiesDto } from '../../dtos/update-elements-properties.dto';
import { DeleteElementsDto } from '../../dtos/delete-elements.dto';
import { UpdateElementSortingDto } from '../../dtos/update-element-sorting.dto';
import { AppUtils } from '../../../../app.utils';

@Injectable()
export class BlueprintComponentService {

  constructor(
    @Inject(REQUEST)
    private readonly req,
    private readonly blueprintComponentDataService: BlueprintComponentDataService,
    private readonly viewDataService: ViewDataService,
    private readonly componentDataService: ComponentDataService,
    private readonly shapeDataService: ShapeDataService
  ) {
  }

  async save(createBlueprintComponent: CreateBlueprintComponentDto): Promise<BlueprintComponentDto> {

    let component: Component | undefined = this.req[RequestKeys.COMPONENT];

    if (component === undefined) {
      throw new InternalServerErrorException('Could not save blueprint component: component was not injected');
    }

    if (component.blueprintComponent !== null) {
      throw new BadRequestException('Could not save blueprint component: component was already referencing a blueprint component');
    }

    if (component.shapes === undefined) {
      throw new InternalServerErrorException('Could not save blueprint component: shapes were not injected');
    }

    component = await this.componentDataService.findDescendants(component, ['blueprintComponent', 'shapes']);

    ComponentUtils.traverseComponentTree(
      component,
      (component: Component) => {
        if (component.blueprintComponent !== null) {
          throw new ForbiddenException('Could not save blueprint component: component was referencing blueprint components');
        }
        return component;
      }
    )

    const blueprintComponent = await this.blueprintComponentDataService.save(
      BlueprintComponentUtils.buildBlueprintComponent(
        component,
        await this.componentDataService.findRoot(component, ['view', 'view.workspace'])
      )
    );

    if (component.children.length !== 0) {
      await this.componentDataService.deleteMany(component.children.map(child => child.uuid));
    }

    component.children = [];
    ComponentMutationUtils.setupComponentMutations(component, blueprintComponent);
    ShapeMutationUtils.setupShapeMutations(component, blueprintComponent);

    await this.componentDataService.save(component);

    return BlueprintComponentUtils.buildBlueprintComponentDto(blueprintComponent);
  }

  async saveWithShape(shapeUuid: string): Promise<BlueprintComponentDto> {
    const shape = await this.shapeDataService.find(
      shapeUuid,
      [
        'component',
        'blueprintComponent',
        'blueprintComponent.shapes'
      ]
    );

    if (shape === undefined) {
      throw new NotFoundException(`There was no shape with uuid ${shapeUuid}`);
    }

    if (shape.blueprintComponent === null || shape.component !== null) {
      throw new ForbiddenException('Could not save blueprint component with shape: shape was not part of a blueprint component');
    }

    if (shape.blueprintComponent.shapes.length < 2) {
      throw new ForbiddenException('The parent blueprint component has not enough shapes');
    }

    const blueprintComponent = new BlueprintComponent();
    blueprintComponent.parent = shape.blueprintComponent;
    blueprintComponent.name = shape.name;
    blueprintComponent.positionX = shape.positionX;
    blueprintComponent.positionY = shape.positionY;
    shape.positionX = 0;
    shape.positionY = 0;
    blueprintComponent.sorting = shape.sorting;
    blueprintComponent.shapes = [shape];
    blueprintComponent.children = [];

    return BlueprintComponentUtils.buildBlueprintComponentDto(
      await this.blueprintComponentDataService.save(blueprintComponent)
    );
  }

  async find(workspaceUuid: string): Promise<BlueprintComponentDto[]> {
    const workspace: DesignerWorkspace | undefined = this.req[RequestKeys.DESIGNER_WORKSPACE];

    if (workspace === undefined) {
      throw new InternalServerErrorException('Could not find blueprint components: workspace was not injected');
    }

    if (workspace.blueprintComponents === undefined) {
      throw new InternalServerErrorException('Could not find blueprint components: blueprint components were not injected');
    }

    return (await this.blueprintComponentDataService.findTrees(['shapes'])).filter(
      blueprintComponentTree => workspace.blueprintComponents.find(
        blueprintComponent => blueprintComponent.uuid === blueprintComponentTree.uuid
      ) !== undefined
    ).map(BlueprintComponentUtils.buildBlueprintComponentDto);
  }

  async instantiate(instantiateBlueprintComponentDto: InstantiateBlueprintComponentDto): Promise<ComponentDto> {
    const view: View | undefined = this.req[RequestKeys.VIEW];

    if (view === undefined) {
      throw new InternalServerErrorException('Could not instantiate blueprint component: view was not injected');
    }

    let parentComponent: Component | null = null;

    if (instantiateBlueprintComponentDto.parentComponentUuid !== undefined) {
      parentComponent = await this.componentDataService.find(instantiateBlueprintComponentDto.parentComponentUuid, ['blueprintComponent']);
    }

    if (parentComponent === undefined) {
      throw new NotFoundException('Could not instantiate blueprint component: parent component did not exist');
    }

    if (parentComponent !== null && parentComponent.blueprintComponent !== null) {
      throw new ForbiddenException('Could not instantiate blueprint component: parent component was a blueprint component instance');
    }

    const blueprintComponent = await this.blueprintComponentDataService.find(
      instantiateBlueprintComponentDto.blueprintComponentUuid,
      ['shapes']
    );

    if (blueprintComponent === undefined) {
      throw new NotFoundException(`There was no blueprint component with uuid ${instantiateBlueprintComponentDto.blueprintComponentUuid}`);
    }

    let component = new Component();
    component.name = blueprintComponent.name;
    component.positionX = instantiateBlueprintComponentDto.positionX;
    component.positionY = instantiateBlueprintComponentDto.positionY;
    component.sorting = 0;
    component.shapes = [];

    if (parentComponent === null) {
      component.view = view;
    }

    component.componentMutations = [];
    component.shapeMutations = [];
    component.children = [];
    const blueprintComponentTree = await this.blueprintComponentDataService.findDescendants(
      blueprintComponent,
      ['shapes']
    );
    component.blueprintComponent = blueprintComponentTree;

    if (parentComponent !== null) {
      component.parent = parentComponent;
    }

    ComponentMutationUtils.setupComponentMutations(component, blueprintComponentTree);
    ShapeMutationUtils.setupShapeMutations(component, blueprintComponentTree);

    for (const component of view.components) {
      component.sorting++;
    }

    await this.componentDataService.saveMany(view.components);
    component = await this.componentDataService.save(component);
    component.blueprintComponent = blueprintComponentTree;

    return ComponentUtils.buildComponentDto(component);
  }

  async updateName(
    blueprintComponentUuid: string,
    updateBlueprintComponentNameDto: UpdateBlueprintComponentNameDto
  ): Promise<BlueprintComponentDto> {
    const blueprintComponent = await this.blueprintComponentDataService.find(blueprintComponentUuid);

    if (blueprintComponent === undefined) {
      throw new NotFoundException(`There was no blueprint component with uuid ${blueprintComponentUuid}`);
    }

    blueprintComponent.name = (updateBlueprintComponentNameDto.name === undefined)
      ? null
      : updateBlueprintComponentNameDto.name;

    return BlueprintComponentUtils.buildBlueprintComponentDto(
      await this.blueprintComponentDataService.save(blueprintComponent)
    );
  }

  async scaleBlueprintComponents(scaleElementsDto: ScaleElementsDto): Promise<UpdatedElementsDto> {
    const {
      blueprintComponents,
      shapes,
      foundAllElements
    } = await BlueprintComponentUtils.findDirectChildElements(
      scaleElementsDto.parentComponentUuid,
      scaleElementsDto.elementUuids,
      this.blueprintComponentDataService,
      {
        blueprintComponentRelations: ['shapes']
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not scale blueprint components: at least one element did not exist');
    }

    const transformOrigin = DesignerUtils.computeTransformOriginForScaling(
      scaleElementsDto.elementsProperties,
      scaleElementsDto.scaleOrigin
    );

    const scaling = { x: scaleElementsDto.scaleX, y: scaleElementsDto.scaleY };

    BlueprintComponentUtils.scaleBlueprintComponents(blueprintComponents, transformOrigin, scaling);
    ShapeUtils.scaleShapes<Shape>(shapes, transformOrigin, scaling);

    await this.blueprintComponentDataService.saveMany(blueprintComponents);
    await this.shapeDataService.saveMany(shapes);

    return { elementUuids: scaleElementsDto.elementUuids };
  }

  async reshapeBlueprintComponents(reshapeElementsDto: ReshapeElementsDto): Promise<UpdatedElementsDto> {
    if (reshapeElementsDto.height === undefined && reshapeElementsDto.width === undefined) {
      throw new BadRequestException('Could not reshape blueprint components: both height and width were not set');
    }

    return this.scaleBlueprintComponents({
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
        : (reshapeElementsDto.height !== undefined) ? ScaleOrigin.BOTTOM : ScaleOrigin.RIGHT
    });
  }

  async translateBlueprintComponents(translateElementsDto: TranslateElementsDto): Promise<UpdatedElementsDto> {
    const {
      blueprintComponents,
      shapes,
      foundAllElements
    } = await BlueprintComponentUtils.findDirectChildElements(
      translateElementsDto.parentComponentUuid,
      translateElementsDto.elementUuids,
      this.blueprintComponentDataService,
      {
        blueprintComponentRelations: ['shapes']
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not translate blueprint components: at least one element did not exist');
    }

    for (const element of [...blueprintComponents, ...shapes]) {
      element.positionX += translateElementsDto.translateX;
      element.positionY += translateElementsDto.translateY;
    }

    await this.blueprintComponentDataService.saveMany(blueprintComponents);
    await this.shapeDataService.saveMany(shapes);

    return { elementUuids: translateElementsDto.elementUuids };
  }

  async positionBlueprintComponents(positionElementsDto: PositionElementsDto): Promise<UpdatedElementsDto> {
    if (positionElementsDto.positionX === undefined && positionElementsDto.positionY === undefined) {
      throw new BadRequestException('Could not position blueprint components: either x or y position has to be set');
    }

    const {
      blueprintComponents,
      shapes,
      foundAllElements
    } = await BlueprintComponentUtils.findDirectChildElements(
      positionElementsDto.parentComponentUuid,
      positionElementsDto.elementUuids,
      this.blueprintComponentDataService,
      {
        blueprintComponentRelations: ['shapes']
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not position blueprint components: at least one element did not exist');
    }

    const translate = {
      x: (positionElementsDto.positionX === undefined)
        ? 0
        : positionElementsDto.positionX - positionElementsDto.elementsProperties.x,
      y: (positionElementsDto.positionY === undefined)
        ? 0
        : positionElementsDto.positionY - positionElementsDto.elementsProperties.y
    };

    for (const element of [...blueprintComponents, ...shapes]) {
      element.positionX += translate.x;
      element.positionY += translate.y;
    }

    await this.blueprintComponentDataService.saveMany(blueprintComponents);
    await this.shapeDataService.saveMany(shapes);

    return { elementUuids: positionElementsDto.elementUuids };
  }

  async alignBlueprintComponents(alignElementsDto: AlignElementsDto): Promise<UpdatedElementsDto> {
    const elementUuids = alignElementsDto.elements.map(element => element.elementUuid);

    const {
      blueprintComponents,
      shapes,
      foundAllElements
    } = await BlueprintComponentUtils.findDirectChildElements(
      alignElementsDto.parentComponentUuid,
      elementUuids,
      this.blueprintComponentDataService,
      {
        blueprintComponentRelations: ['shapes']
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not align blueprint components: at least one element did not exist');
    }

    ComponentUtils.alignElements<BlueprintComponent | Shape>(
      [...blueprintComponents, ...shapes],
      alignElementsDto
    );

    await this.blueprintComponentDataService.saveMany(blueprintComponents);
    await this.shapeDataService.saveMany(shapes);

    return { elementUuids };
  }

  async updateBlueprintComponentProperties(
    updateElementsPropertiesDto: UpdateElementsPropertiesDto
  ): Promise<UpdatedElementsDto> {
    const {
      blueprintComponents,
      shapes,
      foundAllElements
    } = await BlueprintComponentUtils.findDirectChildElements(
      updateElementsPropertiesDto.parentComponentUuid,
      updateElementsPropertiesDto.elementUuids,
      this.blueprintComponentDataService,
      {
        blueprintComponentRelations: ['shapes']
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not update blueprint component properties: at least one element did not exist');
    }

    for (const blueprintComponent of blueprintComponents) {
      ShapeUtils.updateShapesProperties<Shape>(
        BlueprintComponentUtils.getAllShapes(blueprintComponent),
        updateElementsPropertiesDto.properties
      );
    }

    await this.blueprintComponentDataService.saveMany(blueprintComponents);
    await this.shapeDataService.saveMany(ShapeUtils.updateShapesProperties<Shape>(
      shapes,
      updateElementsPropertiesDto.properties
    ));

    return { elementUuids: updateElementsPropertiesDto.elementUuids };
  }

  async deleteBlueprintComponents(deleteElementsDto: DeleteElementsDto): Promise<void> {
    if (deleteElementsDto.elementUuids.length === 0) {
      throw new NotFoundException('Could not delete blueprint components: no blueprint components found');
    }

    const {
      blueprintComponents,
      shapes,
      parentBlueprintComponent,
      foundAllElements
    } = await BlueprintComponentUtils.findDirectChildElements(
      deleteElementsDto.parentComponentUuid,
      deleteElementsDto.elementUuids,
      this.blueprintComponentDataService,
      {
        blueprintComponentRelations: ['shapes']
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not delete blueprint components: at least one element did not exist');
    }

    if (blueprintComponents.length !== 0) {
      await this.blueprintComponentDataService.deleteMany(
        blueprintComponents.map(blueprintComponent => blueprintComponent.uuid)
      );
    }

    if (parentBlueprintComponent !== null && (parentBlueprintComponent.shapes.length - shapes.length) < 1) {
      throw new ForbiddenException('Could not delete shapes: the parent blueprint component would have not enough shapes left');
    }

    if (shapes.length !== 0) {
      await this.shapeDataService.deleteMany(shapes.map(shape => shape.uuid));
    }
  }

  async updateBlueprintComponentSorting(updateElementSortingDto: UpdateElementSortingDto): Promise<UpdatedElementsDto> {
    if (updateElementSortingDto.parentComponentUuid === undefined) {
      throw new BadRequestException('Could not sort blueprint component: parent component uuid was not set');
    }

    const {
      blueprintComponents,
      shapes,
      parentBlueprintComponent,
      foundAllElements
    } = await BlueprintComponentUtils.findDirectChildElements(
      updateElementSortingDto.parentComponentUuid,
      [updateElementSortingDto.elementUuid],
      this.blueprintComponentDataService,
      {
        blueprintComponentRelations: ['shapes']
      }
    );

    if (!foundAllElements) {
      throw new NotFoundException('Could not sort blueprint component: element did not exist');
    }

    const element = (blueprintComponents.length !== 0) ? blueprintComponents[0] : shapes[0];

    const elementsToUpdate = AppUtils.updateSorting<BlueprintComponent | Shape>(
      [...parentBlueprintComponent.shapes, ...parentBlueprintComponent.children],
      {
        itemUuid: element.uuid,
        oldSorting: element.sorting,
        newSorting: updateElementSortingDto.sorting
      }
    );

    await this.blueprintComponentDataService.saveMany(elementsToUpdate.filter(element => element instanceof BlueprintComponent) as BlueprintComponent[]);
    await this.shapeDataService.saveMany(elementsToUpdate.filter(element => element instanceof Shape) as Shape[]);

    return { elementUuids: elementsToUpdate.map(element => element.uuid) };
  }

}
