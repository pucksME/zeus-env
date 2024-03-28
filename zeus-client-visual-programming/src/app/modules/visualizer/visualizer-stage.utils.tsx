import { CodeModuleInstanceDto } from '../../../gen/api-client';
import { useStore } from '../../store';
import CodeModuleInstance from './components/code-module-instance/code-module-instance';

export abstract class VisualizerStageUtils {

  static buildCodeModules(
    codeModuleInstanceDtos: CodeModuleInstanceDto[]
  ): {codeModuleInstances: JSX.Element[], selectedCodeModuleInstances: JSX.Element[]} {
    const selectedCodeModuleInstanceUuids = useStore.getState().selectedCodeModuleInstanceUuids;
    const codeModuleInstances: JSX.Element[] = [];
    const selectedCodeModuleInstances: JSX.Element[] = [];

    for (let i = codeModuleInstanceDtos.length - 1; i >= 0; i--) {
      const codeModuleInstanceDto = codeModuleInstanceDtos[i];
      const codeModuleInstance = <CodeModuleInstance key={i} codeModuleInstanceDto={codeModuleInstanceDto} />;

      if (selectedCodeModuleInstanceUuids.includes(codeModuleInstanceDto.uuid)) {
        selectedCodeModuleInstances.push(codeModuleInstance);
        continue;
      }

      codeModuleInstances.push(codeModuleInstance);
    }

    return {codeModuleInstances, selectedCodeModuleInstances};
  }
}
