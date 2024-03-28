import React from 'react';

import './form-creator-preview-circle.module.scss';
import colors from '../../../../../assets/styling/colors.json';
import { ToolType } from '../../../../enums/tool-type.enum';
import { Ellipse } from 'react-konva';
import { useStore } from '../../../../store';

/* eslint-disable-next-line */
export interface FormCreatorPreviewCircleProps {}

export function FormCreatorPreviewCircle(props: FormCreatorPreviewCircleProps) {

  const createFormPreviewProperties = useStore(state => state.createFormPreviewProperties);

  return (
    <Ellipse
      radiusX={Math.abs(createFormPreviewProperties.width) / 2}
      radiusY={Math.abs(createFormPreviewProperties.height) / 2}
      x={createFormPreviewProperties.positionX + (createFormPreviewProperties.width / 2)}
      y={createFormPreviewProperties.positionY + (createFormPreviewProperties.height / 2)}
      fill={colors.secondary.main}
      visible={createFormPreviewProperties.toolUsed === ToolType.CIRCLE_FORM_CREATOR}
    />
  );
}

export default FormCreatorPreviewCircle;
