import {
  BadRequestException,
  ForbiddenException,
  Inject,
  Injectable,
  InternalServerErrorException,
  NotFoundException
} from '@nestjs/common';
import { ShapeDataService } from '../../data/shape-data/shape-data.service';
import { ComponentDataService } from '../../data/component-data/component-data.service';
import { CreateShapeDto } from '../../dtos/create-shape.dto';
import { ShapeDto } from '../../dtos/shape.dto';
import { RequestKeys } from '../../../../enums/request-keys.enum';
import { REQUEST } from '@nestjs/core';
import { Shape } from '../../entities/shape.entity';
import { ComponentUtils } from '../../component.utils';
import { UpdateShapesPropertiesDto } from '../../dtos/update-shapes-properties.dto';
import { ShapeUtils } from '../../shape.utils';
import { ScaleShapesDto } from '../../dtos/scale-shapes.dto';
import { DesignerUtils } from '../../designer.utils';
import { ReshapeShapesDto } from '../../dtos/reshape-shapes.dto';
import { TranslateShapesDto } from '../../dtos/translate-shapes.dto';
import { PositionShapesDto } from '../../dtos/position-shapes.dto';
import { AlignShapesDto } from '../../dtos/align-shapes.dto';
import { Alignment } from '../../enums/alignment.enum';
import { Component } from '../../entities/component.entity';
import { UpdateShapeSortingDto } from '../../dtos/update-shape-sorting.dto';
import { AppUtils } from '../../../../app.utils';
import { UpdateShapeNameDto } from '../../dtos/update-shape-name.dto';
import { ShapeMutationDataService } from '../../data/shape-mutation-data/shape-mutation-data.service';
import { ShapeMutation } from '../../entities/shape-mutation.entity';
import { ShapeMutationUtils } from '../../shape-mutation.utils';
import { PropertyShape } from '../../interfaces/property-shape.interface';
import { Position } from '../../enums/position.enum';

@Injectable()
export class ShapeService {

  constructor(
    @Inject(REQUEST)
    private readonly req,
    private readonly shapeDataService: ShapeDataService,
    private readonly shapeMutationsDataService: ShapeMutationDataService,
    private readonly componentDataService: ComponentDataService
  ) {
  }

  async save(componentUuid: string, createShapeDto: CreateShapeDto): Promise<ShapeDto> {
    const component: Component | undefined = this.req[RequestKeys.COMPONENT];

    if (component === undefined) {
      throw new InternalServerErrorException('Could not add shape to component: component was not injected');
    }

    if (component.shapes === undefined) {
      throw new InternalServerErrorException('Could not add shape to component: component shapes were not injected');
    }

    const shape = new Shape();
    shape.component = component;
    shape.sorting = 0;
    shape.type = createShapeDto.type;
    shape.positionX = createShapeDto.positionX;
    shape.positionY = createShapeDto.positionY;
    shape.properties = createShapeDto.properties;

    component.shapes.forEach(shape => shape.sorting++);
    await this.shapeDataService.saveMany(component.shapes);

    return ComponentUtils.buildShapeDto(await this.shapeDataService.save(shape));
  }

  async deleteShapes(shapeUuids: string[]): Promise<void> {
    if (shapeUuids.length === 0) {
      throw new NotFoundException('Could not delete shapes: no shapes found');
    }

    const shapes = await this.shapeDataService.findMany(shapeUuids, ['component', 'component.shapes']);

    if (shapes.length !== shapeUuids.length) {
      throw new NotFoundException('Could not delete shapes: at least one shape did not exist');
    }

    if (shapes.length === shapes[0].component.shapes.length) {
      throw new ForbiddenException('Could not delete shapes: component would have no shapes left');
    }

    await this.shapeDataService.deleteMany(shapeUuids);
  }

