import { CodeModuleInstance } from './entities/code-module-instance.entity';
import { CodeModuleInstanceDto } from './dtos/code-module-instance.dto';
import { CodeModulesDto } from '../../../gen/thunder-api-client';
import { CodeModuleUtils } from './code-module.utils';

export abstract class CodeModuleInstanceUtils {

  static buildCodeModuleInstanceDto(
    codeModuleInstanceEntity: CodeModuleInstance,
    codeModulesDtos: CodeModulesDto[] = []
  ): CodeModuleInstanceDto {
    const codeModulesDto = codeModulesDtos.find(
      codeModulesDto => codeModulesDto.uuid === codeModuleInstanceEntity.module.uuid
    );

    return {
      uuid: codeModuleInstanceEntity.uuid,
      flowDescription: codeModuleInstanceEntity.flowDescription,
      positionX: codeModuleInstanceEntity.positionX,
      positionY: codeModuleInstanceEntity.positionY,
      codeModule: (codeModulesDto !== undefined) ? CodeModuleUtils.buildCodeModuleDtos(codeModulesDto)[0] : null
    }
  }

  static calculateTransformerOrigin(codeModuleInstanceEntities: CodeModuleInstance[]): {x: number, y: number} {
    const transformOrigin = {x: Number.MAX_VALUE, y: Number.MAX_VALUE};

    for (const codeModuleInstance of codeModuleInstanceEntities) {
      if (codeModuleInstance.positionX < transformOrigin.x) {
        transformOrigin.x = codeModuleInstance.positionX;
      }

      if (codeModuleInstance.positionY < transformOrigin.y) {
        transformOrigin.y = codeModuleInstance.positionY;
      }
    }

    return transformOrigin;
  }

}
