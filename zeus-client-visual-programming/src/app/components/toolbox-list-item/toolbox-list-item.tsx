import React, { useEffect, useState } from 'react';

import './toolbox-list-item.module.scss';
import {
  IconButton,
  ListItem,
  ListItemAvatar,
  ListItemSecondaryAction,
  ListItemText,
  makeStyles
} from '@material-ui/core';
import spacing from '../../../assets/styling/spacing.json';
import Input from '../input/input';
import EditTitleIcon from '@material-ui/icons/Edit';
import { DesignerUtils } from '../../modules/designer/designer.utils';


const useStyles = makeStyles({
  buttonRoot: {
    width: '100%'
  },
  listItemAvatarRoot: {
    display: 'flex',
    alignItems: 'center',
    marginRight: spacing.toolbox.list.items.icons.margin,
    minWidth: 'auto'
  },
  listItemButton: {
    paddingBottom: spacing.toolbox.list.items.paddingVertical,
    paddingLeft: spacing.toolbox.padding,
    paddingRight: spacing.toolbox.padding,
    paddingTop: spacing.toolbox.list.items.paddingVertical
  },
  listItemSecondaryActionRoot: {
    right: spacing.toolbox.padding
  }
});

export interface ToolboxListItemProps {
  title: string;
  icon?: React.ReactElement;
  onClick?: () => void;
  actions?: React.ReactNode;
  selected?: boolean;
  disabled?: boolean;
  editModeEnabled?: boolean;
  titleEditable?: boolean;
  onSaveTitle?: (title: string) => void;
}

export function ToolboxListItem(props: ToolboxListItemProps) {

  const classes = useStyles();
  const [title, setTitle] = useState<string>(props.title);
  const [editModeEnabled, setEditModeEnabled] = useState<boolean>(false);

  const handleSaveTitle = () => {
    props.onSaveTitle(title);
    setEditModeEnabled(false);
  };

  useEffect(() => {
    if (!props.selected && editModeEnabled) {
      setEditModeEnabled(false);
    }
  }, [props.selected]);

  const buildActions = () => {
    if (editModeEnabled) {
      return <div style={{ paddingRight: 10 }}>
        {DesignerUtils.buildNameEditingActions(
          handleSaveTitle,
          () => setEditModeEnabled(false)
        )}
      </div>;
    }

    return <div>
      {props.actions}
      {!props.titleEditable
        ? null
        : <IconButton size={'small'} onClick={() => setEditModeEnabled(true)}>
          <EditTitleIcon fontSize={'small'} />
        </IconButton>}
    </div>;
  };
  return (
    <ListItem
      classes={{ button: classes.listItemButton }}
      button={true}
      selected={props.selected !== undefined ? props.selected : false}
      onClick={(editModeEnabled) ? undefined : props.onClick}
      disableRipple={editModeEnabled}
      disabled={props.disabled !== undefined ? props.disabled : false}
    >
      {props.icon !== undefined
        ? <ListItemAvatar classes={{ root: classes.listItemAvatarRoot }}>
          {props.icon}
        </ListItemAvatar>
        : null}
      {!editModeEnabled
        ? <ListItemText primary={props.title} />
        : <Input
          onSubmit={handleSaveTitle}
          value={props.title}
          onChange={(event) => setTitle(event.value as string)}
          style={{ width: '100%' }}
        />}

      <ListItemSecondaryAction classes={{ root: classes.listItemSecondaryActionRoot }}>
        {buildActions()}
      </ListItemSecondaryAction>
    </ListItem>
  );
}

export default ToolboxListItem;
