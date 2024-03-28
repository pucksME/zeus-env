import React from 'react';

import './configurator-position-and-dimensions.module.scss';
import ConfiguratorSectionHeader from '../configurator-section-header/configurator-section-header';
import TuneIcon from '@material-ui/icons/Tune';
import spacing from '../../../../../assets/styling/spacing.json';
import { Typography } from '@material-ui/core';
import { DesignerConfiguratorProperty } from '../../enums/designer-configurator-property.enum';
import Input, { InputSize, InputType } from '../../../../components/input/input';
import { SelectedElementsProperties } from '../../interfaces/selected-elements-properties.interface';

export interface ConfiguratorPositionAndDimensionsProps {
  selectedComponentsProperties: SelectedElementsProperties;
  onPropertyChange: (propertyValue: unknown, property: DesignerConfiguratorProperty) => void;
  onPropertyKeyPress: (propertyValue: unknown, property: DesignerConfiguratorProperty) => void;
}

export function ConfiguratorPositionAndDimensions(
  props: ConfiguratorPositionAndDimensionsProps
) {
  return (
    <div>
      <ConfiguratorSectionHeader icon={TuneIcon} title={'General'} />

      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        marginBottom: 20,
        paddingLeft: spacing.configurator.padding + 5,
        paddingRight: spacing.configurator.padding + 5
      }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', marginRight: 25 }}>
            <Typography variant={'body1'} color={'textPrimary'} style={{ marginRight: 10 }}>x:</Typography>
            <Input
              value={props.selectedComponentsProperties === null ? '' : props.selectedComponentsProperties.x}
              size={InputSize.SMALL}
              type={InputType.NUMERIC}
              style={{ maxWidth: 75 }}
              onChange={(event) =>
                props.onPropertyChange(event.value, DesignerConfiguratorProperty.POSITION_X)}
              onSubmit={(event) =>
                props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.POSITION_X)}
            />
          </div>
          <div style={{ display: 'flex', alignItems: 'center', marginTop: 5 }}>
            <Typography variant={'body1'} color={'textPrimary'} style={{ marginRight: 10 }}>y:</Typography>
            <Input
              value={props.selectedComponentsProperties === null ? '' : props.selectedComponentsProperties.y}
              size={InputSize.SMALL}
              type={InputType.NUMERIC}
              style={{ maxWidth: 75 }}
              onChange={(event) =>
                props.onPropertyChange(event.value, DesignerConfiguratorProperty.POSITION_Y)}
              onSubmit={(event) =>
                props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.POSITION_Y)}
            />
          </div>
        </div>

        <div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
            <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>width:</Typography>
            <Input
              value={props.selectedComponentsProperties === null ? '' : props.selectedComponentsProperties.width}
              size={InputSize.SMALL}
              type={InputType.NUMERIC}
              style={{ maxWidth: 75 }}
              onChange={(event) =>
                props.onPropertyChange(event.value, DesignerConfiguratorProperty.WIDTH)}
              onSubmit={(event) =>
                props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.WIDTH)}
            />
          </div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', marginTop: 5 }}>
            <Typography variant={'body2'} color={'textPrimary'} style={{ marginRight: 15 }}>height:</Typography>
            <Input
              value={props.selectedComponentsProperties === null ? '' : props.selectedComponentsProperties.height}
              size={InputSize.SMALL}
              type={InputType.NUMERIC}
              style={{ maxWidth: 75 }}
              onChange={(event) =>
                props.onPropertyChange(event.value, DesignerConfiguratorProperty.HEIGHT)}
              onSubmit={(event) =>
                props.onPropertyKeyPress(event.value, DesignerConfiguratorProperty.HEIGHT)}
            />
          </div>
        </div>
      </div>
    </div>
  );
}

export default ConfiguratorPositionAndDimensions;
