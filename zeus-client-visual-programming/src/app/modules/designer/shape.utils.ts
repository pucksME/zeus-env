import { AllShapesProperties } from '../../types/all-shapes-properties.type';
import {
  FontFamily,
  FontStyle,
  ShapeDto, ShapeIdentifierDto,
  ShapeType,
  TextAlign,
  TextDecoration,
  TextPropertiesDto,
  TextTransform
} from '../../../gen/api-client';
import Konva from 'konva';
import { CSSProperties } from 'react';
import { useStore } from '../../store';

export type SharedProperties = { properties: Partial<AllShapesProperties>, compatible: boolean };

export abstract class ShapeUtils {

  static getSharedProperties(shapeDtos: ShapeDto[], propertyKeys: (keyof AllShapesProperties)[]): SharedProperties {

    const result: SharedProperties = { properties: {}, compatible: true };

    if (shapeDtos.length === 0) {
      return { ...result, compatible: false };
    }

    for (const shape of shapeDtos) {

      if (!result.compatible) {
        continue;
      }

      for (const propertyKey of propertyKeys) {
        if (result.compatible && !Object.keys(shape.properties).includes(propertyKey)) {
          result.compatible = false;
        }

        if (result.properties[propertyKey] === undefined) {
          result.properties[propertyKey as string] = (Array.isArray(shape.properties[propertyKey]))
            ? [...shape.properties[propertyKey]]
            : shape.properties[propertyKey];
          continue;
        }

        if (Array.isArray(result.properties[propertyKey]) && Array.isArray(shape.properties[propertyKey])) {
          for (let i = 0; i < (result.properties[propertyKey] as unknown[]).length; i++) {

            if ((result.properties[propertyKey] as unknown[])[i] === null) {
              continue;
            }

            if ((result.properties[propertyKey] as unknown[])[i] !== (shape.properties[propertyKey] as unknown[])[i]) {
              (result.properties[propertyKey] as unknown[])[i] = null;
            }
          }
          continue;
        }

        if (result.properties[propertyKey] !== shape.properties[propertyKey]) {
          result.properties[propertyKey as string] = null;
        }
      }

    }

    return result;
  }

  static mapFontFamilyToString(fontFamily: FontFamily): string {
    switch (fontFamily) {
      case FontFamily.Arial: return 'Arial';
      default: return 'Arial';
    }
  }

  static mapFontStyleToString(fontStyle: FontStyle): string {
    switch (fontStyle) {
      case FontStyle.Normal: return 'normal';
      case FontStyle.Bold: return 'bold';
      case FontStyle.Italic: return 'italic';
      default: return 'normal';
    }
  }

  static mapTextDecorationToString(textDecoration: TextDecoration): string {
    switch (textDecoration) {
      case TextDecoration.None: return '';
      case TextDecoration.Underline: return 'underline';
      case TextDecoration.StrikeThrough: return 'line-through';
      default: return '';
    }
  }

  static mapTextTransformToString(textTransform: TextTransform): string {
    switch (textTransform) {
      case TextTransform.None: return 'normal';
      case TextTransform.Uppercase: return 'small-caps';
      default: return 'normal';
    }
  }

  static mapTextTransformToCssString(textTransform: TextTransform): 'none' | 'uppercase' {
    switch (textTransform) {
      case TextTransform.None: return 'none';
      case TextTransform.Uppercase: return 'uppercase';
      default: return 'none';
    }
  }

  static mapTextAlignToString(textAlign: TextAlign): string {
    switch (textAlign) {
      case TextAlign.Left: return 'left';
      case TextAlign.Center: return 'center';
      case TextAlign.Right: return 'right';
      default: return 'left';
    }
  }

  static buildShapeConfig(shapeDto: ShapeDto, borderScale = 1): Konva.ShapeConfig {
    return {
      height: shapeDto.properties.height,
      width: shapeDto.properties.width,
      fillEnabled: shapeDto.properties.backgroundColorEnabled,
      fill: shapeDto.properties.backgroundColor,
      strokeEnabled: shapeDto.properties.borderEnabled,
      stroke: shapeDto.properties.borderColor,
      strokeWidth: shapeDto.properties.borderWidth * borderScale,
      fillAfterStrokeEnabled: true,
      strokeScaleEnabled: false,
      shadowForStrokeEnabled: false,
      shadowEnabled: shapeDto.properties.shadowEnabled,
      shadowColor: shapeDto.properties.shadowColor,
      shadowOffsetX: shapeDto.properties.shadowX,
      shadowOffsetY: shapeDto.properties.shadowY,
      shadowBlur: shapeDto.properties.shadowBlur,
      opacity: shapeDto.properties.opacity,
      visible: shapeDto.properties.visible
    }
  }

  static buildTextConfig(shapeDto: ShapeDto, borderScale = 1): Konva.TextConfig | null {

    if (shapeDto.type !== ShapeType.Text) {
      return null;
    }

    const properties = shapeDto.properties as TextPropertiesDto;

    return {
      ...ShapeUtils.buildShapeConfig(shapeDto, borderScale),
      fontFamily: ShapeUtils.mapFontFamilyToString(properties.fontFamily),
      fontSize: properties.fontSize,
      fontStyle: ShapeUtils.mapFontStyleToString(properties.fontStyle),
      textDecoration: ShapeUtils.mapTextDecorationToString(properties.textDecoration),
      // fontVariant: ShapeUtils.mapTextTransformToString(properties.textTransform),
      align: ShapeUtils.mapTextAlignToString(properties.textAlign)
    }
  }

  static mapTextPropertiesToCssProperties(textPropertiesDto: TextPropertiesDto): CSSProperties {
    return {
      fontFamily: ShapeUtils.mapFontFamilyToString(textPropertiesDto.fontFamily),
      fontSize: textPropertiesDto.fontSize,
      fontWeight: ShapeUtils.mapFontStyleToString(textPropertiesDto.fontStyle) as any,
      textDecoration: ShapeUtils.mapTextDecorationToString(textPropertiesDto.textDecoration),
      textTransform: ShapeUtils.mapTextTransformToCssString(textPropertiesDto.textTransform),
      color: textPropertiesDto.backgroundColor,
      WebkitTextStrokeWidth: (!textPropertiesDto.borderEnabled) ? 0 : textPropertiesDto.borderWidth * useStore.getState().designerStageProperties.scale,
      WebkitTextStrokeColor: textPropertiesDto.borderColor
    }
  }

  static buildShapeIdentifierDtos(shapeDtos: ShapeDto[]): ShapeIdentifierDto[] {
    return shapeDtos.map(ShapeUtils.buildShapeIdentifierDto);
  }

  static buildShapeIdentifierDto(shapeDto: ShapeDto): ShapeIdentifierDto {
    return {
      shapeUuid: shapeDto.uuid,
      isMutated: shapeDto.isMutated
    }
  }
}
