import React from 'react';

import './configurator-properties.module.scss';
import ConfiguratorSectionHeader from '../configurator-section-header/configurator-section-header';
import SettingsIcon from '@material-ui/icons/Settings';
import spacing from '../../../../../assets/styling/spacing.json';
import { ChromePicker, ColorResult } from 'react-color';
import { DesignerConfiguratorProperty } from '../../enums/designer-configurator-property.enum';
import { Button, Checkbox, makeStyles, Slider, Typography } from '@material-ui/core';
import OpacityIcon from '@material-ui/icons/Opacity';
import { DesignerConfiguratorUtils } from '../../designer-configurator.utils';
import Input, { InputSize, InputType } from '../../../../components/input/input';

export interface ConfiguratorPropertiesProps {
  fillColorPickerVisible: boolean;
  setFillColorPickerVisible: (visibility: boolean) => void;
  fillColor: string | null;
  onFillColorChange: (color: ColorResult, event: React.ChangeEvent<HTMLInputElement>) => void;
  borderColorPickerVisible: boolean;
  setBorderColorPickerVisible: (visibility: boolean) => void;
  borderColor: string | null;
  onBorderColorChange: (color: ColorResult, event: React.ChangeEvent<HTMLInputElement>) => void;
  onColorPickerSubmit: (
    color: ColorResult,
    event: React.ChangeEvent<HTMLInputElement>,
    property: DesignerConfiguratorProperty
  ) => void;
  borderWidth: number | null;
  onPropertyChange: (propertyValue: unknown, property: DesignerConfiguratorProperty) => void;
  onPropertyKeyPress: (propertyValue: unknown, property: DesignerConfiguratorProperty) => void;
  opacity: number | null;
  onOpacitySliderSubmit: () => void;
  backgroundColorEnabled: boolean;
  handleBackgroundColorEnabledChange: (backgroundColorEnabled: boolean) => void;
  borderEnabled: boolean;
  handleBorderEnabledChange: (borderEnabled: boolean) => void;
}

const useStyles = (backgroundColor: string, borderColor: string) => {
  const style = DesignerConfiguratorUtils.getColorPickerButtonStyle();
  return makeStyles({
    backgroundColorPickerButtonRoot: {
      ...style,
      backgroundColor,
      '&:hover': { backgroundColor }
    },
    borderColorPickerButtonRoot: {
      ...style,
      backgroundColor: borderColor,
      '&:hover': { backgroundColor: borderColor }
    }
  });
};

export function ConfiguratorProperties(
  props: ConfiguratorPropertiesProps
) {

  const classes = useStyles(props.fillColor, props.borderColor)();
  return (
    <div>
      <ConfiguratorSectionHeader icon={SettingsIcon} title={'Properties'} />

      <div style={{
        marginBottom: 20,
        paddingLeft: spacing.configurator.padding + 5,
        paddingRight: spacing.configurator.padding + 5
      }}>
        <div style={{ display: 'flex', alignItems: 'center', position: 'relative' }}>
          <div style={{
            position: 'absolute',
            top: 25,
            left: 0,
            zIndex: 1
          }}>
            {props.fillColorPickerVisible
              ? <ChromePicker
                color={props.fillColor}
                onChange={props.onFillColorChange}
                onChangeComplete={(event, color) => props.onColorPickerSubmit(event, color, DesignerConfiguratorProperty.BACKGROUND_COLOR)}
              />
              : null}
          </div>
          <Checkbox
            checked={(props.backgroundColorEnabled === null) ? false : props.backgroundColorEnabled}
            onChange={() => props.handleBackgroundColorEnabledChange(
              (props.backgroundColorEnabled === null) ? true : !props.backgroundColorEnabled
            )}
            color={'secondary'}
            disableRipple={true}
            style={{ marginRight: 5, padding: 0 }}
          />
          <Button onClick={() => props.setFillColorPickerVisible(!props.fillColorPickerVisible)}
                  classes={{ root: classes.backgroundColorPickerButtonRoot }} style={{ marginRight: 10 }} />
          <Typography variant={'body2'} color={'textPrimary'}>Fill</Typography>
        </div>

        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginTop: 5,
          position: 'relative'
        }}>
          <div style={{
            position: 'absolute',
            top: 25,
            left: 0,
            zIndex: 1
          }}>
            {props.borderColorPickerVisible
              ? <ChromePicker
                color={props.borderColor}
                onChange={props.onBorderColorChange}
                onChangeComplete={(event, color) => props.onColorPickerSubmit(event, color, DesignerConfiguratorProperty.BORDER_COLOR)}
              />
              : null}
          </div>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <Checkbox
              checked={(props.borderEnabled === null) ? false : props.borderEnabled}
              onChange={() => props.handleBorderEnabledChange(
                (props.borderEnabled === null) ? true : !props.borderEnabled
              )}
              color={'secondary'}
              disableRipple={true}
              style={{ marginRight: 5, padding: 0 }}
            />
            <Button onClick={() => props.setBorderColorPickerVisible(!props.borderColorPickerVisible)}
                    classes={{ root: classes.borderColorPickerButtonRoot }} style={{ marginRight: 10 }} />
            <Typography variant={'body2'} color={'textPrimary'}>Border</Typography>
          </div>

          <div style={{ display: 'flex', alignItems: 'center' }}>
            <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>width:</Typography>

            <Input
              value={(props.borderWidth === null) ? '' : props.borderWidth}
              size={InputSize.SMALL}
              type={InputType.NUMERIC_CONTROLS}
              debounceTimeoutOnControlClick={500}
              style={{ maxWidth: 75 }}
              onChange={(event) =>
                props.onPropertyChange(event.value, DesignerConfiguratorProperty.BORDER_WIDTH)}
              onSubmit={(event) =>
                props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.BORDER_WIDTH)}
            />
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', marginTop: 10 }}>
          <Slider
            min={0}
            max={1}
            step={0.05}
            value={(props.opacity === null) ? 0 : props.opacity}
            onChange={(event, value: number) =>
              props.onPropertyChange(value, DesignerConfiguratorProperty.OPACITY)}
            onChangeCommitted={(event, value) => props.onOpacitySliderSubmit()}
            color={'secondary'}
            style={{ marginRight: 15 }}
          />
          <OpacityIcon color={'secondary'} style={{ fontSize: 15, marginRight: 10 }} />
          <Input
            value={props.opacity === null ? '' : (props.opacity * 100)}
            size={InputSize.SMALL}
            type={InputType.NUMERIC}
            style={{ maxWidth: 75 }}
            onChange={(event) =>
              props.onPropertyChange(event.value, DesignerConfiguratorProperty.OPACITY)}
            onSubmit={(event) =>
              props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.OPACITY)}
          />
        </div>

      </div>
    </div>
  );
}

export default ConfiguratorProperties;
