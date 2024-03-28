import React, { useState } from 'react';

import './configurator-view-position.module.scss';
import { ViewDto } from '../../../../../gen/api-client';
import { useQueryClient } from 'react-query';
import { useStore } from '../../../../store';
import { usePositionView } from '../../data/view-data.hooks';
import { Typography } from '@material-ui/core';
import Input, { InputSize, InputType } from '../../../../components/input/input';

export interface ConfiguratorViewPositionProps {
  workspaceUuid: string;
  view: ViewDto;
}

export function ConfiguratorViewPosition(props: ConfiguratorViewPositionProps) {

  const queryClient = useQueryClient();
  const setConfiguratorIsLoading = useStore(state => state.setConfiguratorIsLoading);

  const positionView = usePositionView(queryClient, setConfiguratorIsLoading, props.view.uuid, props.workspaceUuid);

  const [positionX, setPositionX] = useState(props.view.positionX);
  const [positionY, setPositionY] = useState(props.view.positionY);

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
        <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>x:</Typography>
        <Input
          value={props.view.positionX}
          size={InputSize.SMALL}
          type={InputType.NUMERIC}
          style={{ maxWidth: 75 }}
          onChange={(event) => setPositionX(event.value as number)}
          onSubmit={(event) => positionView.mutate({positionX})}
        />
      </div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', marginTop: 5 }}>
        <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>y:</Typography>
        <Input
          value={props.view.positionY}
          size={InputSize.SMALL}
          type={InputType.NUMERIC}
          style={{ maxWidth: 75 }}
          onChange={(event) => setPositionY(event.value as number)}
          onSubmit={(event) => positionView.mutate({positionY})}
        />
      </div>
    </div>
  );
}

export default ConfiguratorViewPosition;
