import React from 'react';

import './configurator-styling.module.scss';
import ConfiguratorSectionHeader from '../configurator-section-header/configurator-section-header';
import FormatPaintIcon from '@material-ui/icons/FormatPaint';
import spacing from '../../../../../assets/styling/spacing.json';
import { ChromePicker, ColorResult } from 'react-color';
import { DesignerConfiguratorProperty } from '../../enums/designer-configurator-property.enum';
import { Button, Checkbox, makeStyles, Typography } from '@material-ui/core';
import RoundedCornerIcon from '@material-ui/icons/RoundedCorner';
import { DesignerConfiguratorUtils } from '../../designer-configurator.utils';
import Input, { InputSize, InputType } from '../../../../components/input/input';

export interface ConfiguratorStylingProps {
  shadowColorPickerVisible: boolean;
  setShadowColorPickerVisible: (visibility: boolean) => void;
  shadowColor: string | null;
  onShadowColorChange: (color: ColorResult, event: React.ChangeEvent<HTMLInputElement>) => void;
  onColorPickerSubmit: (
    color: ColorResult,
    event: React.ChangeEvent<HTMLInputElement>,
    property: DesignerConfiguratorProperty
  ) => void;
  shadowX: number | null;
  shadowY: number | null;
  shadowBlur: number | null;
  borderRadius: [number | null, number | null, number | null, number | null];
  onPropertyChange: (propertyValue: unknown, property: DesignerConfiguratorProperty) => void;
  onPropertyKeyPress: (propertyValue: unknown, property: DesignerConfiguratorProperty) => void;
  shadowEnabled: boolean;
  handleShadowEnabledChange: (shadowEnabled: boolean) => void;
}

const useStyles = (shadowColor: string) => {
  const style = DesignerConfiguratorUtils.getColorPickerButtonStyle();
  return makeStyles({
    shadowColorPickerButtonRoot: {
      ...style,
      backgroundColor: shadowColor,
      '&:hover': { backgroundColor: shadowColor }
    }
  });
};

