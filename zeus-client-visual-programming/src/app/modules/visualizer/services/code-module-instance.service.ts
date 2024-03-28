import {
  CodeModuleInstanceApi, CodeModuleInstanceConnectionDto,
  CodeModuleInstanceDto, CreateCodeModuleInstanceConnectionDto,
  CreateCodeModuleInstanceDto, DeleteCodeModuleInstancesDto, ErrorDto,
  TranslateCodeModuleInstancesDto
} from '../../../../gen/api-client';
import { AppUtils } from '../../../app.utils';

export abstract class CodeModuleInstanceService {
  private static codeModuleInstanceApi = new CodeModuleInstanceApi(AppUtils.getApiConfiguration());

  static async instantiateCodeModule(
    componentUuid: string,
    createCodeModuleInstanceDto: CreateCodeModuleInstanceDto
  ): Promise<CodeModuleInstanceDto> {
    return (await CodeModuleInstanceService.codeModuleInstanceApi.codeModuleInstanceControllerSave(
      componentUuid, createCodeModuleInstanceDto
    )).data;
  }

  static async translateCodeModuleInstances(
    translateCodeModuleInstancesDto: TranslateCodeModuleInstancesDto
  ): Promise<CodeModuleInstanceDto[]> {
    return (
      await CodeModuleInstanceService.codeModuleInstanceApi.codeModuleInstanceControllerTranslateCodeModuleInstances(
        translateCodeModuleInstancesDto
      )
    ).data;
  }

  static async deleteCodeModuleInstances(deleteCodeModuleInstancesDto: DeleteCodeModuleInstancesDto): Promise<void> {
    await CodeModuleInstanceService.codeModuleInstanceApi.codeModuleInstanceControllerDeleteCodeModuleInstances(deleteCodeModuleInstancesDto);
  }

  static async saveConnection(
    componentUuid: string,
    createCodeModuleInstanceConnectionDto: CreateCodeModuleInstanceConnectionDto
  ): Promise<ErrorDto[]> {
    return (
      await CodeModuleInstanceService.codeModuleInstanceApi.codeModuleInstanceControllerSaveConnection(
        componentUuid,
        createCodeModuleInstanceConnectionDto
      )
    ).data
  }

  static async getConnections(componentUuid: string): Promise<CodeModuleInstanceConnectionDto[]> {
    return (
      await CodeModuleInstanceService.codeModuleInstanceApi.codeModuleInstanceControllerGetConnections(componentUuid)
    ).data;
  }

  static async deleteConnection(codeModuleInstancesConnectionUuid: string): Promise<void> {
    await CodeModuleInstanceService.codeModuleInstanceApi.codeModuleInstanceControllerDeleteConnection(
      codeModuleInstancesConnectionUuid
    );
  }
}
