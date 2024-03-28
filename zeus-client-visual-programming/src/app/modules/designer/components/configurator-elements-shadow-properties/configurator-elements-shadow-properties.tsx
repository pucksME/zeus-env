import React, { useEffect, useState } from 'react';

import './configurator-elements-shadow-properties.module.scss';
import { ShapeDto } from '../../../../../gen/api-client';
import { useQueryClient } from 'react-query';
import { Typography } from '@material-ui/core';
import Input, { InputSize, InputType } from '../../../../components/input/input';
import { ShapeUtils } from '../../shape.utils';
import { useUpdateElementsProperties } from '../../data/component-data.hooks';
import { useStore } from '../../../../store';
import { useUpdateBlueprintElementsProperties } from '../../data/blueprint-component-data.hooks';

export interface ConfiguratorShapesShadowPropertiesProps {
  elementUuids: string[];
  shapes: ShapeDto[];
  workspaceUuid: string;
}

export function ConfiguratorElementsShadowProperties(
  props: ConfiguratorShapesShadowPropertiesProps
) {

  const [shadowX, setShadowX] = useState<number | null>(null);
  const [shadowY, setShadowY] = useState<number | null>(null);
  const [shadowBlur, setShadowBlur] = useState<number | null>(null);

  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);

  const updateElementsProperties = useUpdateElementsProperties(props.workspaceUuid);
  const updateBlueprintElementsProperties = useUpdateBlueprintElementsProperties(props.workspaceUuid);
  const [compatible, setCompatible] = useState(false);

  const updateShadowX = (shadowX: number) =>
    ((!stageBlueprintComponentProperties.active) ? updateElementsProperties : updateBlueprintElementsProperties).mutate({
    parentComponentUuid: (!stageBlueprintComponentProperties.active)
      ? focusedComponentUuid
      : (focusedComponentUuid === null)
        ? stageBlueprintComponentProperties.blueprintComponentUuid
        : focusedComponentUuid,
    elementUuids: props.elementUuids,
    properties: {shadowX}
  });

  const updateShadowY = (shadowY: number) =>
    ((!stageBlueprintComponentProperties.active) ? updateElementsProperties : updateBlueprintElementsProperties).mutate({
    parentComponentUuid: (!stageBlueprintComponentProperties.active)
      ? focusedComponentUuid
      : (focusedComponentUuid === null)
        ? stageBlueprintComponentProperties.blueprintComponentUuid
        : focusedComponentUuid,
    elementUuids: props.elementUuids,
    properties: {shadowY}
  });

  const updateShadowBlur = (shadowBlur: number) =>
    ((!stageBlueprintComponentProperties.active) ? updateElementsProperties : updateBlueprintElementsProperties).mutate({
    parentComponentUuid: (!stageBlueprintComponentProperties.active)
      ? focusedComponentUuid
      : (focusedComponentUuid === null)
        ? stageBlueprintComponentProperties.blueprintComponentUuid
        : focusedComponentUuid,
    elementUuids: props.elementUuids,
    properties: {shadowBlur}
  });

  useEffect(() => {
    if (props.elementUuids.length === 0) {
      return;
    }

    const sharedProperties = ShapeUtils.getSharedProperties(
      props.shapes, ['shadowX', 'shadowY', 'shadowBlur']
    );

    if (!sharedProperties.compatible) {
      return;
    }

    setShadowX(sharedProperties.properties.shadowX);
    setShadowY(sharedProperties.properties.shadowY);
    setShadowBlur(sharedProperties.properties.shadowBlur);
    setCompatible(true);
  }, [props.elementUuids]);

  return (!compatible) ? null : (
    <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginTop: 5 }}>
      <div>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>x:</Typography>
          <Input
            value={(shadowX === null) ? '' : shadowX}
            size={InputSize.SMALL}
            type={InputType.NUMERIC_CONTROLS}
            debounceTimeoutOnControlClick={500}
            style={{ maxWidth: 75 }}
            onChange={(event) => setShadowX(event.value as number)}
            onSubmit={(event) => updateShadowX(event.value as number)}
          />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', marginTop: 5 }}>
          <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>y:</Typography>
          <Input
            value={(shadowY === null) ? '' : shadowY}
            size={InputSize.SMALL}
            type={InputType.NUMERIC_CONTROLS}
            debounceTimeoutOnControlClick={500}
            style={{ maxWidth: 75 }}
            onChange={(event) => setShadowY(event.value as number)}
            onSubmit={(event) => updateShadowY(event.value as number)}
          />
        </div>
      </div>
      <div style={{ display: 'flex', alignItems: 'center' }}>
        <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>blur:</Typography>
        <Input
          value={(shadowBlur === null) ? '' : shadowBlur}
          size={InputSize.SMALL}
          type={InputType.NUMERIC_CONTROLS}
          debounceTimeoutOnControlClick={500}
          style={{ maxWidth: 75 }}
          onChange={(event) => setShadowBlur(event.value as number)}
          onSubmit={(event) => updateShadowBlur(event.value as number)}
        />
      </div>
    </div>
  );
}

export default ConfiguratorElementsShadowProperties;
