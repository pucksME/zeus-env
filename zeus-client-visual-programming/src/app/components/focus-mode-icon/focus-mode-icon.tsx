import React from 'react';
import CenterFocusStrongIcon from '@material-ui/icons/CenterFocusStrong';
import CenterFocusWeakIcon from '@material-ui/icons/CenterFocusWeak';

import './focus-mode-icon.module.scss';

export interface FocusModeIconProps {
  focused: boolean;
  fontSize?: ('inherit' | 'small' | 'default' | 'large');
}

export function FocusModeIcon(props: FocusModeIconProps) {
  return (props.focused)
    ? <CenterFocusStrongIcon fontSize={props.fontSize}/>
    : <CenterFocusWeakIcon fontSize={props.fontSize}/>;
}

export default FocusModeIcon;
