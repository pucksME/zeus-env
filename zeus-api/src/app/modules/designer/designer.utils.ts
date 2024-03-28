import { ScaleOrigin } from './enums/scale-origin.enum';
import { ElementsPropertiesDto } from './dtos/elements-properties.dto';

export abstract class DesignerUtils {

  static computeMinMaxRequirementsForScaling(scaleOrigin: ScaleOrigin): {
    computeMinX: boolean,
    computeMinY: boolean,
    computeMaxX: boolean,
    computeMaxY: boolean
  } {
    return {
      computeMinX: scaleOrigin === ScaleOrigin.RIGHT ||
        scaleOrigin === ScaleOrigin.TOP_RIGHT ||
        scaleOrigin === ScaleOrigin.BOTTOM_RIGHT,
      computeMinY: scaleOrigin === ScaleOrigin.BOTTOM ||
        scaleOrigin === ScaleOrigin.BOTTOM_LEFT ||
        scaleOrigin === ScaleOrigin.BOTTOM_RIGHT,
      computeMaxX: scaleOrigin === ScaleOrigin.LEFT ||
        scaleOrigin === ScaleOrigin.TOP_LEFT ||
        scaleOrigin === ScaleOrigin.BOTTOM_LEFT,
      computeMaxY: scaleOrigin === ScaleOrigin.TOP ||
        scaleOrigin === ScaleOrigin.TOP_LEFT ||
        scaleOrigin === ScaleOrigin.TOP_RIGHT
    }
  }

  static computeTransformOriginForScaling(
    elementsProperties: ElementsPropertiesDto,
    scaleOrigin: ScaleOrigin
  ): { x: number, y: number } {
    const {
      computeMinX,
      computeMinY
    } = DesignerUtils.computeMinMaxRequirementsForScaling(scaleOrigin);

    return {
      x: (computeMinX)
        ? elementsProperties.x
        : elementsProperties.x + elementsProperties.width,
      y: (computeMinY)
        ? elementsProperties.y
        : elementsProperties.y + elementsProperties.height
    };
  }
}
