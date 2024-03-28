import { VisualizerWorkspaceApi, WorkspaceVisualizerDto } from '../../../../gen/api-client';
import { AppUtils } from '../../../app.utils';

export abstract class VisualizerWorkspaceService {

  private static visualizerWorkspaceApi = new VisualizerWorkspaceApi(AppUtils.getApiConfiguration());

  static async getWithComponentUuid(componentUuid: string): Promise<WorkspaceVisualizerDto> {
    return (
      await VisualizerWorkspaceService.visualizerWorkspaceApi.visualizerWorkspaceControllerFindWorkspaceByComponentUuid(
        componentUuid
      )
    ).data;
  }
}
