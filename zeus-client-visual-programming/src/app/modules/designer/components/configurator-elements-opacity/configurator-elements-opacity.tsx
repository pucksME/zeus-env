import React, { useEffect, useState } from 'react';

import './configurator-elements-opacity.module.scss';
import { ShapeDto } from '../../../../../gen/api-client';
import { useQueryClient } from 'react-query';
import { Slider } from '@material-ui/core';
import OpacityIcon from '@material-ui/icons/Opacity';
import Input, { InputSize, InputType } from '../../../../components/input/input';
import { useUpdateElementsProperties } from '../../data/component-data.hooks';
import { ShapeUtils } from '../../shape.utils';
import { useStore } from '../../../../store';
import { useUpdateBlueprintElementsProperties } from '../../data/blueprint-component-data.hooks';

export interface ConfiguratorShapesOpacityProps {
  elementUuids: string[];
  shapes: ShapeDto[];
  workspaceUuid: string;
}

export function ConfiguratorElementsOpacity(
  props: ConfiguratorShapesOpacityProps
) {

  const [opacity, setOpacity] = useState<number | null>(null);

  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);

  const updateElementsProperties = useUpdateElementsProperties(props.workspaceUuid);
  const updateBlueprintElementsProperties = useUpdateBlueprintElementsProperties(props.workspaceUuid);
  const [compatible, setCompatible] = useState(false);

  const updateOpacity = (opacity: number) =>
    ((!stageBlueprintComponentProperties.active) ? updateElementsProperties : updateBlueprintElementsProperties).mutate({
    parentComponentUuid: (!stageBlueprintComponentProperties.active)
      ? focusedComponentUuid
      : (focusedComponentUuid === null)
        ? stageBlueprintComponentProperties.blueprintComponentUuid
        : focusedComponentUuid,
    elementUuids: props.elementUuids,
    properties: {opacity}
  });

  useEffect(() => {
    if (props.elementUuids.length === 0) {
      return;
    }

    const sharedProperties = ShapeUtils.getSharedProperties(props.shapes, ['opacity']);

    if (!sharedProperties.compatible) {
      return setCompatible(false);
    }

    setOpacity(sharedProperties.properties.opacity);
    setCompatible(true);
  }, [props.elementUuids]);

  return (!compatible) ? null : (
    <div style={{ display: 'flex', alignItems: 'center', marginTop: 10 }}>
      <Slider
        min={0}
        max={1}
        step={0.05}
        value={(opacity === null) ? 0 : opacity}
        onChange={(event, value: number) => setOpacity(value)}
        onChangeCommitted={(event, value) => updateOpacity(value as number)}
        color={'secondary'}
        style={{ marginRight: 15 }}
      />
      <OpacityIcon color={'secondary'} style={{ fontSize: 15, marginRight: 10 }} />
      <Input
        value={opacity === null ? '' : (opacity * 100)}
        size={InputSize.SMALL}
        type={InputType.NUMERIC}
        style={{ maxWidth: 75 }}
        onChange={(event) => setOpacity((event.value as number) / 100)}
        onSubmit={(event) => updateOpacity((event.value as number) / 100)}
      />
    </div>
  );
}

export default ConfiguratorElementsOpacity;
