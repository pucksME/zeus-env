import React from 'react';

import './configurator-actions-elements-mutations-reset.module.scss';
import ResetMutationsIcon from '@material-ui/icons/LayersClear';
import { Button } from '@material-ui/core';
import { useResetMutations } from '../../data/component-data.hooks';

export interface ConfiguratorActionsElementsMutationsResetProps {
  workspaceUuid: string;
}

export function ConfiguratorActionsElementsMutationsReset(
  props: ConfiguratorActionsElementsMutationsResetProps
) {

  const resetMutations = useResetMutations(props.workspaceUuid);

  return (
    <Button onClick={() => resetMutations.mutate()}>
      <ResetMutationsIcon fontSize={'small'}/>
    </Button>
  );
}

export default ConfiguratorActionsElementsMutationsReset;
