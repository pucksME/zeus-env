import { Shape } from './entities/shape.entity';
import { PropertiesDto } from './interfaces/shape-properties/properties.dto';
import { ComponentProperties } from './interfaces/component-properties.interface';
import { IndividualShapePropertiesDto } from './dtos/individual-shape-properties.dto';
import { PropertyShape } from './interfaces/property-shape.interface';
import { ShapeMutation } from './entities/shape-mutation.entity';
import { ShapeType } from './enums/shape-type.enum';
import { ShapeIdentifierDto } from './dtos/shape-identifier.dto';
import { ShapeDto } from './dtos/shape.dto';
import { ComponentUtils } from './component.utils';

export abstract class ShapeUtils {

  static updateShapesProperties<T extends PropertyShape>(
    propertyShapes: T[],
    properties: PropertiesDto,
    individualProperties: IndividualShapePropertiesDto[] = []
  ): T[] {

    const individualPropertiesDict: {[uuid: string]: PropertiesDto} = {};

    if (individualProperties !== undefined) {
      individualProperties.forEach(
        individualProperty => individualPropertiesDict[individualProperty.shapeUuid] = individualProperty.properties
      );
    }

    for (const propertyShape of propertyShapes) {
      for (const propertyKey of Object.keys(properties)) {
        if (propertyShape.properties[propertyKey] === undefined &&
          !((propertyShape instanceof ShapeMutation) &&
            (propertyShape as ShapeMutation).shape.properties[propertyKey] !== undefined)) {
          continue;
        }
        ShapeUtils.updateShapeProperty(propertyShape, propertyKey, properties[propertyKey]);
      }

      const individualShapeProperties = individualPropertiesDict[propertyShape.uuid];

      if (individualShapeProperties === undefined) {
        continue;
      }

      for (const individualPropertyKey of Object.keys(individualShapeProperties)) {
        if (propertyShape.properties[individualPropertyKey] === undefined) {
          continue;
        }
        ShapeUtils.updateShapeProperty(propertyShape, individualPropertyKey, individualShapeProperties[individualPropertyKey]);
      }
    }
    return propertyShapes;
  }

  private static updateShapeProperty(
    propertyShape: PropertyShape,
    propertyKey: string,
    propertyValue: unknown
  ): PropertyShape {
    // TODO: use enum
    if (propertyKey === 'borderRadius') {
      if (propertyShape.properties[propertyKey] === undefined) {
        propertyShape.properties[propertyKey] = [0, 0, 0, 0];
      }

      propertyShape.properties[propertyKey] =
        (propertyValue as number[]).map(
          (borderRadiusCorner, index) => (borderRadiusCorner !== null)
            ? borderRadiusCorner
            : propertyShape.properties[propertyKey][index]
        );
      return propertyShape;
    }

    propertyShape.properties[propertyKey] = propertyValue;
    return propertyShape;
  }

  static scaleShapes<T extends PropertyShape>(
    shapeEntities: T[],
    transformOrigin: { x: number, y: number },
    scaling: { x: number, y: number }
  ): T[] {
    for (const shape of shapeEntities) {
      const shapeDimensionsScalable = (shape instanceof Shape && shape.type !== ShapeType.TEXT) ||
        (shape instanceof ShapeMutation && shape.shape.type !== ShapeType.TEXT);

      if (transformOrigin.x !== null) {
        if (shape instanceof ShapeMutation && shape.positionX === null) {
          shape.positionX = (shape as ShapeMutation).shape.positionX;
        }

        shape.positionX = ((shape.positionX - transformOrigin.x) * scaling.x) + transformOrigin.x;

        if (shapeDimensionsScalable) {
          if (shape instanceof ShapeMutation && shape.properties.width === undefined) {
            shape.properties.width = (shape as ShapeMutation).shape.properties.width;
          }
          shape.properties.width *= scaling.x;
        }
      }

      if (transformOrigin.y !== null) {
        if (shape instanceof ShapeMutation && shape.positionY === null) {
          shape.positionY = (shape as ShapeMutation).shape.positionY;
        }

        shape.positionY = ((shape.positionY - transformOrigin.y) * scaling.y) + transformOrigin.y;

        if (shapeDimensionsScalable) {
          if (shape instanceof ShapeMutation && shape.properties.height === undefined) {
            shape.properties.height = (shape as ShapeMutation).shape.properties.height;
          }
          shape.properties.height *= scaling.y;
        }
      }
    }
    return shapeEntities;
  }

  static calculateProperties<T extends PropertyShape>(shapeEntities: T[]): ComponentProperties {
    const minPosition = { minX: Number.MAX_VALUE, minY: Number.MAX_VALUE };
    const maxPosition = { maxX: Number.MIN_VALUE, maxY: Number.MIN_VALUE };

    for (const shape of shapeEntities) {
      const shapeXStart = shape.positionX;
      const shapeYStart = shape.positionY;
      // TODO: support other shapes
      const shapeXEnd = shapeXStart + shape.properties.width;
      const shapeYEnd = shapeYStart + shape.properties.height;

      if (shapeXStart < minPosition.minX) {
        minPosition.minX = shapeXStart;
      }

      if (shapeYStart < minPosition.minY) {
        minPosition.minY = shapeYStart;
      }

      if (shapeXEnd > maxPosition.maxX) {
        maxPosition.maxX = shapeXEnd;
      }

      if (shapeYEnd > maxPosition.maxY) {
        maxPosition.maxY = shapeYEnd;
      }
    }

    return {
      height: maxPosition.maxY - minPosition.minY,
      width: maxPosition.maxX - minPosition.minX,
      ...minPosition,
      ...maxPosition
    };
  }

  static calculateShapeProperties<T extends PropertyShape>(
    propertyShape: T
  ): ComponentProperties {
    const height = (propertyShape instanceof ShapeMutation && propertyShape.properties.height === undefined)
      ? propertyShape.shape.properties.height
      : propertyShape.properties.height;
    const width = (propertyShape instanceof ShapeMutation && propertyShape.properties.width === undefined)
      ? propertyShape.shape.properties.width
      : propertyShape.properties.width;
    return {
      height,
      width,
      minX: propertyShape.positionX,
      minY: propertyShape.positionY,
      maxX: propertyShape.positionX + width,
      maxY: propertyShape.positionY + height
    }
  }

  static prepareShapeIdentifiers(
    shapeIdentifierDtos: ShapeIdentifierDto[]
  ): {shapeUuids: string[], shapeMutationsUuids: string[]} {
    const shapeUuids: string[] = [];
    const shapeMutationsUuids: string[] = [];
    for (const shapeIdentifierDto of shapeIdentifierDtos) {
      if (shapeIdentifierDto.isMutated) {
        shapeMutationsUuids.push(shapeIdentifierDto.shapeUuid);
        continue;
      }
      shapeUuids.push(shapeIdentifierDto.shapeUuid);
    }
    return {shapeUuids, shapeMutationsUuids};
  }

  static buildCombinedShapeDtos(shapeEntities: Shape[], shapeMutationsEntities: ShapeMutation[]): ShapeDto[] {
    return [
      ...shapeEntities.map(
        shape => ComponentUtils.buildShapeDto(shape, ComponentUtils.buildShapeName(shape))
      ),
      ...shapeMutationsEntities.map(
        shapeMutationsEntity => ComponentUtils.buildShapeDto(
          shapeMutationsEntity.shape,
          ComponentUtils.buildShapeName(shapeMutationsEntity.shape),
          shapeMutationsEntity
        )
      )
    ]
  }
}
