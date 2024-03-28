import React from 'react';

import './toolbox-list.module.scss';
import { List, makeStyles, Typography } from '@material-ui/core';
import spacing from '../../../assets/styling/spacing.json';
import { DragDropContext, DropResult, ResponderProvided } from 'react-beautiful-dnd';

const useStyles = makeStyles({
  listRoot: {
    paddingTop: 0,
    width: '100%'
  }
});

export interface ToolboxListProps {
  title: string;
  actions?: React.ReactNode;
  children?: React.ReactNode;
  onDragEnd?: (result: DropResult, provided: ResponderProvided) => void;
}

export function ToolboxList(props: ToolboxListProps) {

  const classes = useStyles();
  return (
    <div>
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingBottom: spacing.toolbox.list.header.paddingBottom,
        paddingLeft: spacing.toolbox.padding,
        paddingRight: spacing.toolbox.padding,
        paddingTop: spacing.toolbox.list.header.paddingTop
      }}>
        <Typography variant={'body1'} color={'textSecondary'}>{props.title}</Typography>
        {props.actions}
      </div>
      <List classes={{ root: classes.listRoot }} dense={true}>
        {(props.onDragEnd === undefined)
          ? props.children
          : <DragDropContext onDragEnd={props.onDragEnd}>{props.children}</DragDropContext>}
      </List>
    </div>

  );
}

export default ToolboxList;
