import React, { useEffect, useState } from 'react';

import './configurator-elements-text-align.module.scss';
import { ShapeDto, TextAlign } from '../../../../../gen/api-client';
import FormatAlignLeftIcon from '@material-ui/icons/FormatAlignLeft';
import FormatAlignCenterIcon from '@material-ui/icons/FormatAlignCenter';
import FormatAlignRightIcon from '@material-ui/icons/FormatAlignRight';
import { useStore } from '../../../../store';
import { Button, ButtonGroup } from '@material-ui/core';
import { ShapeUtils } from '../../shape.utils';

export interface ConfiguratorElementsTextAlignProps {
  elementUuids: string[];
  shapes: ShapeDto[];
  workspaceUuid: string;
}

export function ConfiguratorElementsTextAlign(
  props: ConfiguratorElementsTextAlignProps
) {

  const [textAlign, setTextAlign] = useState<TextAlign>(null);

  const [compatible, setCompatible] = useState(false);

  const setTextStyleEditorState = useStore(state => state.setTextStyleEditorState);

  useEffect(() => {
    if (props.elementUuids.length === 0) {
      return;
    }

    const sharedProperties = ShapeUtils.getSharedProperties(
      props.shapes, ['textAlign']
    );

    if (!sharedProperties.compatible) {
      return setCompatible(false);
    }

    setTextAlign(sharedProperties.properties.textAlign);
    setCompatible(true);
  }, [props.elementUuids]);

  const handleClick = (textAlign: TextAlign) => {
    setTextStyleEditorState({
      shapes: props.shapes,
      textPropertiesToUpdate: {textAlign}
    });
    setTextAlign(textAlign);
  };

  return (!compatible) ? null : (
    <div>
      <ButtonGroup size={'small'} variant={'text'}>
        <Button onClick={() => handleClick(TextAlign.Left)}>
          <FormatAlignLeftIcon color={(textAlign === TextAlign.Left) ? 'secondary' : undefined} fontSize={'small'}/>
        </Button>
        <Button onClick={() => handleClick(TextAlign.Center)}>
          <FormatAlignCenterIcon color={(textAlign === TextAlign.Center) ? 'secondary' : undefined} fontSize={'small'}/>
        </Button>
        <Button onClick={() => handleClick(TextAlign.Right)}>
          <FormatAlignRightIcon color={(textAlign === TextAlign.Right) ? 'secondary' : undefined} fontSize={'small'}/>
        </Button>
      </ButtonGroup>
    </div>
  );
}

export default ConfiguratorElementsTextAlign;
