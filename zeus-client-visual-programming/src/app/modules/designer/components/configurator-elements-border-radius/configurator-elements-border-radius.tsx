import React, { useEffect, useState } from 'react';

import './configurator-elements-border-radius.module.scss';
import { ShapeDto } from '../../../../../gen/api-client';
import RoundedCornerIcon from '@material-ui/icons/RoundedCorner';
import Input, { InputSize, InputType } from '../../../../components/input/input';
import { useUpdateElementsProperties } from '../../data/component-data.hooks';
import { ShapeUtils } from '../../shape.utils';
import { useStore } from '../../../../store';
import { useUpdateBlueprintElementsProperties } from '../../data/blueprint-component-data.hooks';

export interface ConfiguratorShapesBorderRadiusProps {
  elementUuids: string[];
  shapes: ShapeDto[];
  workspaceUuid: string;
}

export function ConfiguratorElementsBorderRadius(
  props: ConfiguratorShapesBorderRadiusProps
) {

  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);

  const updateElementsProperties = useUpdateElementsProperties(props.workspaceUuid);
  const updateBlueprintElementsProperties = useUpdateBlueprintElementsProperties(props.workspaceUuid);

  const [borderRadiusTopLeft, setBorderRadiusTopLeft] = useState<number | null>(null);
  const [borderRadiusTopRight, setBorderRadiusTopRight] = useState<number | null>(null);
  const [borderRadiusBottomRight, setBorderRadiusBottomRight] = useState<number | null>(null);
  const [borderRadiusBottomLeft, setBorderRadiusBottomLeft] = useState<number | null>(null);
  const [compatible, setCompatible] = useState(false);

  const updateBorderRadius = (borderRadius: (number | null)[]) =>
    ((!stageBlueprintComponentProperties.active) ? updateElementsProperties : updateBlueprintElementsProperties).mutate({
    parentComponentUuid: (!stageBlueprintComponentProperties.active)
      ? focusedComponentUuid
      : (focusedComponentUuid === null)
        ? stageBlueprintComponentProperties.blueprintComponentUuid
        : focusedComponentUuid,
    elementUuids: props.elementUuids,
    properties: {borderRadius}
  });

  useEffect(() => {
    if (props.elementUuids.length === 0) {
      return;
    }

    const sharedProperties = ShapeUtils.getSharedProperties(
      props.shapes, ['borderRadius']
    );

    if (!sharedProperties.compatible) {
      return setCompatible(false);
    }

    setBorderRadiusTopLeft(sharedProperties.properties.borderRadius[0]);
    setBorderRadiusTopRight(sharedProperties.properties.borderRadius[1]);
    setBorderRadiusBottomLeft(sharedProperties.properties.borderRadius[2]);
    setBorderRadiusBottomRight(sharedProperties.properties.borderRadius[3]);
    setCompatible(true);
  }, [props.elementUuids]);

  return (!compatible) ? null : (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>

      <div>
        <div style={{ display: 'flex', alignItems: 'center', marginRight: 5 }}>
          <RoundedCornerIcon
            color={'secondary'}
            style={{ fontSize: 20, transform: 'rotate(-90deg)', marginRight: 5 }}
          />
          <Input
            value={borderRadiusTopLeft === null ? '' : borderRadiusTopLeft}
            size={InputSize.SMALL}
            type={InputType.NUMERIC_CONTROLS}
            debounceTimeoutOnControlClick={500}
            style={{ maxWidth: 75 }}
            onChange={(event) => setBorderRadiusTopLeft(event.value as number)}
            onSubmit={(event) => updateBorderRadius([event.value as number, null, null, null])}
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', marginRight: 5, marginTop: 5 }}>
          <RoundedCornerIcon
            color={'secondary'}
            style={{ fontSize: 20, marginRight: 5 }}
          />
          <Input
            value={borderRadiusTopRight === null ? '' : borderRadiusTopRight}
            size={InputSize.SMALL}
            type={InputType.NUMERIC_CONTROLS}
            debounceTimeoutOnControlClick={500}
            style={{ maxWidth: 75 }}
            onChange={(event) => setBorderRadiusTopRight(event.value as number)}
            onSubmit={(event) => updateBorderRadius([null, event.value as number, null, null])}
          />
        </div>
      </div>
      <div>
        <div style={{ display: 'flex', alignItems: 'center', marginRight: 5 }}>
          <RoundedCornerIcon
            color={'secondary'}
            style={{ fontSize: 20, transform: 'rotate(90deg)', marginRight: 5 }}
          />
          <Input
            value={borderRadiusBottomLeft === null ? '' : borderRadiusBottomLeft}
            size={InputSize.SMALL}
            type={InputType.NUMERIC_CONTROLS}
            debounceTimeoutOnControlClick={500}
            style={{ maxWidth: 75 }}
            onChange={(event) => setBorderRadiusBottomLeft(event.value as number)}
            onSubmit={(event) => updateBorderRadius([null, null, event.value as number, null])}
          />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', marginTop: 5 }}>
          <RoundedCornerIcon
            color={'secondary'}
            style={{ fontSize: 20, transform: 'rotate(180deg)', marginRight: 5 }} />
          <Input
            value={borderRadiusBottomRight === null ? '' : borderRadiusBottomRight}
            size={InputSize.SMALL}
            type={InputType.NUMERIC_CONTROLS}
            debounceTimeoutOnControlClick={500}
            style={{ maxWidth: 75 }}
            onChange={(event) => setBorderRadiusBottomRight(event.value as number)}
            onSubmit={(event) => updateBorderRadius([null, null, null, event.value as number])}
          />
        </div>
      </div>
    </div>
  );
}

export default ConfiguratorElementsBorderRadius;
