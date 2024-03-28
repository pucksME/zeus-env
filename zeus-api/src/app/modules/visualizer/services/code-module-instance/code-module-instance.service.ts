import {
  BadRequestException, ForbiddenException,
  Inject,
  Injectable,
  InternalServerErrorException,
  NotFoundException
} from '@nestjs/common';
import { REQUEST } from '@nestjs/core';
import { CodeModuleInstanceDataService } from '../../data/code-module-instance-data/code-module-instance-data.service';
import { CreateCodeModuleInstanceDto } from '../../dtos/create-code-module-instance.dto';
import { CodeModuleInstanceDto } from '../../dtos/code-module-instance.dto';
import { Component } from '../../../designer/entities/component.entity';
import { RequestKeys } from '../../../../enums/request-keys.enum';
import { CodeModuleInstance } from '../../entities/code-module-instance.entity';
import { CodeModuleDataService } from '../../data/code-module-data/code-module-data.service';
import { CodeModuleInstanceUtils } from '../../code-module-instance.utils';
import { VisualizerWorkspaceService } from '../visualizer-workspace/visualizer-workspace.service';
import { PositionCodeModuleInstancesDto } from '../../dtos/position-code-module-instances.dto';
import { TranslateCodeModuleInstancesDto } from '../../dtos/translate-code-module-instances.dto';
import { DeleteCodeModuleInstancesDto } from '../../dtos/delete-code-module-instances.dto';
import { CreateCodeModuleInstanceConnectionDto } from '../../dtos/create-code-module-instance-connection.dto';
import { ZeusCompilerApplicationApi } from '../../../../../gen/thunder-api-client';
import { ComponentDataService } from '../../../designer/data/component-data/component-data.service';
import { CodeModuleInstancesConnection } from '../../entities/code-module-instances-connection.entity';
import {
  CodeModuleInstancesConnectionDataService
} from '../../data/code-module-instances-connection-data/code-module-instances-connection-data.service';
import { CodeModuleUtils } from '../../code-module.utils';
import { ErrorDto } from '../../dtos/error.dto';
import { CodeModuleInstanceConnectionDto } from '../../dtos/code-module-instance-connection.dto';
import { GetCodeModuleInstancesConnectionsDto } from '../../dtos/get-code-module-instances-connections.dto';

@Injectable()
export class CodeModuleInstanceService {

  private thunderApplicationApi = new ZeusCompilerApplicationApi();

  constructor(
    @Inject(REQUEST)
    private readonly req,
    private readonly codeModuleInstanceDataService: CodeModuleInstanceDataService,
    private readonly codeModuleDataService: CodeModuleDataService,
    private readonly visualizerWorkspaceService: VisualizerWorkspaceService,
    private readonly componentDataService: ComponentDataService,
    private readonly codeModuleInstancesConnectionDataService: CodeModuleInstancesConnectionDataService
  ) {
  }

  async save(createCodeModuleInstanceDto: CreateCodeModuleInstanceDto): Promise<CodeModuleInstanceDto> {
    let component: Component = this.req[RequestKeys.COMPONENT];

    if (component === undefined) {
      throw new InternalServerErrorException('Could not create code module instance: component was not injected');
    }

    if (component.workspace === undefined) {
      throw new InternalServerErrorException('Could not create code module instance: component\'s workspace was not injected');
    }

    if (component.workspace === null) {
      component = await this.visualizerWorkspaceService.createVisualizerWorkspace(component);
    }

    const codeModule = await this.codeModuleDataService.find(createCodeModuleInstanceDto.codeModuleUuid);

    if (codeModule === undefined) {
      throw new NotFoundException(`There was no code module with uuid ${createCodeModuleInstanceDto.codeModuleUuid}`);
    }

    const existingCodeModuleInstance = component.workspace.codeModuleInstances.find(
      codeModuleInstance => codeModuleInstance.module.uuid === codeModule.uuid
    );

    if (existingCodeModuleInstance !== undefined) {
      throw new ForbiddenException('Could not create new code module instance: code module already instantiated');
    }

    const codeModuleInstance = new CodeModuleInstance();
    codeModuleInstance.flowDescription = null;
    codeModuleInstance.positionX = createCodeModuleInstanceDto.positionX;
    codeModuleInstance.positionY = createCodeModuleInstanceDto.positionY;
    codeModuleInstance.module = codeModule;
    codeModuleInstance.workspace = component.workspace;

    return CodeModuleInstanceUtils.buildCodeModuleInstanceDto(
      await this.codeModuleInstanceDataService.save(codeModuleInstance)
    );
  }

