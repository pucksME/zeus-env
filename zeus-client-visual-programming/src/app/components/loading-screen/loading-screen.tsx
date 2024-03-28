import React from 'react';

import './loading-screen.module.scss';
import { CircularProgress } from '@material-ui/core';

export interface LoadingScreenProps {
  spinnerSize?: number;
  visible: boolean;
}

export function LoadingScreen(props: LoadingScreenProps) {
  return !props.visible ? null : (
    <div style={{
      backgroundColor: 'rgba(255, 255, 255, 0.5)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      height: '100%',
      width: '100%',
      position: 'absolute',
      left: 0,
      top: 0,
      zIndex: 1
    }}>
      <CircularProgress size={(props.spinnerSize === undefined) ? 50 : props.spinnerSize} thickness={5} />
    </div>
  );
}

export default LoadingScreen;
