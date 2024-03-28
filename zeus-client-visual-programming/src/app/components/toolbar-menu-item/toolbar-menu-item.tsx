import React from 'react';

import styles from './toolbar-menu-item.module.scss';
import { Box, Button, makeStyles, SvgIcon } from '@material-ui/core';
import spacing from '../../../assets/styling/spacing.json';
import colors from '../../../assets/styling/colors.json';
import zIndices from '../../../assets/styling/z-indices.json';

const useStyles = makeStyles({
  buttonRoot: {
    backgroundColor: props => props['active'] ? colors.secondary.main: 'transparent',
    border: 'none',
    borderRadius: 0,
    height: spacing.toolbar.width,
    width: spacing.toolbar.width,
    minWidth: 'auto',
    position: 'relative',
    '&:hover': {
      backgroundColor: colors.secondary.main
    }
  }
});

export interface ToolbarMenuItemProps {
  icon: typeof SvgIcon;
  iconFontSize?: number;
  offsetX?: number;
  indicatorIcon?: typeof SvgIcon;
  active?: boolean;
  onClick?: (...args: unknown[]) => void;
  children?: React.ReactNode;
}

export function ToolbarMenuItem(props: ToolbarMenuItemProps) {

  const classes = useStyles(props);

  const Icon = props.icon;
  const IndicatorIcon = props.indicatorIcon;

  const buildActiveItemFromSubMenuIcon = () => <div style={{
    backgroundColor: colors.primary.main,
    borderRadius: 20,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    height: 20,
    width: 20,
    position: 'absolute',
    right: -5,
    top: -5,
    zIndex: zIndices.toolbar.indicatorIcon
  }}>
    <IndicatorIcon style={{ color: '#ffffff', fontSize: 10 }} />
  </div>;

  return (
    <div className={styles.toolbarMenuItemParent} style={{ position: 'relative' }}>
      {props.indicatorIcon !== undefined ? buildActiveItemFromSubMenuIcon() : null}

      <Button
        classes={{ root: classes.buttonRoot }}
        onClick={props.onClick}
      >
        <Icon style={{
          color: '#ffffff',
          fontSize: props.iconFontSize ? props.iconFontSize : 20,
          marginLeft: props.offsetX ? props.offsetX : undefined
        }} />
      </Button>
      <Box
        className={styles.toolbarMenuItemChildren}
        boxShadow={1}
        style={{
          backgroundColor: colors.background_dark,
          position: 'absolute',
          left: spacing.toolbar.width,
          top: 0,
          zIndex: zIndices.toolbar.subMenu
        }}>
        {props.children}
      </Box>

    </div>
  );
}

export default ToolbarMenuItem;
