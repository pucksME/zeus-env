import React, { useEffect, useRef } from 'react';

import './text-style-editor.module.scss';
import {
  IndividualShapePropertiesDto,
  ShapeType, WorkspaceDesignerDto
} from '../../../../../gen/api-client';
import { Layer, Stage } from 'react-konva';
import { useStore } from '../../../../store';
import Konva from 'konva';
import { DesignerStageUtils } from '../../designer-stage.utils';
import { useUpdateShapesProperties } from '../../data/shape-data.hooks';
import { ShapeUtils } from '../../shape.utils';

export interface TextStyleEditorProps {
  workspace: WorkspaceDesignerDto;
}

export function TextStyleEditor(props: TextStyleEditorProps) {

  const textStyleEditorState = useStore(state => state.textStyleEditorState);
  const resetTextStyleEditorState = useStore(state => state.resetTextStyleEditorState);

  const stageRef = useRef<Konva.Stage | null>(null);

  const updateShapesProperties = useUpdateShapesProperties(props.workspace.uuid);

  const shapeDtos = (textStyleEditorState === null)
    ? []
    : textStyleEditorState.shapes.filter(shapeDto => shapeDto.type === ShapeType.Text);

  useEffect(() => {
    if (textStyleEditorState === null || shapeDtos.length === 0 || stageRef.current === null) {
      return;
    }

    const individualProperties: IndividualShapePropertiesDto[] =
      ((stageRef.current.getChildren()[0] as Konva.Layer).getChildren() as Konva.Text[]).map(
        textShape => ({
          shapeUuid: textShape.id(),
          properties: {height: textShape.getClientRect().height, width: textShape.getClientRect().width}
        })
      )

    updateShapesProperties.mutate({
      shapeIdentifiers: ShapeUtils.buildShapeIdentifierDtos(shapeDtos),
      properties: textStyleEditorState.textPropertiesToUpdate,
      individualProperties
    });

    resetTextStyleEditorState();

  }, [textStyleEditorState]);

  if (textStyleEditorState === null) {
    return null;
  }

  return (
    <Stage ref={stageRef} visible={false}>
      <Layer>
      {DesignerStageUtils.buildTextShapes(shapeDtos.map(shapeDto => ({
        ...shapeDto,
        properties: {
          ...shapeDto.properties,
          ...textStyleEditorState.textPropertiesToUpdate,
          height: undefined,
          width: undefined
        }
      })))}
      </Layer>
    </Stage>
  );
}

export default TextStyleEditor;
