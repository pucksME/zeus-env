import { VisualizerWorkspace } from './entities/visualizer-workspace.entity';
import { WorkspaceVisualizerDto } from './dtos/workspace-visualizer.dto';
import { CodeModuleInstanceUtils } from './code-module-instance.utils';
import { CodeModulesDto } from '../../../gen/thunder-api-client';

export abstract class VisualizerWorkspaceUtils {

  static buildWorkspaceDto(
    visualizerWorkspaceEntity: VisualizerWorkspace,
    codeModulesDtos: CodeModulesDto[] = []
  ): WorkspaceVisualizerDto {
    return {
      uuid: visualizerWorkspaceEntity.uuid,
      codeModuleInstances: (!visualizerWorkspaceEntity.codeModuleInstances)
        ? null
        : visualizerWorkspaceEntity.codeModuleInstances.map(
          codeModuleInstance => CodeModuleInstanceUtils.buildCodeModuleInstanceDto(codeModuleInstance, codeModulesDtos)
        )
    }
  }
}
