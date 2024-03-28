import React, { useEffect, useState } from 'react';

import './code-module-instances-connection-editor.module.scss';
import { useStore } from '../../../../store';
import { Line } from 'react-konva';
import { CodeModuleInstanceDto } from '../../../../../gen/api-client';
import spacing from '../../../../../assets/styling/spacing.json';
import colors from '../../../../../assets/styling/colors.json';
import { useSaveConnection } from '../../data/code-module-instance-data.hooks';
import { CodeModuleInstanceUtils } from '../../code-module-instance.utils';

/* eslint-disable-next-line */
export interface CodeModuleInstancesConnectionEditorProps {
  componentUuid: string;
  codeModuleInstanceDtos: CodeModuleInstanceDto[];
}

export function CodeModuleInstancesConnectionEditor(
  props: CodeModuleInstancesConnectionEditorProps
) {
  const visualizerStageProperties = useStore(state => state.visualizerStageProperties);
  const codeModuleInstancesConnectionEditorState = useStore(state => state.codeModuleInstancesConnectionEditorState);
  const setCodeModuleInstancesConnectionEditorState = useStore(state => state.setCodeModuleInstancesConnectionEditorState);
  const [mousePosition, setMousePosition] = useState<{x: number, y: number}>({x: 0, y: 0});
  const saveConnection = useSaveConnection(props.componentUuid);

  useEffect(() => {
    const mouseMoveEventListener = (event) => setMousePosition({x: event.x, y: event.y});
    window.addEventListener('mousemove', mouseMoveEventListener);
    return () => window.removeEventListener('mousemove', mouseMoveEventListener);
  }, []);

  useEffect(() => {
    if (codeModuleInstancesConnectionEditorState.input === null ||
      codeModuleInstancesConnectionEditorState.output === null) {
      return;
    }

    if (codeModuleInstancesConnectionEditorState.input.codeModuleInstanceUuid ===
      codeModuleInstancesConnectionEditorState.output.codeModuleInstanceUuid) {
      setCodeModuleInstancesConnectionEditorState({input: null, output: null});
      return;
    }

    saveConnection.mutate({
      input: codeModuleInstancesConnectionEditorState.input,
      output: codeModuleInstancesConnectionEditorState.output
    });

    setCodeModuleInstancesConnectionEditorState({input: null, output: null});
  })

  if (codeModuleInstancesConnectionEditorState.input === null &&
    codeModuleInstancesConnectionEditorState.output === null) {
    return null;
  }

  const buildPoints = () => {
    const codeModuleInstancePort = (codeModuleInstancesConnectionEditorState.input !== null)
      ? codeModuleInstancesConnectionEditorState.input
      : codeModuleInstancesConnectionEditorState.output;

    const codeModuleInstanceDto = props.codeModuleInstanceDtos.find(
      codeModuleInstanceDto => codeModuleInstanceDto.uuid === codeModuleInstancePort.codeModuleInstanceUuid
    );

    if (codeModuleInstanceDto === undefined) {
      return [0, 0, 0, 0];
    }

    const portPosition = CodeModuleInstanceUtils.getPortPosition(
      codeModuleInstanceDto,
      codeModuleInstancePort,
      codeModuleInstancesConnectionEditorState.input === null
    );

    const start: [number, number] = [portPosition.x, portPosition.y];
    const end: [number, number] = [
      (mousePosition.x - spacing.toolbar.width - spacing.toolbox.width - visualizerStageProperties.x) / visualizerStageProperties.scale,
      (mousePosition.y - spacing.navigation.height - visualizerStageProperties.y) / visualizerStageProperties.scale
    ]

    return [...start, ...CodeModuleInstanceUtils.calculateCurvePoints(start, end), ...end];
  };

  return (
    <Line
      points={buildPoints()}
      bezier={true}
      stroke={colors.border_main}
      strokeWidth={2}
      strokeScaleEnabled={false}
    />
  );
}

export default CodeModuleInstancesConnectionEditor;
