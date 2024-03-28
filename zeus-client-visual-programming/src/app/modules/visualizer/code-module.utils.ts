import { CodeModuleDto } from '../../../gen/api-client';

export abstract class CodeModuleUtils {

  static buildCodeModuleDescription(codeModuleDto: CodeModuleDto): string {
    return (!codeModuleDto.description) ? 'No description' : codeModuleDto.description;
  }

}
