import {
  AlignElementsDto,
  BlueprintComponentApi,
  BlueprintComponentDto,
  ComponentDto, DeleteElementsDto,
  InstantiateBlueprintComponentDto, PositionElementsDto,
  ReshapeElementsDto,
  ScaleElementsDto, TranslateElementsDto,
  UpdateBlueprintComponentNameDto,
  UpdatedElementsDto, UpdateElementSortingDto, UpdateElementsPropertiesDto
} from '../../../../gen/api-client';
import { AppUtils } from '../../../app.utils';

export abstract class BlueprintComponentService {

  private static blueprintComponentApi = new BlueprintComponentApi(AppUtils.getApiConfiguration());

  static async saveBlueprintComponent(componentUuid: string): Promise<BlueprintComponentDto> {
    return (await BlueprintComponentService.blueprintComponentApi.blueprintComponentControllerSave(
      {componentUuid}
    )).data;
  }

  static async getAllInWorkspace(workspaceUuid: string): Promise<BlueprintComponentDto[]> {
    return (await BlueprintComponentService.blueprintComponentApi.blueprintComponentControllerFindWorkspace(
      workspaceUuid
    )).data;
  }

  static async instantiateBlueprintComponent(
    instantiateBlueprintComponentDto: InstantiateBlueprintComponentDto
  ): Promise<ComponentDto> {
    return (await BlueprintComponentService.blueprintComponentApi.blueprintComponentControllerInstantiate(
      instantiateBlueprintComponentDto
    )).data;
  }

  static async updateName(
    blueprintComponentUuid: string,
    updateBlueprintComponentNameDto: UpdateBlueprintComponentNameDto
  ): Promise<BlueprintComponentDto> {
    return (await BlueprintComponentService.blueprintComponentApi.blueprintComponentControllerUpdateName(
      blueprintComponentUuid,
      updateBlueprintComponentNameDto
    )).data;
  }

  static async scaleBlueprintElements(scaleElementsDto: ScaleElementsDto): Promise<UpdatedElementsDto> {
    return (await BlueprintComponentService.blueprintComponentApi.blueprintComponentControllerScaleBlueprintComponents(
      scaleElementsDto
    )).data;
  }

  static async reshapeBlueprintElements(reshapeElementsDto: ReshapeElementsDto): Promise<UpdatedElementsDto> {
    return (await BlueprintComponentService.blueprintComponentApi.blueprintComponentControllerReshapeBlueprintComponents(
      reshapeElementsDto
    )).data;
  }

  static async translateBlueprintElements(translateElementsDto: TranslateElementsDto): Promise<UpdatedElementsDto> {
    return (await BlueprintComponentService.blueprintComponentApi.blueprintComponentControllerTranslateBlueprintComponents(
      translateElementsDto
    )).data;
  }

  static async positionBlueprintElements(positionBlueprintElements: PositionElementsDto): Promise<UpdatedElementsDto> {
    return (await BlueprintComponentService.blueprintComponentApi.blueprintComponentControllerPositionBlueprintComponents(
      positionBlueprintElements
    )).data;
  }

  static async alignBlueprintElements(alignElementsDto: AlignElementsDto): Promise<UpdatedElementsDto> {
    return (await BlueprintComponentService.blueprintComponentApi.blueprintComponentControllerAlignBlueprintComponents(
      alignElementsDto
    )).data;
  }

  static async updateBlueprintElementsProperties(
    updateElementsPropertiesDto: UpdateElementsPropertiesDto
  ): Promise<UpdatedElementsDto> {
    return (await BlueprintComponentService.blueprintComponentApi.blueprintComponentControllerUpdateBlueprintComponentsProperties(
      updateElementsPropertiesDto
    )).data;
  }

  static async deleteBlueprintElements(deleteElementsDto: DeleteElementsDto): Promise<void> {
    return (await BlueprintComponentService.blueprintComponentApi.blueprintComponentControllerDeleteBlueprintComponents(
      deleteElementsDto
    )).data;
  }

  static async updateBlueprintElementSorting(
    updateElementSortingDto: UpdateElementSortingDto
  ): Promise<UpdatedElementsDto> {
    return (await BlueprintComponentService.blueprintComponentApi.blueprintComponentControllerUpdateSorting(
      updateElementSortingDto
    )).data;
  }

}
