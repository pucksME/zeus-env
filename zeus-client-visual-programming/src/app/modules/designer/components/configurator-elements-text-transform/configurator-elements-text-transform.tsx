import React, { useEffect, useState } from 'react';

import './configurator-elements-text-transform.module.scss';
import { ShapeDto, TextTransform } from '../../../../../gen/api-client';
import { useStore } from '../../../../store';
import { Button, ButtonGroup } from '@material-ui/core';
import TextFieldsIcon from '@material-ui/icons/TextFields';
import { ShapeUtils } from '../../shape.utils';

export interface ConfiguratorElementsTextTransformProps {
  elementUuids: string[];
  shapes: ShapeDto[];
  workspaceUuid: string;
}

export function ConfiguratorElementsTextTransform(
  props: ConfiguratorElementsTextTransformProps
) {

  const [textTransform, setTextTransform] = useState<TextTransform>(null);

  const setTextStyleEditorState = useStore(state => state.setTextStyleEditorState);

  const [compatible, setCompatible] = useState(false);

  useEffect(() => {
    if (props.elementUuids.length === 0) {
      return;
    }

    const sharedProperties = ShapeUtils.getSharedProperties(props.shapes, ['textTransform']);

    if (!sharedProperties.compatible) {
      return setCompatible(false);
    }

    setTextTransform(sharedProperties.properties.textTransform);
    setCompatible(true);
  }, [props.elementUuids]);

  const handleClick = (textTransform: TextTransform) => {
    setTextStyleEditorState({
      shapes: props.shapes,
      textPropertiesToUpdate: {textTransform}
    });
    setTextTransform(textTransform);
  };

  return (!compatible) ? null : (
    <div>
      <ButtonGroup size={'small'} variant={'text'}>
        <Button onClick={() => handleClick((textTransform === TextTransform.Uppercase) ? TextTransform.None : TextTransform.Uppercase)}>
          <TextFieldsIcon color={(textTransform === TextTransform.Uppercase) ? 'secondary' : undefined} fontSize={'small'}/>
        </Button>
      </ButtonGroup>
    </div>
  );
}

export default ConfiguratorElementsTextTransform;
