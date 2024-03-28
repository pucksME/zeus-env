import React, { useEffect, useState } from 'react';

import './configurator-elements-shadow-color.module.scss';
import { PropertiesDto, ShapeDto } from '../../../../../gen/api-client';
import { DesignerConfiguratorUtils } from '../../designer-configurator.utils';
import { Button, Checkbox, makeStyles, Typography } from '@material-ui/core';
import { useQueryClient } from 'react-query';
import { ChromePicker } from 'react-color';
import { DesignerUtils } from '../../designer.utils';
import { useUpdateElementsProperties } from '../../data/component-data.hooks';
import { ShapeUtils } from '../../shape.utils';
import { useStore } from '../../../../store';
import { useUpdateBlueprintElementsProperties } from '../../data/blueprint-component-data.hooks';

export interface ConfiguratorShapesShadowColorProps {
  elementUuids: string[];
  shapes: ShapeDto[];
  workspaceUuid: string;
}

const useStyles = (color: string) => {
  const style = DesignerConfiguratorUtils.getColorPickerButtonStyle();
  return makeStyles({
    backgroundColorPickerButtonRoot: {
      ...style,
      backgroundColor: color,
      '&:hover': { backgroundColor: color }
    }
  });
};

export function ConfiguratorElementsShadowColor(
  props: ConfiguratorShapesShadowColorProps
) {

  const [colorPickerVisible, setColorPickerVisible] = useState(false);
  const [color, setColor] = useState<string | null>(null);
  const [shadowEnabled, setShadowEnabled] = useState<boolean | null>(null);

  const classes = useStyles(color)();

  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);

  const updateElementsProperties = useUpdateElementsProperties(props.workspaceUuid);
  const updateBlueprintElementsProperties = useUpdateBlueprintElementsProperties(props.workspaceUuid);
  const [compatible, setCompatible] = useState(false);

  const updateShadowColor = () =>
    ((!stageBlueprintComponentProperties.active) ? updateElementsProperties : updateBlueprintElementsProperties).mutate({
    parentComponentUuid: (!stageBlueprintComponentProperties.active)
      ? focusedComponentUuid
      : (focusedComponentUuid === null)
        ? stageBlueprintComponentProperties.blueprintComponentUuid
        : focusedComponentUuid,
    elementUuids: props.elementUuids,
    properties: {shadowColor: color}
  });

  const updateShadowEnabled = () => {
    const properties: PropertiesDto = {shadowEnabled: (shadowEnabled === null) ? true : !shadowEnabled};
    ((!stageBlueprintComponentProperties.active) ? updateElementsProperties : updateBlueprintElementsProperties).mutate({
      parentComponentUuid: (!stageBlueprintComponentProperties.active)
        ? focusedComponentUuid
        : (focusedComponentUuid === null)
          ? stageBlueprintComponentProperties.blueprintComponentUuid
          : focusedComponentUuid,
      elementUuids: props.elementUuids,
      properties
    });
    setShadowEnabled(!shadowEnabled);
  };

  useEffect(() => {
    if (props.elementUuids.length === 0) {
      return;
    }

    const sharedProperties = ShapeUtils.getSharedProperties(
      props.shapes, ['shadowEnabled', 'shadowColor']
    );

    if (!sharedProperties.compatible) {
      return setCompatible(false);
    }

    setColor(sharedProperties.properties.shadowColor);
    setShadowEnabled(sharedProperties.properties.shadowEnabled);
    setCompatible(true);
  }, [props.elementUuids]);

  return (!compatible) ? null : (
    <div style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', position: 'relative'}}>
      {colorPickerVisible
        ? <div style={{
          position: 'absolute',
          top: 40,
          left: 0,
          zIndex: 1
        }}>
          <ChromePicker
            color={(color === null) ? '' : color}
            onChange={(color, event) => setColor(DesignerUtils.convertRgbToString(color.rgb))}
            onChangeComplete={updateShadowColor}
          />
        </div>
        : null
      }
      <Checkbox
        checked={(shadowEnabled === null) ? false : shadowEnabled}
        onChange={updateShadowEnabled}
      />
      <Button
        onClick={() => setColorPickerVisible(!colorPickerVisible)}
        classes={{root: classes.backgroundColorPickerButtonRoot}}
        style={{marginRight: 10}}
      />
      <Typography variant={'body2'} color={'textPrimary'}>Shadow</Typography>
    </div>
  );
}

export default ConfiguratorElementsShadowColor;