export function ConfiguratorStyling(
  props: ConfiguratorStylingProps
) {
  const classes = useStyles(props.shadowColor)();
  return (
    <div>
      <ConfiguratorSectionHeader icon={FormatPaintIcon} title={'Styling'} />

      <div style={{
        marginBottom: 20,
        paddingLeft: spacing.configurator.padding + 5,
        paddingRight: spacing.configurator.padding + 5
      }}>
        <div>
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
              {props.shadowColorPickerVisible ?
                <ChromePicker
                  color={props.shadowColor}
                  onChange={props.onShadowColorChange}
                  onChangeComplete={(event, color) =>
                    props.onColorPickerSubmit(event, color, DesignerConfiguratorProperty.SHADOW_COLOR)}
                /> : null}
            </div>
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <Checkbox
                checked={(props.shadowEnabled === null) ? false : props.shadowEnabled}
                onChange={() => props.handleShadowEnabledChange(
                  (props.shadowEnabled === null) ? true : !props.shadowEnabled
                )}
                color={'secondary'}
                disableRipple={true}
                style={{ marginRight: 5, padding: 0 }}
              />
              <Button onClick={() => props.setShadowColorPickerVisible(!props.shadowColorPickerVisible)}
                      classes={{ root: classes.shadowColorPickerButtonRoot }} style={{ marginRight: 10 }} />
              <Typography variant={'body2'} color={'textPrimary'}>Shadow</Typography>
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginTop: 5 }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>x:</Typography>
                <Input
                  value={(props.shadowX === null) ? '' : props.shadowX}
                  size={InputSize.SMALL}
                  type={InputType.NUMERIC_CONTROLS}
                  debounceTimeoutOnControlClick={500}
                  style={{ maxWidth: 75 }}
                  onChange={(event) =>
                    props.onPropertyChange(event.value, DesignerConfiguratorProperty.SHADOW_X)}
                  onSubmit={(event) =>
                    props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.SHADOW_X)}
                />
              </div>
              <div style={{ display: 'flex', alignItems: 'center', marginTop: 5 }}>
                <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>y:</Typography>
                <Input
                  value={(props.shadowY === null) ? '' : props.shadowY}
                  size={InputSize.SMALL}
                  type={InputType.NUMERIC_CONTROLS}
                  debounceTimeoutOnControlClick={500}
                  style={{ maxWidth: 75 }}
                  onChange={(event) =>
                    props.onPropertyChange(event.value, DesignerConfiguratorProperty.SHADOW_Y)}
                  onSubmit={(event) =>
                    props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.SHADOW_Y)}
                />
              </div>
            </div>
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>blur:</Typography>
              <Input
                value={(props.shadowBlur === null) ? '' : props.shadowBlur}
                size={InputSize.SMALL}
                type={InputType.NUMERIC_CONTROLS}
                debounceTimeoutOnControlClick={500}
                style={{ maxWidth: 75 }}
                onChange={(event) =>
                  props.onPropertyChange(event.value, DesignerConfiguratorProperty.SHADOW_BLUR)}
                onSubmit={(event) =>
                  props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.SHADOW_BLUR)}
              />
            </div>
          </div>

        </div>
        <div style={{ marginTop: 15 }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>

            <div>
              <div style={{ display: 'flex', alignItems: 'center', marginRight: 5 }}>
                <RoundedCornerIcon
                  color={'secondary'}
                  style={{ fontSize: 20, transform: 'rotate(-90deg)', marginRight: 5 }}
                />
                <Input
                  value={props.borderRadius[0] === null ? '' : props.borderRadius[0]}
                  size={InputSize.SMALL}
                  type={InputType.NUMERIC_CONTROLS}
                  debounceTimeoutOnControlClick={500}
                  style={{ maxWidth: 75 }}
                  onChange={(event) =>
                    props.onPropertyChange(event.value, DesignerConfiguratorProperty.BORDER_RADIUS_TOP_LEFT)}
                  onSubmit={(event) =>
                    props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.BORDER_RADIUS_TOP_LEFT)}
                />
              </div>

              <div style={{ display: 'flex', alignItems: 'center', marginRight: 5, marginTop: 5 }}>
                <RoundedCornerIcon
                  color={'secondary'}
                  style={{ fontSize: 20, marginRight: 5 }}
                />
                <Input
                  value={props.borderRadius[1] === null ? '' : props.borderRadius[1]}
                  size={InputSize.SMALL}
                  type={InputType.NUMERIC_CONTROLS}
                  debounceTimeoutOnControlClick={500}
                  style={{ maxWidth: 75 }}
                  onChange={(event) =>
                    props.onPropertyChange(event.value, DesignerConfiguratorProperty.BORDER_RADIUS_TOP_RIGHT)}
                  onSubmit={(event) =>
                    props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.BORDER_RADIUS_TOP_RIGHT)}
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
                  value={props.borderRadius[2] === null ? '' : props.borderRadius[2]}
                  size={InputSize.SMALL}
                  type={InputType.NUMERIC_CONTROLS}
                  debounceTimeoutOnControlClick={500}
                  style={{ maxWidth: 75 }}
                  onChange={(event) =>
                    props.onPropertyChange(event.value, DesignerConfiguratorProperty.BORDER_RADIUS_BOTTOM_LEFT)}
                  onSubmit={(event) =>
                    props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.BORDER_RADIUS_BOTTOM_LEFT)}
                />
              </div>
              <div style={{ display: 'flex', alignItems: 'center', marginTop: 5 }}>
                <RoundedCornerIcon
                  color={'secondary'}
                  style={{ fontSize: 20, transform: 'rotate(180deg)', marginRight: 5 }} />
                <Input
                  value={props.borderRadius[3] === null ? '' : props.borderRadius[3]}
                  size={InputSize.SMALL}
                  type={InputType.NUMERIC_CONTROLS}
                  debounceTimeoutOnControlClick={500}
                  style={{ maxWidth: 75 }}
                  onChange={(event) =>
                    props.onPropertyChange(event.value, DesignerConfiguratorProperty.BORDER_RADIUS_BOTTOM_RIGHT)}
                  onSubmit={(event) =>
                    props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.BORDER_RADIUS_BOTTOM_RIGHT)}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ConfiguratorStyling;
