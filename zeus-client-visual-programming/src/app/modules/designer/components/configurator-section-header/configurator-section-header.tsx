import React, { CSSProperties } from 'react';

import './configurator-section-header.module.scss';
import spacing from '../../../../../assets/styling/spacing.json';
import SettingsIcon from '@material-ui/icons/Settings';
import { SvgIcon, Typography } from '@material-ui/core';

export interface ConfiguratorSectionHeaderProps {
  icon?: typeof SvgIcon;
  title?: string;
  style?: CSSProperties;
}

export function ConfiguratorSectionHeader(
  props: ConfiguratorSectionHeaderProps
) {
  const Icon = props.icon;

  const buildContent = () => {
    if (props.icon === undefined && props.title === undefined) {
      return null;
    }
    return <div style={{ display: 'flex', alignItems: 'center' }}>
      {props.icon === undefined ? null : <Icon color={'primary'} style={{ fontSize: 15, marginRight: 5 }} />}
      {props.title === undefined ? null : <Typography variant={'body1'} color={'textSecondary'}>{props.title}</Typography>}
    </div>
  }
  return (
    <div style={{
      marginBottom: 10,
      paddingLeft: spacing.configurator.padding,
      paddingRight: spacing.configurator.padding,
      paddingTop: 10,
      borderTop: '2px solid #eeeeee',
      ...props.style
    }}>
      {buildContent()}
    </div>
  );
}

export default ConfiguratorSectionHeader;
