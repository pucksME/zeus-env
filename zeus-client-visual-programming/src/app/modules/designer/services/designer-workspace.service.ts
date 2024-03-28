import {
  UpdatedWorkspacePositionDto, UpdatedWorkspacePropertiesDto, UpdatedWorkspaceScaleDto,
  UpdateWorkspacePositionDto, UpdateWorkspacePropertiesDto, UpdateWorkspaceScaleDto,
  DesignerWorkspaceApi, WorkspaceDesignerDto
} from '../../../../gen/api-client';
import { AppUtils } from '../../../app.utils';

export abstract class DesignerWorkspaceService {

  private static workspaceApi = new DesignerWorkspaceApi(AppUtils.getApiConfiguration());

  static async get(workspaceUuid: string): Promise<WorkspaceDesignerDto> {
    return (await DesignerWorkspaceService.workspaceApi.designerWorkspaceControllerFindWorkspace(workspaceUuid)).data;
  }

  static async updateProperties(
    workspaceUuid: string,
    updateWorkspacePropertiesDto: UpdateWorkspacePropertiesDto
  ): Promise<UpdatedWorkspacePropertiesDto> {
    return (await DesignerWorkspaceService.workspaceApi.designerWorkspaceControllerUpdateProperties(
      workspaceUuid, updateWorkspacePropertiesDto
    )).data;
  }

  static async updatePosition(
    workspaceUuid: string,
    updateWorkspacePositionDto: UpdateWorkspacePositionDto
  ): Promise<UpdatedWorkspacePositionDto> {
    return (await DesignerWorkspaceService.workspaceApi.designerWorkspaceControllerUpdatePosition(
      workspaceUuid, updateWorkspacePositionDto
    )).data;
  }

  static async updateScale(
    workspaceUuid: string,
    updateWorkspaceScaleDto: UpdateWorkspaceScaleDto
  ): Promise<UpdatedWorkspaceScaleDto> {
    return (await DesignerWorkspaceService.workspaceApi.designerWorkspaceControllerUpdateScale(
      workspaceUuid, updateWorkspaceScaleDto
    )).data;
  }

}
