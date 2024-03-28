import React, { useEffect, useState } from 'react';

import './configurator-elements-font-size.module.scss';
import { ShapeDto } from '../../../../../gen/api-client';
import { useStore } from '../../../../store';
import Input, { InputSize, InputType } from '../../../../components/input/input';
import { Typography } from '@material-ui/core';
import { ShapeUtils } from '../../shape.utils';

export interface ConfiguratorElementsFontSizeProps {
  elementUuids: string[];
  shapes: ShapeDto[];
  workspaceUuid: string;
}

export function ConfiguratorElementsFontSize(
  props: ConfiguratorElementsFontSizeProps
) {

  const [fontSize, setFontSize] = useState<number | null>(null);
  const [compatible, setCompatible] = useState(false);
  const setTextStyleEditorState = useStore(state => state.setTextStyleEditorState);

  useEffect(() => {
    if (props.elementUuids.length === 0) {
      return;
    }

    const sharedProperties = ShapeUtils.getSharedProperties(
      props.shapes, ['fontSize']
    );

    if (!sharedProperties.compatible) {
      return setCompatible(false);
    }

    setFontSize(sharedProperties.properties.fontSize);
    setCompatible(true);
  }, [props.elementUuids]);

  return (!compatible) ? null : (
    <div style={{display: 'flex', alignItems: 'center'}}>
      <Typography variant={'body2'} color={'textPrimary'} style={{marginRight: 15}}>size:</Typography>
    <Input
      style={{maxWidth: 75}}
      value={(fontSize === null) ? '' : fontSize}
      size={InputSize.SMALL}
      type={InputType.NUMERIC_CONTROLS}
      debounceTimeoutOnControlClick={500}
      onChange={(event) => setFontSize(event.value as number)}
      onSubmit={(event) => setTextStyleEditorState({
        shapes: props.shapes,
        textPropertiesToUpdate: {fontSize: event.value as number}
      })}
    />
    </div>
  );
}

export default ConfiguratorElementsFontSize;
