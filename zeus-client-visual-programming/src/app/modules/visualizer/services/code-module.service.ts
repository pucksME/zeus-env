import { CheckCodeDto, CodeModuleApi, CodeModuleDto, ErrorDto, UpdateCodeModuleDto } from '../../../../gen/api-client';
import { AppUtils } from '../../../app.utils';

export abstract class CodeModuleService {

  private static codeModuleApi = new CodeModuleApi(AppUtils.getApiConfiguration());

  static async getProjectCodeModules(projectUuid: string): Promise<CodeModuleDto[]> {
    return (await CodeModuleService.codeModuleApi.codeModuleControllerFindProjectCodeModules(projectUuid)).data;
  }

  static async getSystemCodeModules(): Promise<CodeModuleDto[]> {
    return (await CodeModuleService.codeModuleApi.codeModuleControllerFindSystemCodeModules()).data;
  }

  static async checkCode(checkCodeDto: CheckCodeDto): Promise<ErrorDto[]> {
    return (await CodeModuleService.codeModuleApi.codeModuleControllerCheckCode(checkCodeDto)).data;
  }

  static async save(projectUuid: string): Promise<CodeModuleDto> {
    return (await CodeModuleService.codeModuleApi.codeModuleControllerSave(
      projectUuid,
      {code: ''}
    )).data;
  }

  static async update(codeModuleUuid: string, updateCodeModuleDto: UpdateCodeModuleDto): Promise<ErrorDto[]> {
    return (await CodeModuleService.codeModuleApi.codeModuleControllerUpdate(codeModuleUuid, updateCodeModuleDto)).data
  }

  static async delete(codeModuleUuid: string): Promise<void> {
    return (await CodeModuleService.codeModuleApi.codeModuleControllerDelete(codeModuleUuid)).data;
  }

}
