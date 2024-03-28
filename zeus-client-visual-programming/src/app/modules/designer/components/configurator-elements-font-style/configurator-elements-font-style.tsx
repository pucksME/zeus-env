import React, { useEffect, useState } from 'react';

import './configurator-elements-font-style.module.scss';
import { FontStyle, ShapeDto } from '../../../../../gen/api-client';
import { useStore } from '../../../../store';
import { Button, ButtonGroup } from '@material-ui/core';
import FormatBoldIcon from '@material-ui/icons/FormatBold';
import FormatItalicIcon from '@material-ui/icons/FormatItalic';
import { ShapeUtils } from '../../shape.utils';

export interface ConfiguratorElementsFontStyleProps {
  elementUuids: string[];
  shapes: ShapeDto[];
  workspaceUuid: string;
}

export function ConfiguratorElementsFontStyle(
  props: ConfiguratorElementsFontStyleProps
) {

  const [fontStyle, setFontStyle] = useState<FontStyle>(null);

  const setTextStyleEditorState = useStore(state => state.setTextStyleEditorState);

  const [compatible, setCompatible] = useState(false);

  useEffect(() => {
    if (props.elementUuids.length === 0) {
      return;
    }

    const sharedProperties = ShapeUtils.getSharedProperties(props.shapes, ['fontStyle']);

    if (!sharedProperties.compatible) {
      return setCompatible(false);
    }

    setFontStyle(sharedProperties.properties.fontStyle);
    setCompatible(true);
  }, [props.elementUuids]);

  const handleClick = (fontStyle: FontStyle) => {
    setTextStyleEditorState({
      shapes: props.shapes,
      textPropertiesToUpdate: {fontStyle}
    });
    setFontStyle(fontStyle);
  };

  return (!compatible) ? null : (
    <div>
      <ButtonGroup size={'small'} variant={'text'}>
        <Button onClick={() => handleClick((fontStyle === FontStyle.Bold) ? FontStyle.Normal : FontStyle.Bold)}>
          <FormatBoldIcon color={(fontStyle === FontStyle.Bold) ? 'secondary' : undefined} fontSize={'small'}/>
        </Button>
        <Button onClick={() => handleClick((fontStyle === FontStyle.Italic) ? FontStyle.Normal : FontStyle.Italic)}>
          <FormatItalicIcon color={(fontStyle === FontStyle.Italic) ? 'secondary' : undefined} fontSize={'small'}/>
        </Button>
      </ButtonGroup>
    </div>
  );
}

export default ConfiguratorElementsFontStyle;
