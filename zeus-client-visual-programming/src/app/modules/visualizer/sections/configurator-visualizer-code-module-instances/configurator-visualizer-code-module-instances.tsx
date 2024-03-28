import React from 'react';

import './configurator-visualizer-code-module-instances.module.scss';
import ConfiguratorActions from '../../../designer/components/configurator-actions/configurator-actions';
import ConfiguratorActionsCodeModuleInstancesDelete
  from '../../components/configurator-actions-code-module-instances-delete/configurator-actions-code-module-instances-delete';
import { useStore } from '../../../../store';
import { WorkspaceVisualizerDto } from '../../../../../gen/api-client';
import { useConnections } from '../../data/code-module-instance-data.hooks';
import ConfiguratorRow from '../../../../components/configurator-row/configurator-row';
import ConfiguratorSectionHeader
  from '../../../designer/components/configurator-section-header/configurator-section-header';
import ConfiguratorCodeModuleInstance
  from '../../components/configurator-code-module-instance/configurator-code-module-instance';

export interface ConfiguratorVisualizerCodeModuleInstancesProps {
  componentUuid: string;
  workspace: WorkspaceVisualizerDto;
}

export function ConfiguratorVisualizerCodeModuleInstances(
  props: ConfiguratorVisualizerCodeModuleInstancesProps
) {
  const selectedCodeModuleInstanceUuids = useStore(state => state.selectedCodeModuleInstanceUuids);
  const {isLoading, isError, codeModuleInstancesConnectionDtos, error} = useConnections(props.componentUuid);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (isError) {
    return <div>{error}</div>;
  }

  const selectedCodeModuleInstances = props.workspace.codeModuleInstances.filter(
    codeModuleInstance => selectedCodeModuleInstanceUuids.includes(codeModuleInstance.uuid)
  );

  return (
    <div>
      <ConfiguratorActions>
        <ConfiguratorActionsCodeModuleInstancesDelete
          componentUuid={props.componentUuid}
          codeModuleInstanceUuids={selectedCodeModuleInstanceUuids}
        />
      </ConfiguratorActions>


      {selectedCodeModuleInstances.map((selectedCodeModuleInstance, index) =>
        <ConfiguratorRow
          key={index}
          header={<ConfiguratorSectionHeader
            title={selectedCodeModuleInstance.codeModule.name}
            style={(index === 0) ? {borderTop: 'none'} : undefined}
          />}
        >
          <ConfiguratorCodeModuleInstance
            componentUuid={props.componentUuid}
            codeModuleInstanceDto={selectedCodeModuleInstance}
          />
        </ConfiguratorRow>
      )}

    </div>
  );
}

export default ConfiguratorVisualizerCodeModuleInstances;