  async updateProperties(updateShapePropertiesDto: UpdateShapesPropertiesDto): Promise<ShapeDto[]> {
    const {shapeUuids, shapeMutationsUuids} = ShapeUtils.prepareShapeIdentifiers(updateShapePropertiesDto.shapeIdentifiers);

    if (shapeUuids.length === 0 && shapeMutationsUuids.length === 0) {
      throw new BadRequestException('Could not update shapes properties: no valid shape identifiers were given');
    }

    const shapes = await this.shapeDataService.findMany(shapeUuids);
    const shapeMutations = await this.shapeMutationsDataService.findMany(shapeMutationsUuids, ['shape']);

    if (shapes.length === 0 && shapeMutations.length === 0) {
      throw new NotFoundException('Could not update shape properties: no shapes found');
    }

    if (shapes.length !== shapeUuids.length || shapeMutations.length !== shapeMutationsUuids.length) {
      throw new NotFoundException('Could not update shape properties: at least one shape did not exist');
    }

    return ShapeUtils.buildCombinedShapeDtos(
      await this.shapeDataService.saveMany(ShapeUtils.updateShapesProperties<Shape>(
        shapes,
        updateShapePropertiesDto.properties,
        updateShapePropertiesDto.individualProperties
      )),
      await this.shapeMutationsDataService.saveMany(ShapeUtils.updateShapesProperties<ShapeMutation>(
        shapeMutations,
        updateShapePropertiesDto.properties,
        updateShapePropertiesDto.individualProperties
      ))
    );
  }

  async scaleShapes(scaleShapesDto: ScaleShapesDto): Promise<ShapeDto[]> {
    const {shapeUuids, shapeMutationsUuids} = ShapeUtils.prepareShapeIdentifiers(scaleShapesDto.shapeIdentifiers);

    if (shapeUuids.length === 0 && shapeMutationsUuids.length === 0) {
      throw new BadRequestException('Could not update shapes properties: no valid shape identifiers were given');
    }

    const shapes = await this.shapeDataService.findMany(shapeUuids);
    const shapeMutations = await this.shapeMutationsDataService.findMany(shapeMutationsUuids, ['shape']);

    if (shapes.length === 0 && shapeMutations.length === 0) {
      throw new NotFoundException('Could not scale shapes: no shapes found');
    }

    if (shapes.length !== shapeUuids.length || shapeMutations.length !== shapeMutationsUuids.length) {
      throw new NotFoundException('Could not scale shapes: at least one shape did not exist');
    }

    const {
      computeMinX,
      computeMinY,
      computeMaxX,
      computeMaxY
    } = DesignerUtils.computeMinMaxRequirementsForScaling(scaleShapesDto.scaleOrigin);

    const transformOrigin = { x: null, y: null };

    [...shapes,
      ...shapeMutations.map(
        shapeMutations => ShapeMutationUtils.buildShapeWithMutations(
          null,
          shapeMutations.shape,
          shapeMutations
        )
      )].forEach(shape => {
      const currentX = shape.positionX + (computeMaxX ? shape.properties.width : 0);
      const currentY = shape.positionY + (computeMaxY ? shape.properties.height : 0);

      if ((computeMinX && (transformOrigin.x === null || currentX < transformOrigin.x)) ||
        (computeMaxX && (transformOrigin.x === null || currentX > transformOrigin.x))) {
        transformOrigin.x = currentX;
      }

      if ((computeMinY && (transformOrigin.y === null || currentY < transformOrigin.y)) ||
        (computeMaxY && (transformOrigin.y === null || currentY > transformOrigin.y))) {
        transformOrigin.y = currentY;
      }
    });

    return ShapeUtils.buildCombinedShapeDtos(
      await this.shapeDataService.saveMany(ShapeUtils.scaleShapes<Shape>(
        shapes,
        transformOrigin,
        { x: scaleShapesDto.scaleX, y: scaleShapesDto.scaleY }
      )),
      await this.shapeMutationsDataService.saveMany(ShapeUtils.scaleShapes<ShapeMutation>(
        shapeMutations,
        transformOrigin,
        {x: scaleShapesDto.scaleX, y: scaleShapesDto.scaleY}
      ))
    );
  }

