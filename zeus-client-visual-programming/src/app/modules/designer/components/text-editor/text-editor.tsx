import React, { useEffect, useRef, useState } from 'react';

import './text-editor.module.scss';
import { InputBase } from '@material-ui/core';
import { useStore } from '../../../../store';
import {
  BlueprintComponentDto,
  ComponentDto,
  FontFamily,
  FontStyle,
  ShapeType,
  TextAlign,
  TextDecoration,
  TextPropertiesDto,
  TextTransform,
  WorkspaceDesignerDto
} from '../../../../../gen/api-client';
import { useSaveComponent } from '../../data/component-data.hooks';
import { useSaveShape, useUpdateShapesProperties } from '../../data/shape-data.hooks';
import { Layer, Stage, Text } from 'react-konva';
import Konva from 'konva';
import { ShapeUtils } from '../../shape.utils';
import spacing from '../../../../../assets/styling/spacing.json';
import { AppUtils } from '../../../../app.utils';
import { useBlueprintComponentsWorkspace } from '../../data/blueprint-component-data.hooks';
import { StageMode } from '../../../../enums/stage-mode.enum';

export interface TextEditorProps {
  workspace: WorkspaceDesignerDto;
  stageMode: StageMode.DESIGNER | StageMode.DESIGNER_BLUEPRINT_COMPONENT;
}

const defaultTextProperties: TextPropertiesDto = {
  text: '',
  fontFamily: FontFamily.Arial,
  fontSize: 20,
  fontStyle: FontStyle.Normal,
  textDecoration: TextDecoration.None,
  textTransform: TextTransform.None,
  textAlign: TextAlign.Left,
  height: undefined,
  width: undefined,
  backgroundColorEnabled: true,
  backgroundColor: '#000000',
  borderEnabled: false,
  borderColor: '#000000',
  borderWidth: 0,
  shadowEnabled: false,
  shadowColor: '#000000',
  shadowX: 0,
  shadowY: 0,
  shadowBlur: 0,
  opacity: 1,
  visible: true
};

