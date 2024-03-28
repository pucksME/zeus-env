import React, { CSSProperties } from 'react';

import './configurator-row.module.scss';
import spacing from '../../../assets/styling/spacing.json';

export interface ConfiguratorRowProps {
  header?: React.ReactElement;
  visible?: boolean;
  children: React.ReactNode;
  style?: CSSProperties;
  innerStyle?: CSSProperties;
}

export function ConfiguratorRow(props: ConfiguratorRowProps) {
  return (props.visible !== undefined && !props.visible) ? null : (
    <div style={{...props.style}}>
      {props.header}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: 20,
        paddingLeft: spacing.configurator.padding + 5,
        paddingRight: spacing.configurator.padding + 5,
        ...props.innerStyle
      }}>{props.children}</div>
    </div>
  );
}

export default ConfiguratorRow;
