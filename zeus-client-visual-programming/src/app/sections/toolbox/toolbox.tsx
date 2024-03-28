import React, { useState } from 'react';

import './toolbox.module.scss';
import { ToolboxPage } from '../../interfaces/toolbox-page.interface';
import { makeStyles, Tab, Tabs } from '@material-ui/core';
import colors from '../../../assets/styling/colors.json';
import spacing from '../../../assets/styling/spacing.json';

export interface ToolboxProps {
  pages: ToolboxPage[];
  defaultPageName: string;
}

// TODO: could also create a custom tab version: https://material-ui.com/components/tabs/#customized-tabs
const useStyles = makeStyles({
  tabsRoot: {
    borderBottomStyle: 'solid',
    borderBottomWidth: 2,
    borderBottomColor: colors.border_light,
    minHeight: 'auto',
    paddingBottom: spacing.toolbox.padding,
    paddingTop: spacing.toolbox.padding
  },
  tabsIndicator: {
    display: 'none'
  },
  tabRoot: {
    // fontSize: 5
    minHeight: 'auto',
    minWidth: 'auto',
    padding: 0,
    paddingLeft: spacing.toolbox.padding,
    paddingRight: spacing.toolbox.padding
  }
});

export function Toolbox(props: ToolboxProps) {

  const classes = useStyles();
  const [activePageName, setActivePageName] = useState(props.defaultPageName);

  const handlePageChange = (event: React.ChangeEvent<unknown>, pageName: string) => setActivePageName(pageName);

  const getContent = () => {
    const page = props.pages.find(page => page.name === activePageName);
    return (page !== undefined) ? page.content : null;
  };

  return (
    <div
      className={'height-100-percent'}
      style={{
        backgroundColor: colors.background_light,
        borderRightColor: colors.border_light,
        borderRightStyle: 'solid',
        borderRightWidth: 1,
        boxSizing: 'border-box',
        flexShrink: 0,
        overflowY: 'auto',
        width: spacing.toolbox.width
      }}
    >
      <Tabs
        classes={{ root: classes.tabsRoot, indicator: classes.tabsIndicator }}
        value={activePageName}
        indicatorColor={'primary'}
        textColor={'primary'}
        onChange={handlePageChange}
      >
        {props.pages.map(page =>
          <Tab
            key={page.name}
            classes={{ root: classes.tabRoot }}
            disableRipple={true}
            label={page.name}
            value={page.name}
          />)}
      </Tabs>
      {getContent()}
    </div>
  );
}

export default Toolbox;
