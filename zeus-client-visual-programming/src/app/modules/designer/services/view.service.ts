import {
  CreateViewDto, PositionViewDto, ReshapeViewDto,
  ScaleViewDto,
  TranslateViewDto,
  UpdateViewNameDto,
  ViewApi,
  ViewDto
} from '../../../../gen/api-client';
import { AppUtils } from '../../../app.utils';

export abstract class ViewService {

  private static viewApi = new ViewApi(AppUtils.getApiConfiguration());

  public static async saveView(workspaceUuid: string, createViewDto: CreateViewDto): Promise<ViewDto> {
    return (await ViewService.viewApi.viewControllerSave(workspaceUuid, createViewDto)).data;
  }

  static async updateViewName(viewUuid: string, updateViewNameDto: UpdateViewNameDto): Promise<ViewDto> {
    return (await ViewService.viewApi.viewControllerUpdateName(
      viewUuid,
      {
        ...updateViewNameDto, name: (updateViewNameDto.name === '')
          ? null
          : updateViewNameDto.name
      }
    )).data;
  }

  static async setRootView(viewUuid: string): Promise<ViewDto[]> {
    return (await ViewService.viewApi.viewControllerSetRootView(viewUuid)).data;
  }

  public static async scaleView(viewUuid: string, scaleViewDto: ScaleViewDto): Promise<ViewDto> {
    return (await ViewService.viewApi.viewControllerScaleView(viewUuid, scaleViewDto)).data;
  }

  public static async translateView(viewUuid: string, translateViewDto: TranslateViewDto): Promise<ViewDto> {
    return (await ViewService.viewApi.viewControllerTranslateView(viewUuid, translateViewDto)).data;
  }

  public static async reshapeView(viewUuid: string, reshapeViewDto: ReshapeViewDto): Promise<ViewDto> {
    return (await ViewService.viewApi.viewControllerReshapeView(viewUuid, reshapeViewDto)).data;
  }

  public static async positionView(viewUuid: string, positionViewDto: PositionViewDto): Promise<ViewDto> {
    return (await ViewService.viewApi.viewControllerPositionView(viewUuid, positionViewDto)).data;
  }

  public static async deleteView(viewUuid: string): Promise<void> {
    return (await ViewService.viewApi.viewControllerDeleteView(viewUuid)).data;
  }

}
