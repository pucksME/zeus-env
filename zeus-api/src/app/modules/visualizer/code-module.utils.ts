import { CodeModule } from './entities/code-module.entity';
import { CodeModuleDto } from './dtos/code-module.dto';
import { CodeModulesCategorizedDto } from './dtos/code-modules-categorized.dto';
import {
  ClientCodeModuleDto,
  CodeModuleDtoCodeModuleTypeEnum,
  CodeModulesDto,
  ConfigDto,
  ConfigDtoConfigTypeEnum,
  ErrorDto as ThunderErrorDto,
  PortDto
} from '../../../gen/thunder-api-client';
import { ErrorDto } from './dtos/error.dto';
import { CodeModuleEndpointDto } from './dtos/code-module-endpoint.dto';
import { CodeModuleConfigDto } from './dtos/code-module-config.dto';
import { ConfigType } from './enums/config-type.enum';
import { CodeModuleInstancesConnection } from './entities/code-module-instances-connection.entity';
import { CodeModuleInstanceConnectionDto } from './dtos/code-module-instance-connection.dto';

export abstract class CodeModuleUtils {

  static buildCodeModuleConfigDto(configDto: ConfigDto): CodeModuleConfigDto {
    return {
      name: configDto.id,
      type: { type: configDto.type.id },
      configType: (configDto.configType === ConfigDtoConfigTypeEnum.Input) ? ConfigType.INPUT : ConfigType.SELECTION
    }
  }

  static buildCodeModuleEndpointDto(portDto: PortDto): CodeModuleEndpointDto {
    return {
      name: portDto.id,
      type: portDto.type.id
    };
  }

  static buildCodeModuleDto(codeModuleEntity: CodeModule): CodeModuleDto {
    return {
      uuid: codeModuleEntity.uuid,
      name: '',
      description: '',
      code: codeModuleEntity.code,
      configs: [],
      inputEndpoints: [],
      outputEndpoints: [],
      errors: []
    }
  }

  static buildErrorDto(errorDto: ThunderErrorDto): ErrorDto {
    return {
      line: errorDto.line,
      linePosition: errorDto.linePosition,
      message: errorDto.message
    }
  }

  static buildCodeModulesCategorizedDto(
    systemCodeModuleEntities: CodeModule[], projectCodeModuleEntities: CodeModule[]
  ): CodeModulesCategorizedDto {
    return {
      system: systemCodeModuleEntities.map(CodeModuleUtils.buildCodeModuleDto),
      project: projectCodeModuleEntities.map(CodeModuleUtils.buildCodeModuleDto)
    }
  }

  static buildCodeModuleDtos(codeModulesDto: CodeModulesDto): CodeModuleDto[] {
    return codeModulesDto.codeModules.map(codeModule => ({
      uuid: codeModulesDto.uuid,
      name: codeModule.id,
      description: codeModule.description,
      code: codeModulesDto.code,
      configs: (codeModule.codeModuleType === CodeModuleDtoCodeModuleTypeEnum.Client)
        ? (codeModule as ClientCodeModuleDto).configs.map(CodeModuleUtils.buildCodeModuleConfigDto)
        : [],
      inputEndpoints: (codeModule.codeModuleType === CodeModuleDtoCodeModuleTypeEnum.Client)
        ? (codeModule as ClientCodeModuleDto).inputs.map(CodeModuleUtils.buildCodeModuleEndpointDto)
        : [],
      outputEndpoints: (codeModule.codeModuleType === CodeModuleDtoCodeModuleTypeEnum.Client)
        ? (codeModule as ClientCodeModuleDto).outputs.map(CodeModuleUtils.buildCodeModuleEndpointDto)
        : [],
      errors: codeModulesDto.errors.map(CodeModuleUtils.buildErrorDto)
    }))
  }

  static getConnectionInstanceCodeModuleSignature(): string {
    return 'module instance connection';
  }

  static buildInstanceCodeModuleCode(connections: {
    input: {codeModuleName: string, codeModulePortName: string},
    output: {codeModuleName: string, codeModulePortName: string}
  }[]): string {
    return `${CodeModuleUtils.getConnectionInstanceCodeModuleSignature()}::connection code module {` +
      connections.map(
        connection => `${connection.output.codeModuleName}.${connection.output.codeModulePortName} -> ${connection.input.codeModuleName}.${connection.input.codeModulePortName};`
      ).join('\n') +
      '}';
  }

  static getConnectionInstanceCodeModulePosition(code: string): {line: number, linePosition: number} | null {
    const lines = code.split('\n');

    for (let i = 0; i < lines.length; i++) {
      const index = lines[i].indexOf(CodeModuleUtils.getConnectionInstanceCodeModuleSignature());

      if (index === -1) {
        continue;
      }

      return {line: i + 1, linePosition: index};
    }

    return null;
  }

  static buildCodeModuleInstancesConnectionDto(
    codeModuleIsntancesConnectionEntity: CodeModuleInstancesConnection
  ): CodeModuleInstanceConnectionDto {
    return {
      uuid: codeModuleIsntancesConnectionEntity.uuid,
      inputCodeModuleInstanceName: codeModuleIsntancesConnectionEntity.inputCodeModuleInstanceName,
      inputCodeModuleInstancePortName: codeModuleIsntancesConnectionEntity.inputCodeModuleInstancePortName,
      outputCodeModuleInstanceName: codeModuleIsntancesConnectionEntity.outputCodeModuleInstanceName,
      outputCodeModuleInstancePortName: codeModuleIsntancesConnectionEntity.outputCodeModuleInstancePortName
    };
  }
}
