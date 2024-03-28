import React, { useState } from 'react';

import './configurator-elements-position.module.scss';
import { Typography } from '@material-ui/core';
import Input, { InputSize, InputType } from '../../../../components/input/input';
import { useQueryClient } from 'react-query';
import { useStore } from '../../../../store';
import { usePositionElements } from '../../data/component-data.hooks';
import { usePositionBlueprintElements } from '../../data/blueprint-component-data.hooks';

export interface ConfiguratorShapesPositionProps {
  workspaceUuid: string;
  elementUuids: string[];
}

export function ConfiguratorElementsPosition(
  props: ConfiguratorShapesPositionProps
) {

  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const selectedElementsProperties = useStore(state => state.selectedElementsProperties);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);

  const positionElements = usePositionElements(props.workspaceUuid);
  const positionBlueprintElements = usePositionBlueprintElements(props.workspaceUuid);

  const [positionX, setPositionX] = useState(selectedElementsProperties.x);
  const [positionY, setPositionY] = useState(selectedElementsProperties.y);

  const handleSubmit = (elementUuids: string[], position: {x?: number, y?: number}) =>
    ((!stageBlueprintComponentProperties.active) ? positionElements : positionBlueprintElements).mutate({
    parentComponentUuid: (!stageBlueprintComponentProperties.active)
      ? focusedComponentUuid
      : (focusedComponentUuid === null)
        ? stageBlueprintComponentProperties.blueprintComponentUuid
        : focusedComponentUuid,
    elementUuids,
    elementsProperties: {
      height: selectedElementsProperties.height,
      width: selectedElementsProperties.width,
      x: selectedElementsProperties.x,
      y: selectedElementsProperties.y
    },
    positionX: position.x,
    positionY: position.y
  });

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
        <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>x:</Typography>
        <Input
          value={selectedElementsProperties.x}
          size={InputSize.SMALL}
          type={InputType.NUMERIC}
          style={{ maxWidth: 75 }}
          onChange={(event) => setPositionX(event.value as number)}
          onSubmit={(event) => handleSubmit(props.elementUuids, {x: positionX})}
        />
      </div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', marginTop: 5 }}>
        <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>y:</Typography>
        <Input
          value={selectedElementsProperties.y}
          size={InputSize.SMALL}
          type={InputType.NUMERIC}
          style={{ maxWidth: 75 }}
          onChange={(event) => setPositionY(event.value as number)}
          onSubmit={(event) => handleSubmit(props.elementUuids, {y: positionY})}
        />
      </div>
    </div>
  );
}

export default ConfiguratorElementsPosition;
