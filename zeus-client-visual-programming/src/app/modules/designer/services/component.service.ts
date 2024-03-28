import {
  AlignElementsDto,
  ComponentApi,
  ComponentDto,
  CreateComponentDto, DeleteElementsDto, PositionElementsDto, ResetElementsMutationsDto, ReshapeElementsDto,
  ScaleElementsDto, TranslateElementsDto,
  UpdateComponentNameDto,
  UpdateComponentPositionDto,
  UpdatedComponentPositionDto, UpdatedElementsDto, UpdateElementSortingDto, UpdateElementsPropertiesDto
} from '../../../../gen/api-client';
import { AppUtils } from '../../../app.utils';

export abstract class ComponentService {

  private static componentApi = new ComponentApi(AppUtils.getApiConfiguration());

  static async saveComponent(viewUuid: string, createComponentDto: CreateComponentDto): Promise<ComponentDto> {
    return (await ComponentService.componentApi.componentControllerSave(viewUuid, createComponentDto)).data;
  }

  static async updateComponentPosition(
    componentUuid: string, updateComponentPositionDto: UpdateComponentPositionDto
  ): Promise<UpdatedComponentPositionDto> {
    return (await ComponentService.componentApi.componentControllerUpdatePosition(
      componentUuid, updateComponentPositionDto
    )).data;
  }

  static async scaleElements(scaleElementsDto: ScaleElementsDto): Promise<UpdatedElementsDto> {
    return (await ComponentService.componentApi.componentControllerScaleComponents(scaleElementsDto)).data;
  }

  static async translateElements(translateElementsDto: TranslateElementsDto): Promise<UpdatedElementsDto> {
    return (await ComponentService.componentApi.componentControllerTranslateComponents(translateElementsDto)).data;
  }

  static async deleteElements(deleteElementsDto: DeleteElementsDto): Promise<void> {
    await ComponentService.componentApi.componentControllerDeleteComponents(deleteElementsDto);
  }

  static async positionElements(positionElementsDto: PositionElementsDto): Promise<UpdatedElementsDto> {
    return (await ComponentService.componentApi.componentControllerPositionComponents(positionElementsDto)).data;
  }

  static async reshapeElements(reshapeElementsDto: ReshapeElementsDto): Promise<UpdatedElementsDto> {
    return (await ComponentService.componentApi.componentControllerReshapeComponents(reshapeElementsDto)).data;
  }

  static async updateElementsProperties(
    updateElementsPropertiesDto: UpdateElementsPropertiesDto
  ): Promise<UpdatedElementsDto> {
    return (await ComponentService.componentApi.componentControllerUpdateComponentsProperties(
      updateElementsPropertiesDto
    )).data;
  }

  static async alignElements(alignElementsDto: AlignElementsDto): Promise<UpdatedElementsDto> {
    return (await ComponentService.componentApi.componentControllerAlignComponents(alignElementsDto)).data;
  }

  static async updateElementSorting(updateElementSortingDto: UpdateElementSortingDto): Promise<UpdatedElementsDto> {
    return (await ComponentService.componentApi.componentControllerUpdateSorting(updateElementSortingDto)).data;
  }

  static async updateComponentName(componentUuid: string, updateComponentNameDto: UpdateComponentNameDto): Promise<ComponentDto> {
    return (await ComponentService.componentApi.componentControllerUpdateName(
      componentUuid,
      {
        ...updateComponentNameDto, name: (updateComponentNameDto.name === '')
          ? null
          : updateComponentNameDto.name
      }
    )).data;
  }

  static async saveWithShape(shapeUuid: string): Promise<ComponentDto> {
    return (await ComponentService.componentApi.componentControllerSaveWithShape(shapeUuid)).data;
  }

  static async resetMutations(resetElementsMutationsDto: ResetElementsMutationsDto): Promise<void> {
    return (await ComponentService.componentApi.componentControllerResetMutations(resetElementsMutationsDto)).data;
  }

}