  async reshapeShapes(reshapeShapesDto: ReshapeShapesDto): Promise<ShapeDto[]> {
    const {shapeUuids, shapeMutationsUuids} = ShapeUtils.prepareShapeIdentifiers(reshapeShapesDto.shapeIdentifiers);
    if (reshapeShapesDto.height === undefined && reshapeShapesDto.width === undefined) {
      throw new BadRequestException('Could not reshape shapes: both height and width were not set');
    }

    if (shapeUuids.length === 0 && shapeMutationsUuids.length === 0) {
      throw new BadRequestException('Could not update shapes properties: no valid shape identifiers were given');
    }

    const shapes = await this.shapeDataService.findMany(shapeUuids);
    const shapeMutations = await this.shapeMutationsDataService.findMany(shapeMutationsUuids, ['shape']);

    if (shapes.length === 0 && shapeMutations.length === 0) {
      throw new NotFoundException('Could not reshape shapes: no shapes found');
    }

    if (shapes.length !== shapeUuids.length || shapeMutations.length !== shapeMutationsUuids.length) {
      throw new NotFoundException('Could not reshape shapes: at least one shape did not exist');
    }

    const properties = ShapeUtils.calculateProperties([
      ...shapes,
      ...shapeMutations.map(
        shapeMutations => ShapeMutationUtils.buildShapeWithMutations(null, shapeMutations.shape, shapeMutations)
      )
    ]);
    const transformOrigin = {
      x: reshapeShapesDto.width !== undefined ? properties.minX : null,
      y: reshapeShapesDto.height !== undefined ? properties.minY : null
    };

    const scaling = {
      x: reshapeShapesDto.width !== undefined
        ? reshapeShapesDto.width / properties.width
        : 1,
      y: reshapeShapesDto.height !== undefined
        ? reshapeShapesDto.height / properties.height
        : 1
    };

    return ShapeUtils.buildCombinedShapeDtos(
      await this.shapeDataService.saveMany(ShapeUtils.scaleShapes<Shape>(
        shapes,
        transformOrigin,
        scaling
      )),
      await this.shapeMutationsDataService.saveMany(ShapeUtils.scaleShapes<ShapeMutation>(
        shapeMutations,
        transformOrigin,
        scaling
      ))
    );
  }

  async translateShapes(translateShapesDto: TranslateShapesDto): Promise<ShapeDto[]> {
    const {shapeUuids, shapeMutationsUuids} = ShapeUtils.prepareShapeIdentifiers(translateShapesDto.shapeIdentifiers);

    if (shapeUuids.length === 0 && shapeMutationsUuids.length === 0) {
      throw new BadRequestException('Could not update shapes properties: no valid shape identifiers were given');
    }

    const shapes = await this.shapeDataService.findMany(shapeUuids);
    const shapeMutations = await this.shapeMutationsDataService.findMany(shapeMutationsUuids, ['shape']);

    if (shapes.length === 0 && shapeMutations.length === 0) {
      throw new NotFoundException('Could not translate shapes: no shapes found');
    }

    if (shapes.length !== shapeUuids.length || shapeMutations.length !== shapeMutationsUuids.length) {
      throw new NotFoundException('Could not translate shapes: at least one shape did not exist');
    }

    for (const shapeMutation of shapeMutations) {
      ShapeMutationUtils.setupShapeMutationPositions(shapeMutation);
    }

    for (const shape of [...shapes, ...shapeMutations] as PropertyShape[]) {
      shape.positionX += translateShapesDto.translateX;
      shape.positionY += translateShapesDto.translateY;
    }

    return ShapeUtils.buildCombinedShapeDtos(
      await this.shapeDataService.saveMany(shapes),
      await this.shapeMutationsDataService.saveMany(shapeMutations)
    );
  }

