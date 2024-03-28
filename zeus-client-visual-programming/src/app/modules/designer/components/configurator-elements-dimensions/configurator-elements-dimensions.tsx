import React, { useState } from 'react';

import './configurator-elements-dimensions.module.scss';
import { useQueryClient } from 'react-query';
import { useStore } from '../../../../store';
import { Typography } from '@material-ui/core';
import Input, { InputSize, InputType } from '../../../../components/input/input';
import { useReshapeElements } from '../../data/component-data.hooks';
import { useReshapeBlueprintElements } from '../../data/blueprint-component-data.hooks';

export interface ConfiguratorShapesDimensionsProps {
  workspaceUuid: string;
  elementUuids: string[];
}

export function ConfiguratorElementsDimensions(
  props: ConfiguratorShapesDimensionsProps
) {

  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const selectedElementsProperties = useStore(state => state.selectedElementsProperties);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);

  const reshapeElements = useReshapeElements(props.workspaceUuid);
  const reshapeBlueprintElements = useReshapeBlueprintElements(props.workspaceUuid);

  const [height, setHeight] = useState(selectedElementsProperties.height);
  const [width, setWidth] = useState(selectedElementsProperties.width);

  const handleSubmit = (elementUuids: string[], dimensions: {height?: number, width?: number}) =>
    (!stageBlueprintComponentProperties.active ? reshapeElements : reshapeBlueprintElements).mutate({
      parentComponentUuid: (!stageBlueprintComponentProperties.active)
        ? focusedComponentUuid
        : (focusedComponentUuid === null)
          ? stageBlueprintComponentProperties.blueprintComponentUuid
          : focusedComponentUuid,
      elementUuids: elementUuids,
      elementsProperties: {
        height: selectedElementsProperties.height,
        width: selectedElementsProperties.width,
        x: selectedElementsProperties.x,
        y: selectedElementsProperties.y
      },
      ...dimensions
    });

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
        <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>width:</Typography>
        <Input
          value={selectedElementsProperties.width}
          size={InputSize.SMALL}
          type={InputType.NUMERIC}
          style={{ maxWidth: 75 }}
          onChange={(event) => setWidth(event.value as number)}
          onSubmit={(event) => handleSubmit(props.elementUuids, {width})}
        />
      </div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', marginTop: 5 }}>
        <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>height:</Typography>
        <Input
          value={selectedElementsProperties.height}
          size={InputSize.SMALL}
          type={InputType.NUMERIC}
          style={{ maxWidth: 75 }}
          onChange={(event) => setHeight(event.value as number)}
          onSubmit={(event) => handleSubmit(props.elementUuids, {height})}
        />
      </div>
    </div>
  );
}

export default ConfiguratorElementsDimensions;
