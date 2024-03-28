import React from 'react';

import './configurator.module.scss';
import colors from '../../../assets/styling/colors.json';
import spacing from '../../../assets/styling/spacing.json';
import { Box } from '@material-ui/core';
import LoadingScreen from '../../components/loading-screen/loading-screen';
import { useStore } from '../../store';

export interface ConfiguratorProps {
  visible: boolean;
  children: React.ReactNode;
}

export function Configurator(props: ConfiguratorProps) {

  const configuratorIsLoading = useStore(state => state.configuratorIsLoading);

  if (!props.visible) {
    return null;
  }

  return (
    <div
      style={{
        height: `calc(100% - ${spacing.configurator.padding * 2}px)`,
        paddingTop: spacing.navigation.height + spacing.configurator.padding,
        position: 'absolute',
        right: spacing.configurator.padding,
        top: -spacing.navigation.height
      }}
    >
      <Box
        style={{
          backgroundColor: colors.background_light,
          borderColor: colors.border_light,
          borderStyle: 'solid',
          borderWidth: 1,
          borderRadius: 10,
          boxSizing: 'border-box',
          height: '100%',
          width: spacing.configurator.width,
          overflowX: 'hidden',
          overflowY: 'auto',
          position: 'relative'
        }}
        boxShadow={1}>
        <LoadingScreen visible={configuratorIsLoading}/>
        {props.children}
      </Box>
    </div>
  );
}

export default Configurator;