  async positionCodeModuleInstances(
    positionCodeModuleInstancesDto: PositionCodeModuleInstancesDto
  ): Promise<CodeModuleInstanceDto[]> {
    if (positionCodeModuleInstancesDto.positionX === undefined && positionCodeModuleInstancesDto.positionY === undefined) {
      throw new BadRequestException('Could not position code module instances: either x or y position has to be set');
    }

    const codeModuleInstances = await this.codeModuleInstanceDataService.findMany(
      positionCodeModuleInstancesDto.codeModuleInstanceUuids
    );

    if (codeModuleInstances.length === 0) {
      throw new NotFoundException('Could not position code module instances: no code module instances found');
    }

    if (codeModuleInstances.length !== positionCodeModuleInstancesDto.codeModuleInstanceUuids.length) {
      throw new NotFoundException('Could not position code module instances: at least one code module instance did not exist');
    }

    const transformerOrigin = CodeModuleInstanceUtils.calculateTransformerOrigin(codeModuleInstances);

    for (const codeModuleInstance of codeModuleInstances) {
      if (positionCodeModuleInstancesDto.positionX !== undefined) {
        codeModuleInstance.positionX = (codeModuleInstance.positionX - transformerOrigin.x) + positionCodeModuleInstancesDto.positionX;
      }

      if (positionCodeModuleInstancesDto.positionY !== undefined) {
        codeModuleInstance.positionY = (codeModuleInstance.positionY - transformerOrigin.y) + positionCodeModuleInstancesDto.positionY;
      }
    }

    return (await this.codeModuleInstanceDataService.saveMany(codeModuleInstances)).map(
      codeModuleInstance => CodeModuleInstanceUtils.buildCodeModuleInstanceDto(codeModuleInstance)
    );
  }

  async translateCodeModuleInstances(
    translateCodeModuleInstancesDto: TranslateCodeModuleInstancesDto
  ): Promise<CodeModuleInstanceDto[]> {
    if (translateCodeModuleInstancesDto.translateX === undefined && translateCodeModuleInstancesDto.translateY === undefined) {
      throw new BadRequestException('Could not translate code module instances: either x or y translation has to be set');
    }

    const codeModuleInstances = await this.codeModuleInstanceDataService.findMany(translateCodeModuleInstancesDto.codeModuleInstanceUuids);

    if (codeModuleInstances.length === 0) {
      throw new NotFoundException('Could not translate code module instances: no code module instances found');
    }

    if (codeModuleInstances.length !== translateCodeModuleInstancesDto.codeModuleInstanceUuids.length) {
      throw new NotFoundException('Could not translate code module instances: at least one code module instance did not exist');
    }

    for (const codeModuleInstance of codeModuleInstances) {
      if (translateCodeModuleInstancesDto.translateX !== undefined) {
        codeModuleInstance.positionX += translateCodeModuleInstancesDto.translateX;
      }

      if (translateCodeModuleInstancesDto.translateY !== undefined) {
        codeModuleInstance.positionY += translateCodeModuleInstancesDto.translateY;
      }
    }

    return (await this.codeModuleInstanceDataService.saveMany(codeModuleInstances)).map(
      codeModuleInstance => CodeModuleInstanceUtils.buildCodeModuleInstanceDto(codeModuleInstance)
    );
  }

