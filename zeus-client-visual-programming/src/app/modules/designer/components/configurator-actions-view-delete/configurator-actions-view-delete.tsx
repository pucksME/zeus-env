import React from 'react';

import './configurator-actions-view-delete.module.scss';
import { Button } from '@material-ui/core';
import { useQueryClient } from 'react-query';
import { useDeleteView } from '../../data/view-data.hooks';
import { useStore } from '../../../../store';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';

export interface ConfiguratorActionsViewDeleteProps {
  viewUuid: string;
  workspaceUuid: string;
}

export function ConfiguratorActionsViewDelete(
  props: ConfiguratorActionsViewDeleteProps
) {

  const queryClient = useQueryClient();
  const resetActiveView = useStore(state => state.resetActiveDesignerView);

  const deleteView = useDeleteView(queryClient, resetActiveView, props.workspaceUuid);

  return (
    <Button onClick={() => deleteView.mutate(props.viewUuid)}>
      <DeleteForeverIcon fontSize={'small'}/>
    </Button>
  );
}

export default ConfiguratorActionsViewDelete;
