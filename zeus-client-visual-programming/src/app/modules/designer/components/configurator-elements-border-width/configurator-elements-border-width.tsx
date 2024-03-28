import React, { useEffect, useState } from 'react';

import './configurator-elements-border-width.module.scss';
import { ShapeDto } from '../../../../../gen/api-client';
import { useQueryClient } from 'react-query';
import { Typography } from '@material-ui/core';
import Input, { InputSize, InputType } from '../../../../components/input/input';
import { useUpdateElementsProperties } from '../../data/component-data.hooks';
import { ShapeUtils } from '../../shape.utils';
import { useStore } from '../../../../store';
import { useUpdateBlueprintElementsProperties } from '../../data/blueprint-component-data.hooks';

export interface ConfiguratorShapesBorderWidthProps {
  elementUuids: string[];
  shapes: ShapeDto[];
  workspaceUuid: string;
}

export function ConfiguratorElementsBorderWidth(
  props: ConfiguratorShapesBorderWidthProps
) {

  const [borderWidth, setBorderWidth] = useState<number | null>(null);

  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);

  const updateElementsProperties = useUpdateElementsProperties(props.workspaceUuid);
  const updateBlueprintElementsProperties = useUpdateBlueprintElementsProperties(props.workspaceUuid);
  const [compatible, setCompatible] = useState(false);

  const updateBorderWidth = (borderWidth: number) =>
    ((!stageBlueprintComponentProperties.active) ? updateElementsProperties : updateBlueprintElementsProperties).mutate({
    parentComponentUuid: (!stageBlueprintComponentProperties.active)
      ? focusedComponentUuid
      : (focusedComponentUuid === null)
        ? stageBlueprintComponentProperties.blueprintComponentUuid
        : focusedComponentUuid,
    elementUuids: props.elementUuids,
    properties: {borderWidth}
  });

  useEffect(() => {
    if (props.elementUuids.length === 0) {
      return;
    }

    const sharedProperties = ShapeUtils.getSharedProperties(props.shapes, ['borderWidth']);

    if (!sharedProperties.compatible) {
      return setCompatible(false);
    }

    setBorderWidth(sharedProperties.properties.borderWidth);
    setCompatible(true);
  }, [props.elementUuids]);

  return (!compatible) ? null : (
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>width:</Typography>

      <Input
        value={(borderWidth === null) ? '' : borderWidth}
        size={InputSize.SMALL}
        type={InputType.NUMERIC_CONTROLS}
        debounceTimeoutOnControlClick={500}
        style={{ maxWidth: 75 }}
        onChange={(event) =>
          setBorderWidth(event.value as number)}
        onSubmit={(event) => updateBorderWidth(event.value as number)}
      />
    </div>
  );
}

export default ConfiguratorElementsBorderWidth;
