import React, { useState } from 'react';

import './configurator-view-dimensions.module.scss';
import { useQueryClient } from 'react-query';
import { useReshapeView } from '../../data/view-data.hooks';
import { useStore } from '../../../../store';
import { Typography } from '@material-ui/core';
import Input, { InputSize, InputType } from '../../../../components/input/input';
import { DesignerConfiguratorProperty } from '../../enums/designer-configurator-property.enum';
import { ViewDto } from '../../../../../gen/api-client';

export interface ConfiguratorViewDimensionsProps {
  workspaceUuid: string;
  view: ViewDto;
}

export function ConfiguratorViewDimensions(
  props: ConfiguratorViewDimensionsProps
) {

  const queryClient = useQueryClient();
  const setConfiguratorIsLoading = useStore(state => state.setConfiguratorIsLoading);

  const reshapeView = useReshapeView(queryClient, setConfiguratorIsLoading, props.view.uuid, props.workspaceUuid);

  const [height, setHeight] = useState(props.view.height);
  const [width, setWidth] = useState(props.view.width);

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
        <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>width:</Typography>
        <Input
          value={props.view.width}
          size={InputSize.SMALL}
          type={InputType.NUMERIC}
          style={{ maxWidth: 75 }}
          onChange={(event) => setWidth(event.value as number)}
          onSubmit={(event) => reshapeView.mutate({width})}
        />
      </div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', marginTop: 5 }}>
        <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>height:</Typography>
        <Input
          value={props.view.height}
          size={InputSize.SMALL}
          type={InputType.NUMERIC}
          style={{ maxWidth: 75 }}
          onChange={(event) => setHeight(event.value as number)}
          onSubmit={(event) => reshapeView.mutate({height})}
        />
      </div>
    </div>
  );
}

export default ConfiguratorViewDimensions;
