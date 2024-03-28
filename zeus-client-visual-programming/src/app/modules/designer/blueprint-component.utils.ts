import { BlueprintComponentDto, ComponentDto, ElementDto, ElementType, ShapeDto } from '../../../gen/api-client';
import { v4 as generateUuid } from 'uuid';

export abstract class BlueprintComponentUtils {

  static mapToComponentDto(
    blueprintComponentDto: BlueprintComponentDto,
    position: {x: number, y: number} | null = null
  ): ComponentDto {
    return {
      uuid: generateUuid(),
      name: '...',
      positionX: (position !== null) ? position.x : blueprintComponentDto.positionX,
      positionY: (position !== null) ? position.y : blueprintComponentDto.positionY,
      sorting: -1,
      isBlueprintComponentInstance: true,
      elements: blueprintComponentDto.elements.map(blueprintElement => (blueprintElement.type === ElementType.Shape)
        ? {
        ...blueprintElement,
          element: {
            ...blueprintElement.element,
            uuid: generateUuid(),
            properties: { ...(blueprintElement.element as ShapeDto).properties }
        }
      } as ElementDto
        : {
          element: BlueprintComponentUtils.mapToComponentDto(blueprintElement.element as BlueprintComponentDto),
          type: ElementType.Component
      })
    }
  }
}
