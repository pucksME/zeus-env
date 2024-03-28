import React from 'react';

import './configurator-actions-elements-delete.module.scss';
import { useDeleteElements } from '../../data/component-data.hooks';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import { Button } from '@material-ui/core';
import { useDeleteBlueprintElements } from '../../data/blueprint-component-data.hooks';
import { StageMode } from '../../../../enums/stage-mode.enum';
import { useStore } from '../../../../store';

export interface ConfiguratorActionsComponentsDeleteProps {
  elementUuids: string[];
  workspaceUuid: string;
}

export function ConfiguratorActionsElementsDelete(
  props: ConfiguratorActionsComponentsDeleteProps
) {

  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);

  const deleteElements = useDeleteElements(props.workspaceUuid);
  const deleteBlueprintElements = useDeleteBlueprintElements(
    props.workspaceUuid,
    StageMode.DESIGNER_BLUEPRINT_COMPONENT
  );

  const handleButtonClick = () => ((!stageBlueprintComponentProperties.active)
    ? deleteElements
    : deleteBlueprintElements).mutate();

  return (
    <Button onClick={handleButtonClick}>
      <DeleteForeverIcon fontSize={'small'}/>
    </Button>
  );
}

export default ConfiguratorActionsElementsDelete;