  async positionShapes(positionShapesDto: PositionShapesDto): Promise<ShapeDto[]> {
    const {shapeUuids, shapeMutationsUuids} = ShapeUtils.prepareShapeIdentifiers(positionShapesDto.shapeIdentifiers);

    if (shapeUuids.length === 0 && shapeMutationsUuids.length === 0) {
      throw new BadRequestException('Could not update shapes properties: no valid shape identifiers were given');
    }

    if (positionShapesDto.positionX === undefined && positionShapesDto.positionY === undefined) {
      throw new BadRequestException('Could not position shapes: either x or y position has to be set');
    }

    const shapes = await this.shapeDataService.findMany(shapeUuids);
    const shapeMutations = await this.shapeMutationsDataService.findMany(shapeMutationsUuids, ['shape']);

    if (shapes.length === 0 && shapeMutations.length === 0) {
      throw new NotFoundException('Could not position shapes: no shapes found');
    }

    if (shapes.length !== shapeUuids.length || shapeMutations.length !== shapeMutationsUuids.length) {
      throw new NotFoundException('Could not position shapes: at least one shape did not exist');
    }

    const properties = ShapeUtils.calculateProperties([
      ...shapes,
      ...shapeMutations.map(
        shapeMutation => ShapeMutationUtils.buildShapeWithMutations(null, shapeMutation.shape, shapeMutation)
      )
    ]);

    for (const shapeMutation of shapeMutations) {
      ShapeMutationUtils.setupShapeMutationPositions(shapeMutation);
    }

    for (const shape of [...shapes, ...shapeMutations] as PropertyShape[]) {
      if (positionShapesDto.positionX !== undefined) {
        shape.positionX = (shape.positionX - properties.minX) + positionShapesDto.positionX;
      }

      if (positionShapesDto.positionY !== undefined) {
        shape.positionY = (shape.positionY - properties.minY) + positionShapesDto.positionY;
      }
    }

    return ShapeUtils.buildCombinedShapeDtos(
      await this.shapeDataService.saveMany(shapes),
      await this.shapeMutationsDataService.saveMany(shapeMutations)
    );
  }

