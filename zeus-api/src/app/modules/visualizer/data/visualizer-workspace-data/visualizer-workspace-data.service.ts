import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { VisualizerWorkspace } from '../../entities/visualizer-workspace.entity';
import { DeleteResult, Repository } from 'typeorm';
import { Component } from '../../../designer/entities/component.entity';
import {
  FindVisualizerWorkspaceByComponentUuidStatus
} from '../../enums/find-visualizer-workspace-by-component-uuid-status.enum';

@Injectable()
export class VisualizerWorkspaceDataService {

  constructor(
    @InjectRepository(VisualizerWorkspace)
    private readonly workspaceRepository: Repository<VisualizerWorkspace>,
    @InjectRepository(Component)
    private readonly componentRepository: Repository<Component>
  ) {
  }

  async findByComponentUuid(
    componentUuid: string,
    relations: string[] = []
  ): Promise<VisualizerWorkspace | FindVisualizerWorkspaceByComponentUuidStatus> {
    if (!relations.includes('workspace')) {
      relations.push('workspace');
    }

    const componentEntity = await this.componentRepository.findOne(
      {uuid: componentUuid},
      {relations: relations.map(relation => (relation === 'workspace') ? relation : 'workspace.' + relation)}
    );

    if (componentEntity === undefined) {
      return FindVisualizerWorkspaceByComponentUuidStatus.COMPONENT_NOT_FOUND;
    }

    if (componentEntity.workspace === null) {
      return FindVisualizerWorkspaceByComponentUuidStatus.VISUALIZER_WORKSPACE_NOT_FOUND;
    }

    return componentEntity.workspace;
  }

  save(workspaceEntity: VisualizerWorkspace): Promise<VisualizerWorkspace> {
    return this.workspaceRepository.save(workspaceEntity);
  }

  delete(visualizerWorkspaceUuid: string): Promise<DeleteResult> {
    return this.workspaceRepository.delete(visualizerWorkspaceUuid);
  }

}
