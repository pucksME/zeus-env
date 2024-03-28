import React from 'react';

import './form-creator-preview-rectangle.module.scss';
import colors from '../../../../../assets/styling/colors.json';
import { ToolType } from '../../../../enums/tool-type.enum';
import { Rect } from 'react-konva';
import { useStore } from '../../../../store';

/* eslint-disable-next-line */
export interface FormCreatorPreviewRectangleProps {}

export function FormCreatorPreviewRectangle(
  props: FormCreatorPreviewRectangleProps
) {

  const createFormPreviewProperties = useStore(state => state.createFormPreviewProperties);

  return (
    <Rect
      height={createFormPreviewProperties.height}
      width={createFormPreviewProperties.width}
      x={createFormPreviewProperties.positionX}
      y={createFormPreviewProperties.positionY}
      fill={colors.secondary.main}
      visible={createFormPreviewProperties.toolUsed === ToolType.RECTANGLE_FORM_CREATOR}
    />
  );
}

export default FormCreatorPreviewRectangle;
