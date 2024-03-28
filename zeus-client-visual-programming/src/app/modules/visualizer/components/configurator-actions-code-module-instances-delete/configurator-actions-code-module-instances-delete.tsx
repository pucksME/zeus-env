import React from 'react';

import './configurator-actions-code-module-instances-delete.module.scss';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import { Button } from '@material-ui/core';
import { useDeleteCodeModuleInstances } from '../../data/code-module-instance-data.hooks';

export interface ConfiguratorActionsCodeModuleInstancesDeleteProps {
  componentUuid: string;
  codeModuleInstanceUuids: string[];
}

export function ConfiguratorActionsCodeModuleInstancesDelete(
  props: ConfiguratorActionsCodeModuleInstancesDeleteProps
) {
  const deleteCodeModuleInstances = useDeleteCodeModuleInstances(props.componentUuid);

  const handleClick = () => deleteCodeModuleInstances.mutate({
    codeModuleInstanceUuids: props.codeModuleInstanceUuids
  });

  return (
    <Button onClick={handleClick}>
      <DeleteForeverIcon fontSize={'small'}/>
    </Button>
  );
}

export default ConfiguratorActionsCodeModuleInstancesDelete;
