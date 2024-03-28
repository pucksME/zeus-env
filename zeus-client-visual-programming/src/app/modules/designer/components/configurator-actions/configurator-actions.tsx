import React from 'react';

import './configurator-actions.module.scss';
import colors from '../../../../../assets/styling/colors.json';
import { ButtonGroup } from '@material-ui/core';

export interface ConfiguratorActionsProps {
  children: React.ReactNode;
}

export function ConfiguratorActions(
  props: ConfiguratorActionsProps
) {
  return (
    <div style={{
      backgroundColor: colors.background_light,
      borderColor: colors.border_light,
      borderStyle: 'solid',
      borderWidth: 1,
      borderRadius: 10,
      position: 'absolute',
      right: 10,
      top: 10
    }}>
      <ButtonGroup size={'small'} variant={'text'}>
        {props.children}
      </ButtonGroup>
    </div>
  );
}

export default ConfiguratorActions;