  async deleteCodeModuleInstances(deleteCodeModuleInstancesDto: DeleteCodeModuleInstancesDto): Promise<void> {
    if (deleteCodeModuleInstancesDto.codeModuleInstanceUuids.length === 0) {
      throw new NotFoundException('Could not delete code module instances: no code module instances found');
    }

    const codeModuleInstances = await this.codeModuleInstanceDataService.findMany(deleteCodeModuleInstancesDto.codeModuleInstanceUuids);

    if (codeModuleInstances.length !== deleteCodeModuleInstancesDto.codeModuleInstanceUuids.length) {
      throw new NotFoundException('Could not delete code module instances: at least one code module instance did not exist');
    }

    await this.codeModuleInstanceDataService.deleteMany(deleteCodeModuleInstancesDto.codeModuleInstanceUuids);
  }

  async saveConnection(
    componentUuid: string,
    createCodeModuleInstanceConnectionDto: CreateCodeModuleInstanceConnectionDto
  ): Promise<ErrorDto[]> {
    if (createCodeModuleInstanceConnectionDto.inputCodeModuleInstanceName === createCodeModuleInstanceConnectionDto.outputCodeModuleInstanceName) {
      throw new ForbiddenException('Could not create code module instances connection: input and output code modules were the same');
    }

    const component = await this.componentDataService.find(
      componentUuid,
      [
        'workspace',
        'workspace.codeModuleInstances',
        'workspace.codeModuleInstances.module',
        'workspace.codeModuleInstancesConnections'
      ]
    );

    if (component === undefined) {
      throw new NotFoundException('Could not save connection: component did not exist');
    }

    let codeModuleInstancesConnections = component.workspace.codeModuleInstancesConnections.find(
      codeModulesConnection => codeModulesConnection.inputCodeModuleInstanceName === createCodeModuleInstanceConnectionDto.inputCodeModuleInstanceName &&
        codeModulesConnection.inputCodeModuleInstancePortName === createCodeModuleInstanceConnectionDto.inputCodeModuleInstancePortName &&
        codeModulesConnection.outputCodeModuleInstanceName === createCodeModuleInstanceConnectionDto.outputCodeModuleInstanceName &&
        codeModulesConnection.outputCodeModuleInstancePortName === createCodeModuleInstanceConnectionDto.outputCodeModuleInstancePortName
    );

    if (codeModuleInstancesConnections !== undefined) {
      throw new ForbiddenException('Could not create code module instances connection: connection did already exist');
    }

    codeModuleInstancesConnections = component.workspace.codeModuleInstancesConnections.find(
      codeModulesConnection => codeModulesConnection.inputCodeModuleInstanceName === createCodeModuleInstanceConnectionDto.inputCodeModuleInstanceName &&
        codeModulesConnection.inputCodeModuleInstancePortName === createCodeModuleInstanceConnectionDto.inputCodeModuleInstancePortName
    );

    if (codeModuleInstancesConnections !== undefined) {
      throw new ForbiddenException('Could not create code module instances connection: multiple connections to an input are not allowed');
    }

    let codeModuleInstancesConnection = new CodeModuleInstancesConnection();
    codeModuleInstancesConnection.visualizerWorkspace = component.workspace;
    codeModuleInstancesConnection.inputCodeModuleInstanceName = createCodeModuleInstanceConnectionDto.inputCodeModuleInstanceName;
    codeModuleInstancesConnection.inputCodeModuleInstancePortName = createCodeModuleInstanceConnectionDto.inputCodeModuleInstancePortName;
    codeModuleInstancesConnection.outputCodeModuleInstanceName = createCodeModuleInstanceConnectionDto.outputCodeModuleInstanceName;
    codeModuleInstancesConnection.outputCodeModuleInstancePortName = createCodeModuleInstanceConnectionDto.outputCodeModuleInstancePortName;

    const codeModulesCode = [
      ...component.workspace.codeModuleInstances.map(codeModuleInstance => codeModuleInstance.module.code),
      CodeModuleUtils.buildInstanceCodeModuleCode([
        ...component.workspace.codeModuleInstancesConnections,
        codeModuleInstancesConnection
      ].map(
        codeModuleInstanceConnections => ({
          input: {
            codeModuleName: codeModuleInstanceConnections.inputCodeModuleInstanceName,
            codeModulePortName: codeModuleInstanceConnections.inputCodeModuleInstancePortName
          },
          output: {
            codeModuleName: codeModuleInstanceConnections.outputCodeModuleInstanceName,
            codeModulePortName: codeModuleInstanceConnections.outputCodeModuleInstancePortName
          }
        })
      ))
    ].join('\n');

    const connectionInstanceCodeModulePosition = CodeModuleUtils.getConnectionInstanceCodeModulePosition(codeModulesCode);

    if (connectionInstanceCodeModulePosition === null) {
      throw new InternalServerErrorException('Could not create code module instances connection: connection instance code module not injected');
    }

    const codeModules = (await this.thunderApplicationApi.createCodeModules([{
      uuid: 'unknown',
      code: codeModulesCode
    }])).data[0];

    const errors = codeModules.errors.filter(
      error => error.line >= connectionInstanceCodeModulePosition.line &&
        error.linePosition >= connectionInstanceCodeModulePosition.linePosition
    );

    if (errors.length !== 0) {
      return errors.map(CodeModuleUtils.buildErrorDto);
    }

    codeModuleInstancesConnection = await this.codeModuleInstancesConnectionDataService.save(codeModuleInstancesConnection);
    return [];
  }

