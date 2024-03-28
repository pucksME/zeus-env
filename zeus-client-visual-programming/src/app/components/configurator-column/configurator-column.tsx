import React, { CSSProperties } from 'react';

import './configurator-column.module.scss';

export interface ConfiguratorColumnProps {
  children: React.ReactNode;
  style?: CSSProperties;
}

export function ConfiguratorColumn(props: ConfiguratorColumnProps) {
  return (<div style={{...props.style}}>{props.children}</div>);
}

export default ConfiguratorColumn;
