import { Inject, Injectable, InternalServerErrorException, NotFoundException } from '@nestjs/common';
import { VisualizerWorkspaceDataService } from '../../data/visualizer-workspace-data/visualizer-workspace-data.service';
import { WorkspaceVisualizerDto } from '../../dtos/workspace-visualizer.dto';
import { VisualizerWorkspaceUtils } from '../../visualizer-workspace.utils';
import { VisualizerWorkspace } from '../../entities/visualizer-workspace.entity';
import { ComponentDataService } from '../../../designer/data/component-data/component-data.service';
import { REQUEST } from '@nestjs/core';
import { Component } from '../../../designer/entities/component.entity';
import { RequestKeys } from '../../../../enums/request-keys.enum';
import { ZeusCompilerApplicationApi } from '../../../../../gen/thunder-api-client';

@Injectable()
export class VisualizerWorkspaceService {

  constructor(
    @Inject(REQUEST)
    private readonly req,
    private readonly workspaceDataService: VisualizerWorkspaceDataService,
    private readonly componentDataService: ComponentDataService
  ) {
  }

  private thunderApplicationApi = new ZeusCompilerApplicationApi();

  async createVisualizerWorkspace(componentEntity: Component): Promise<Component> {
    const workspace = new VisualizerWorkspace();
    workspace.codeModuleInstances = [];
    componentEntity.workspace = workspace;
    return await this.componentDataService.save(componentEntity);
  }

  async findByComponentUuid(
    componentUuid: string
  ): Promise<WorkspaceVisualizerDto> {
    let component: Component = this.req[RequestKeys.COMPONENT];

    if (component === undefined) {
      throw new InternalServerErrorException('Could not find workspace: its components were not injected');
    }

    if (component.workspace !== null) {
      const createCodeModuleDtos = component.workspace.codeModuleInstances.map(codeModuleInstance => ({uuid: codeModuleInstance.module.uuid, code: codeModuleInstance.module.code}));
      const codeModulesDtos = (await this.thunderApplicationApi.createCodeModules(createCodeModuleDtos)).data;
      return VisualizerWorkspaceUtils.buildWorkspaceDto(component.workspace, codeModulesDtos);
    }

    component = await this.createVisualizerWorkspace(component);
    return VisualizerWorkspaceUtils.buildWorkspaceDto(component.workspace);
  }

  async deleteByComponentUuid(
    componentUuid: string
  ): Promise<void> {
    const component: Component = this.req[RequestKeys.COMPONENT];

    if (component === undefined) {
      throw new InternalServerErrorException('Could not delete workspace: its component was not injected');
    }

    if (component.workspace === null) {
      throw new NotFoundException(`The component with uuid ${componentUuid} has no visualizer workspace`);
    }

    await this.workspaceDataService.delete(component.workspace.uuid);
  }
}