  async getConnections(componentUuid: string): Promise<CodeModuleInstanceConnectionDto[]> {
    const component = await this.componentDataService.find(
      componentUuid,
      [
        'workspace',
        'workspace.codeModuleInstancesConnections'
      ]
    )

    if (component === undefined) {
      throw new NotFoundException('Could not get connections: component did not exist');
    }

    return component.workspace.codeModuleInstancesConnections.map(
      CodeModuleUtils.buildCodeModuleInstancesConnectionDto
    );
  }

  async getCodeModuleInstancesConnections(
    getCodeModuleInstancesConnectionsDto: GetCodeModuleInstancesConnectionsDto
  ): Promise<CodeModuleInstanceConnectionDto[]> {
    if (getCodeModuleInstancesConnectionsDto.codeModuleInstanceUuids.length === 0) {
      throw new NotFoundException('Could not get code module instances connections: no code module instances found');
    }

    const codeModuleInstances = await this.codeModuleInstanceDataService.findMany(
      getCodeModuleInstancesConnectionsDto.codeModuleInstanceUuids,
      [
        'module',
        'workspace',
        'workspace.codeModuleInstancesConnections'
      ]
    );

    if (codeModuleInstances.length !== getCodeModuleInstancesConnectionsDto.codeModuleInstanceUuids.length) {
      throw new NotFoundException('Could not get code module instances connections: at least one code module instance not found');
    }

    const codeModules = (await this.thunderApplicationApi.createCodeModules(codeModuleInstances.map(
      codeModuleInstance => ({uuid: codeModuleInstance.module.uuid, code: codeModuleInstance.module.code})
    ))).data.flatMap(codeModulesDto => codeModulesDto.codeModules);

    const codeModuleInstanceConnectionDtos = [];

    for (const codeModuleInstanceConnection of codeModuleInstances[0].workspace.codeModuleInstancesConnections) {
      if (codeModules.find(codeModule => codeModule.id === codeModuleInstanceConnection.inputCodeModuleInstanceName ||
        codeModule.id === codeModuleInstanceConnection.outputCodeModuleInstanceName) === undefined) {
        continue;
      }

      codeModuleInstanceConnectionDtos.push(CodeModuleUtils.buildCodeModuleInstancesConnectionDto(codeModuleInstanceConnection));
    }

    return codeModuleInstanceConnectionDtos;
  }

  async deleteConnection(codeModuleInstanceConnectionUuid: string): Promise<void> {
    await this.codeModuleInstancesConnectionDataService.delete(codeModuleInstanceConnectionUuid);
  }

}