  async alignShapes(alignShapesDto: AlignShapesDto): Promise<ShapeDto[]> {
    const {shapeUuids, shapeMutationsUuids} = ShapeUtils.prepareShapeIdentifiers(alignShapesDto.shapeIdentifiers);

    if (shapeUuids.length === 0 && shapeMutationsUuids.length === 0) {
      throw new BadRequestException('Could not update shapes properties: no valid shape identifiers were given');
    }

    const shapes = await this.shapeDataService.findMany(shapeUuids);
    const shapeMutations = await this.shapeMutationsDataService.findMany(shapeMutationsUuids, ['shape']);

    if (shapes.length === 0 && shapeMutations.length === 0) {
      throw new NotFoundException('Could not align shapes: no shapes found');
    }

    if (shapes.length !== shapeUuids.length || shapeMutations.length !== shapeMutationsUuids.length) {
      throw new NotFoundException('Could not align shapes: at least one shape did not exist');
    }

    const anchorShape = (alignShapesDto.anchorShapeIdentifier === undefined)
      ? null
      : (!alignShapesDto.anchorShapeIdentifier.isMutated)
        ? shapes.find(shape => shape.uuid === alignShapesDto.anchorShapeIdentifier.shapeUuid)
        : shapeMutations.find(shapeMutation => shapeMutation.uuid === alignShapesDto.anchorShapeIdentifier.shapeUuid);

    if (anchorShape === undefined) {
      throw new BadRequestException('Could not align shapes: anchor shape was not included');
    }

    const anchorProperties = (anchorShape === null)
      ? ShapeUtils.calculateProperties([
        ...shapes,
        ...shapeMutations.map(
          shapeMutation => ShapeMutationUtils.buildShapeWithMutations(null, shapeMutation.shape, shapeMutation)
        )
      ])
      : ShapeUtils.calculateShapeProperties(
        (anchorShape instanceof ShapeMutation)
          ? ShapeMutationUtils.buildShapeWithMutations(null, anchorShape.shape, anchorShape)
          : anchorShape
      );

    const propertyShapes: PropertyShape[] = [...shapes, ...shapeMutations];

    if (alignShapesDto.alignment === Alignment.HORIZONTAL_LEFT) {
      for (const propertyShape of propertyShapes) {
        if (propertyShape instanceof ShapeMutation) {
          ShapeMutationUtils.setupShapeMutationPositions(propertyShape, Position.POSITION_X);
        }
        propertyShape.positionX -= propertyShape.positionX - anchorProperties.minX;
      }
    }

    if (alignShapesDto.alignment === Alignment.HORIZONTAL_RIGHT) {
      for (const propertyShape of propertyShapes) {
        if (propertyShape instanceof ShapeMutation) {
          ShapeMutationUtils.setupShapeMutationPositions(propertyShape, Position.POSITION_X);
        }
        propertyShape.positionX -= ShapeUtils.calculateShapeProperties(propertyShape).maxX - anchorProperties.maxX;
      }
    }

    if (alignShapesDto.alignment === Alignment.HORIZONTAL_CENTER) {
      for (const propertyShape of propertyShapes) {
        if (propertyShape instanceof ShapeMutation) {
          ShapeMutationUtils.setupShapeMutationPositions(propertyShape, Position.POSITION_X);
        }
        const shapeProperties = ShapeUtils.calculateShapeProperties(propertyShape);
        propertyShape.positionX -= shapeProperties.minX - anchorProperties.minX - (
          (anchorProperties.width - shapeProperties.width) / 2
        );
      }
    }

    if (alignShapesDto.alignment === Alignment.VERTICAL_TOP) {
      for (const propertyShape of propertyShapes) {
        if (propertyShape instanceof ShapeMutation) {
          ShapeMutationUtils.setupShapeMutationPositions(propertyShape, Position.POSITION_Y);
        }
        propertyShape.positionY -= propertyShape.positionY - anchorProperties.minY;
      }
    }

    if (alignShapesDto.alignment === Alignment.VERTICAL_BOTTOM) {
      for (const propertyShape of propertyShapes) {
        if (propertyShape instanceof ShapeMutation) {
          ShapeMutationUtils.setupShapeMutationPositions(propertyShape, Position.POSITION_Y);
        }
        propertyShape.positionY -= ShapeUtils.calculateShapeProperties(propertyShape).maxY - anchorProperties.maxY;
      }
    }

    if (alignShapesDto.alignment === Alignment.VERTICAL_CENTER) {
      for (const propertyShape of propertyShapes) {
        if (propertyShape instanceof ShapeMutation) {
          ShapeMutationUtils.setupShapeMutationPositions(propertyShape, Position.POSITION_Y);
        }
        const shapeProperties = ShapeUtils.calculateShapeProperties(propertyShape);
        propertyShape.positionY -= shapeProperties.minY - anchorProperties.minY - (
          (anchorProperties.height - shapeProperties.height) / 2
        );
      }
    }

    return ShapeUtils.buildCombinedShapeDtos(
      await this.shapeDataService.saveMany(shapes),
      await this.shapeMutationsDataService.saveMany(shapeMutations)
    );
  }

  async updateSorting(shapeUuid: string, updateShapeSortingDto: UpdateShapeSortingDto): Promise<ShapeDto> {
    const shape: Shape | undefined = this.req[RequestKeys.SHAPE];

    if (shape === undefined) {
      throw new InternalServerErrorException('Could not update shape sorting: shape was not injected');
    }

    const shapesToUpdate = AppUtils.updateSorting(
      shape.component.shapes,
      {
        itemUuid: shape.uuid,
        oldSorting: shape.sorting,
        newSorting: updateShapeSortingDto.sorting
      }
    );

    await this.shapeDataService.saveMany(shapesToUpdate);
    return ComponentUtils.buildShapeDto(shape);
  }

  async updateName(shapeUuid: string, updateShapeNameDto: UpdateShapeNameDto): Promise<ShapeDto> {
    const shape: Shape | undefined = this.req[RequestKeys.SHAPE];

    if (shape === undefined) {
      throw new InternalServerErrorException('Could not update shape name: shape was not injected');
    }

    shape.name = (updateShapeNameDto.name === undefined) ? null : updateShapeNameDto.name;

    return ComponentUtils.buildShapeDto(await this.shapeDataService.save(shape));
  }

}
