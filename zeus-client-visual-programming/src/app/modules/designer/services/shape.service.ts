import {
  AlignShapesDto,
  CreateShapeDto, DeleteShapesDto, PositionShapesDto, ReshapeShapesDto,
  ScaleShapesDto,
  ShapeApi,
  ShapeDto,
  TranslateShapesDto, UpdateShapeNameDto,
  UpdateShapeSortingDto, UpdateShapesPropertiesDto
} from '../../../../gen/api-client';
import { AppUtils } from '../../../app.utils';

export abstract class ShapeService {

  private static shapeApi = new ShapeApi(AppUtils.getApiConfiguration());

  static async saveShape(componentUuid: string, createShapeDto: CreateShapeDto): Promise<ShapeDto> {
    return (await ShapeService.shapeApi.shapeControllerSave(componentUuid, createShapeDto)).data;
  }

  static async scaleShapes(scaleShapesDto: ScaleShapesDto): Promise<ShapeDto[]> {
    return (await ShapeService.shapeApi.shapeControllerScaleShapes(scaleShapesDto)).data;
  }

  static async translateShapes(translateShapesDto: TranslateShapesDto): Promise<ShapeDto[]> {
    return (await ShapeService.shapeApi.shapeControllerTranslateShapes(translateShapesDto)).data;
  }

  static async updateShapeSorting(shapeUuid: string, updateShapeSortingDto: UpdateShapeSortingDto): Promise<ShapeDto> {
    return (await ShapeService.shapeApi.shapeControllerUpdateSorting(shapeUuid, updateShapeSortingDto)).data;
  }

  static async positionShapes(positionShapesDto: PositionShapesDto): Promise<ShapeDto[]> {
    return (await ShapeService.shapeApi.shapeControllerPositionShapes(positionShapesDto)).data;
  }

  static async deleteShapes(deleteShapesDto: DeleteShapesDto): Promise<void> {
    return (await ShapeService.shapeApi.shapeControllerDeleteShapes(deleteShapesDto)).data;
  }

  static async reshapeShapes(reshapeShapesDto: ReshapeShapesDto): Promise<ShapeDto[]> {
    return (await ShapeService.shapeApi.shapeControllerReshapeShapes(reshapeShapesDto)).data;
  }

  static async alignShapes(alignShapesDto: AlignShapesDto): Promise<ShapeDto[]> {
    return (await ShapeService.shapeApi.shapeControllerAlignShapes(alignShapesDto)).data;
  }

  static async updateShapesProperties(updateShapesPropertiesDto: UpdateShapesPropertiesDto): Promise<ShapeDto[]> {
    return (await ShapeService.shapeApi.shapeControllerUpdateShapesProperties(updateShapesPropertiesDto)).data;
  }

  static async updateShapeName(shapeUuid: string, updateShapeNameDto: UpdateShapeNameDto): Promise<ShapeDto> {
    return (await ShapeService.shapeApi.shapeControllerUpdateName(
      shapeUuid,
      {
        ...updateShapeNameDto, name: (updateShapeNameDto.name === '')
          ? null
          : updateShapeNameDto.name
      }
    )).data
  }

}
