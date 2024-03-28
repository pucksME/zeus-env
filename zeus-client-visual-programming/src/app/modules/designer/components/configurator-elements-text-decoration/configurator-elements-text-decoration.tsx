import React, { useEffect, useState } from 'react';

import './configurator-elements-text-decoration.module.scss';
import { ShapeDto, TextDecoration } from '../../../../../gen/api-client';
import { useStore } from '../../../../store';
import { Button, ButtonGroup } from '@material-ui/core';
import FormatUnderlinedIcon from '@material-ui/icons/FormatUnderlined';
import FormatStrikethroughIcon from '@material-ui/icons/FormatStrikethrough';
import { ShapeUtils } from '../../shape.utils';

export interface ConfiguratorElementsTextDecorationProps {
  elementUuids: string[];
  shapes: ShapeDto[];
  workspaceUuid: string;
}

export function ConfiguratorElementsTextDecoration(
  props: ConfiguratorElementsTextDecorationProps
) {

  const [textDecoration, setTextDecoration] = useState<TextDecoration>(null);

  const [compatible, setCompatible] = useState(false);

  const setTextStyleEditorState = useStore(state => state.setTextStyleEditorState);

  useEffect(() => {
    if (props.elementUuids.length === 0) {
      return;
    }

    const sharedProperties = ShapeUtils.getSharedProperties(
      props.shapes, ['textDecoration']
    );

    if (!sharedProperties.compatible) {
      return setCompatible(false);
    }

    setTextDecoration(sharedProperties.properties.textDecoration);
    setCompatible(true);
  }, [props.elementUuids]);

  const handleClick = (textDecoration: TextDecoration) => {
    setTextStyleEditorState({
      shapes: props.shapes,
      textPropertiesToUpdate: {textDecoration}
    });
    setTextDecoration(textDecoration);
  };

  return (!compatible) ? null : (
    <div>
      <ButtonGroup size={'small'} variant={'text'}>
        <Button onClick={() => handleClick(
          (textDecoration === TextDecoration.Underline)
            ? TextDecoration.None
            : TextDecoration.Underline
        )}>
          <FormatUnderlinedIcon color={
            (textDecoration === TextDecoration.Underline)
              ? 'secondary'
              : undefined
          } fontSize={'small'}/>
        </Button>
        <Button onClick={
          () => handleClick(
          (textDecoration === TextDecoration.StrikeThrough)
            ? TextDecoration.None
            : TextDecoration.StrikeThrough
          )
        }>
          <FormatStrikethroughIcon color={
            (textDecoration === TextDecoration.StrikeThrough)
              ? 'secondary'
              : undefined
          } fontSize={'small'}/>
        </Button>
      </ButtonGroup>
    </div>
  );
}

export default ConfiguratorElementsTextDecoration;
