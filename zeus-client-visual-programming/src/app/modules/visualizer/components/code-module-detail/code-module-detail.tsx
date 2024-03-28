import React, { CSSProperties } from 'react';

import './code-module-detail.module.scss';
import { SvgIcon, Typography } from '@material-ui/core';

export interface CodeModuleDetailProps {
  icon: typeof SvgIcon;
  count: number;
  title: string;
  titleOne?: string;
  visible?: boolean;
  warning?: boolean;
  style?: CSSProperties;
  iconStyle?: CSSProperties;
  textVariant: 'body2' | 'caption';
}

export function CodeModuleDetail(props: CodeModuleDetailProps) {
  const Icon = props.icon;
  return (props.visible !== undefined && !props.visible) ? null : (
    <div style={{display: 'flex', alignItems: 'center', ...props.style}}>
      <Icon style={{ fontSize: 12, marginRight: 3, ...props.iconStyle }} color={(props.warning) ? 'primary' : 'secondary'}/>
      <Typography variant={props.textVariant} color={'textPrimary'}>
        {props.count} {(props.count === 1 && props.titleOne !== undefined) ? props.titleOne : props.title}
      </Typography>
    </div>
  );
}

export default CodeModuleDetail;
