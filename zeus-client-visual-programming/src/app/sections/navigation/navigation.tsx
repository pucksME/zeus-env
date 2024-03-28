import React, { useState } from 'react';
import './navigation.module.scss';
import { Link, useRouteMatch } from 'react-router-dom';
import { AppBar, Box, Button, makeStyles, Toolbar, withTheme } from '@material-ui/core';
import GetAppIcon from '@material-ui/icons/GetApp';
import FlashOnIcon from '@material-ui/icons/FlashOn';
import LogoutIcon from '@material-ui/icons/Logout';
import colors from '../../../assets/styling/colors.json';
import spacing from '../../../assets/styling/spacing.json';
import { useStore } from '../../store';
import { useQueryClient } from 'react-query';
import DesignerNavigation from '../../modules/designer/sections/designer-navigation/designer-navigation';
import LoadingScreen from '../../components/loading-screen/loading-screen';
import Exporter from '../../modules/designer/components/exporter/exporter';

/* eslint-disable-next-line */
export interface NavigationProps {
}

// https://material-ui.com/styles/advanced/#overriding-styles-classes-prop
const useStyles = makeStyles({
  appBarRoot: {
    backgroundColor: colors.background_light,
    borderBottomColor: colors.border_light,
    borderBottomStyle: 'solid',
    borderBottomWidth: 1,
    boxShadow: 'none',
    height: spacing.navigation.height,
    position: 'absolute'
  },
  toolbarRoot: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    height: '100%',
    paddingLeft: 0,
    paddingRight: 0,
    paddingTop: 0
  },
  buttonRoot: {
    borderRadius: 0,
    boxShadow: 'none',
    height: '100%',
    paddingRight: spacing.navigation.button.paddingHorizontal,
    paddingLeft: spacing.navigation.button.paddingHorizontal
  }
});

export function Navigation(props: NavigationProps) {
  const classes = useStyles();
  const [exporterVisible, setExporterVisible] = useState<boolean>(false);
  const signOut = useStore(state => state.signOut);
  const queryClient = useQueryClient();
  const designerMatch = useRouteMatch('/project/:projectUuid');
  const visualizerMatch = useRouteMatch('/project/:projectUuid/component/:componentUuid');
  const projectUuid = (designerMatch === null) ? undefined : designerMatch.params['projectUuid'];
  const componentUuid = (visualizerMatch === null) ? undefined : visualizerMatch.params['componentUuid'];
  const isLoading = useStore(state => state.navigationIsLoading);

  const handleSignOut = () => {
    signOut();
    queryClient.clear();
  }

  const handleExportClick = () => setExporterVisible(true);

  const handleExporterClose = () => setExporterVisible(false);

  return (
    <AppBar classes={{ root: classes.appBarRoot }} position={'relative'}>
      <Box style={{ position: 'relative' }} boxShadow={1}>
        <Toolbar classes={{ root: classes.toolbarRoot }} variant={'dense'}>
          <div style={{ display: 'flex', alignItems: 'center', height: '100%' }}>
            <div style={{
              backgroundColor: props['theme'].palette.primary.main,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              height: '100%',
              width: spacing.toolbar.width
            }}>
              <FlashOnIcon />
            </div>

            <ul className={'height-100-percent'}>
              <li className={'height-100-percent'}>
                <Link to='/' style={{ textDecoration: 'none' }}>
                  <Button
                    classes={{ root: classes.buttonRoot }}
                    style={{color: colors.color_dark}}
                    size={'small'}
                    variant={'text'}
                  >
                    My Projects
                  </Button>
                </Link>
              </li>
            </ul>

            {(projectUuid === undefined || componentUuid !== undefined)
              ? null
              : <DesignerNavigation projectUuid={projectUuid}/>}
          </div>

          <div style={{ height: '100%' }}>
            {(designerMatch !== null) ? <Button
              classes={{ root: classes.buttonRoot }}
              color={'secondary'}
              size={'small'}
              startIcon={<GetAppIcon />}
              variant={'contained'}
              onClick={handleExportClick}
            >
              Export
            </Button> : null}
            <Button
              classes={{ root: classes.buttonRoot }}
              style={{color: colors.color_dark}}
              size={'small'}
              startIcon={<LogoutIcon />}
              variant={'text'}
              onClick={handleSignOut}
            >
              Sign Out
            </Button>
          </div>
        </Toolbar>
        <LoadingScreen visible={isLoading} spinnerSize={25}/>
      </Box>
      <Exporter
        visible={exporterVisible}
        projectUuid={projectUuid}
        onClose={handleExporterClose}
      />
    </AppBar>
  );
}

export default withTheme(Navigation);
