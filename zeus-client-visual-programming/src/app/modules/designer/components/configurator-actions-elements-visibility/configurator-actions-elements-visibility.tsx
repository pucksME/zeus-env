import React, { useEffect, useState } from 'react';

import './configurator-actions-elements-visibility.module.scss';
import { ShapeDto } from '../../../../../gen/api-client';
import { useUpdateElementsProperties } from '../../data/component-data.hooks';
import { Button } from '@material-ui/core';
import VisibilityIcon from '@material-ui/icons/Visibility';
import VisibilityOffIcon from '@material-ui/icons/VisibilityOff';
import { ShapeUtils } from '../../shape.utils';
import { useStore } from '../../../../store';
import { useUpdateBlueprintElementsProperties } from '../../data/blueprint-component-data.hooks';

export interface ConfiguratorActionsComponentsVisibilityProps {
  shapes: ShapeDto[];
  elementUuids: string[];
  workspaceUuid: string;
}

export function ConfiguratorActionsElementsVisibility(
  props: ConfiguratorActionsComponentsVisibilityProps
) {

  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);

  const updateElementsProperties = useUpdateElementsProperties(props.workspaceUuid);
  const updateBlueprintElementsProperties = useUpdateBlueprintElementsProperties(props.workspaceUuid);

  const [visible, setVisible] = useState<boolean | null>(null);
  const [compatible, setCompatible] = useState(false);

  const updateVisibility = () => {
    const newVisible = (visible === null) ? false : !visible;

    ((!stageBlueprintComponentProperties.active) ? updateElementsProperties : updateBlueprintElementsProperties).mutate({
      parentComponentUuid: (!stageBlueprintComponentProperties.active)
        ? focusedComponentUuid
        : (focusedComponentUuid === null)
          ? stageBlueprintComponentProperties.blueprintComponentUuid
          : focusedComponentUuid,
      elementUuids: props.elementUuids,
      properties: { visible: newVisible }
    });

    setVisible(newVisible);
  };

  useEffect(() => {
    if (props.elementUuids.length === 0) {
      return;
    }

    const sharedProperties = ShapeUtils.getSharedProperties(props.shapes, ['visible']);

    if (sharedProperties.compatible) {
      setCompatible(true);
    }

    setVisible(sharedProperties.properties.visible);
  }, [props.elementUuids]);

  return (!compatible) ? null : (
    <Button onClick={updateVisibility}>
      {(visible === null || visible) ? <VisibilityOffIcon fontSize={'small'}/> : <VisibilityIcon fontSize={'small'}/>}
    </Button>
  );
}

export default ConfiguratorActionsElementsVisibility;