export function TextEditor(props: TextEditorProps) {

  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const textEditorState = useStore(state => state.textEditorState);
  const resetTextEditorState = useStore(state => state.resetTextEditorState);
  const stageProperties = useStore(state => state.designerStageProperties);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);

  const { isLoading, isError, blueprintComponentDtos } = useBlueprintComponentsWorkspace(props.workspace.uuid);

  const stageModeStageProperties = (!stageBlueprintComponentProperties.active)
    ? stageProperties
    : stageBlueprintComponentProperties;

  const view = (activeViewUuid === null) ? undefined : props.workspace.views.find(view => view.uuid === activeViewUuid);

  let componentContainer: {
    components: ComponentDto[] | BlueprintComponentDto[],
    positionX: number,
    positionY: number
  } | null = null;

  const [value, setValue] = useState<string>('');

  const saveComponent = useSaveComponent(props.workspace);
  const saveShape = useSaveShape(props.workspace);
  const updateShapesProperties = useUpdateShapesProperties(props.workspace.uuid);

  const textRef = useRef<Konva.Text>(null);
  const textProperties = textEditorState.shape !== null
    ? (textEditorState.shape.properties as TextPropertiesDto)
    : defaultTextProperties;

  useEffect(() => {
    setValue('');
  }, [textEditorState.position]);

  useEffect(() => {
    if (textEditorState.shape === null) {
      return;
    }
    setValue((textEditorState.shape.properties as TextPropertiesDto).text);
  }, [textEditorState.shape]);

  if ((props.stageMode === StageMode.DESIGNER && stageBlueprintComponentProperties.active) ||
    (props.stageMode === StageMode.DESIGNER_BLUEPRINT_COMPONENT && !stageBlueprintComponentProperties.active)) {
    return null;
  }

  if (!stageBlueprintComponentProperties.active) {
    if (view === undefined) {
      return null;
    }

    componentContainer = {
      components: view.components,
      positionX: view.positionX,
      positionY: view.positionY
    };
  } else {
    if (isLoading || isError) {
      return null;
    }

    const blueprintComponent = blueprintComponentDtos.find(
      blueprintComponent => blueprintComponent.uuid === stageBlueprintComponentProperties.blueprintComponentUuid
    );

    if (blueprintComponent === undefined) {
      return null;
    }

    componentContainer = {
      components: [blueprintComponent],
      positionX: 0,
      positionY: 0
    };
  }

  const handleSubmit = () => {

    if (textRef.current === null) {
      return;
    }

    const textClientRect = textRef.current.getClientRect();

    if (textEditorState.shape !== null) {
      updateShapesProperties.mutate({
        shapeIdentifiers: [ShapeUtils.buildShapeIdentifierDto(textEditorState.shape)],
        properties: {text: value, height: textClientRect.height, width: textClientRect.width}
      });
      resetTextEditorState();
      return;
    }

    const textComponentPosition = {
      x: ((
        textEditorState.position.x - spacing.toolbar.width - spacing.toolbox.width - stageModeStageProperties.x
      ) / stageModeStageProperties.scale) - componentContainer.positionX,
      y: ((
        textEditorState.position.y - spacing.navigation.height - stageModeStageProperties.y
      ) / stageModeStageProperties.scale) - componentContainer.positionY
    };

    if (focusedComponentUuid === null) {
      saveComponent.mutate({
        viewUuid: view.uuid,
        createComponentDto: {
          positionX: textComponentPosition.x,
          positionY: textComponentPosition.y,
          shapes: [{
            positionX: 0,
            positionY: 0,
            type: ShapeType.Text,
            properties: {
              ...textProperties,
              height: textClientRect.height,
              width: textClientRect.width,
              text: value
            }
          }]
        }
      });
      resetTextEditorState();
      return;
    }

    const {node, pathCoordinates} = AppUtils.findInTrees<ComponentDto | BlueprintComponentDto>(
      componentContainer.components,
      focusedComponentUuid
    );

    if (node === null) {
      return;
    }

    saveShape.mutate({
      componentUuid: node.uuid,
      createShapeDto: {
        positionX: textComponentPosition.x - (pathCoordinates.x + node.positionX),
        positionY: textComponentPosition.y - (pathCoordinates.y + node.positionY),
        type: ShapeType.Text,
        properties: {
          ...textProperties,
          height: textClientRect.height,
          width: textClientRect.width,
          text: value
        }
      }
    });

    resetTextEditorState();

  };

  const handleKeyPress = (event: React.KeyboardEvent<HTMLInputElement>) => {

    if (event.shiftKey && event.code === 'Enter') {
      return;
    }

    if (event.code === 'Enter') {
      event.preventDefault();
      handleSubmit();
      return;
    }
  };

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {

    setValue(event.target.value);

  };

  if (!textEditorState.active || (!stageBlueprintComponentProperties.active && activeViewUuid === null)) {
    return null;
  }

  return (
    <div style={{
      fontSize: 0,
      position: 'absolute',
      left: textEditorState.position.x,
      top: textEditorState.position.y - ((!stageBlueprintComponentProperties.active)
        ? spacing.navigation.height
        : 0) - 1.4 * stageModeStageProperties.scale,
      width: '100%'
    }}>
      <InputBase
        style={{
          paddingBottom: 0,
          paddingTop: 0,
          width: '100%'
        }}
        inputProps={{
          style: {
            lineHeight: textProperties.fontSize * stageModeStageProperties.scale + 'px',
            ...ShapeUtils.mapTextPropertiesToCssProperties(textProperties),
            fontSize: textProperties.fontSize * stageModeStageProperties.scale,
            width: '100%'
          }
        }}
        autoFocus={true}
        multiline={true}
        onKeyPress={handleKeyPress}
        onChange={handleChange}
        value={value}
        onMouseDown={(event) => event.stopPropagation()}
      />
      <Stage visible={false}>
        <Layer>
          <Text
            {...ShapeUtils.buildTextConfig({
              uuid: undefined,
              name: undefined,
              positionX: undefined,
              positionY: undefined,
              sorting: -1,
              type: ShapeType.Text,
              isMutated: false,
              properties: { ...textProperties, height: undefined, width: undefined }
            })}
            ref={textRef}
            text={
              (textEditorState.shape !== null && textProperties.textTransform === TextTransform.Uppercase)
                ? value.toUpperCase()
                : value
            }
          />
        </Layer>
      </Stage>
    </div>
  );
}

export default TextEditor;
