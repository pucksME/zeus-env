import React from 'react';

import './code-module-instance-port.module.scss';
import { CodeModuleEndpointDto, CodeModuleInstanceDto } from '../../../../../gen/api-client';
import { Ellipse, Group, Text } from 'react-konva';
import spacing from '../../../../../assets/styling/spacing.json';
import colors from '../../../../../assets/styling/colors.json';
import Konva from 'konva';
import { useStore } from '../../../../store';
import { ToolType } from '../../../../enums/tool-type.enum';
import { CodeModuleInstanceUtils } from '../../code-module-instance.utils';

export interface CodeModuleInstancePortProps {
  index: number;
  codeModuleInstanceDto: CodeModuleInstanceDto;
  codeModuleEndpointDto: CodeModuleEndpointDto;
  isOutput?: boolean;
}

export function CodeModuleInstancePort(props: CodeModuleInstancePortProps) {
  const activeVisualizerTool = useStore(state => state.activeVisualizerTool);
  const codeModuleInstancesConnectionEditorState = useStore(state => state.codeModuleInstancesConnectionEditorState);
  const setCodeModuleInstancesConnectionEditorState = useStore(state => state.setCodeModuleInstancesConnectionEditorState);
  const textWidth = spacing.codeModuleInstance.width / 2;
  const portHeight = CodeModuleInstanceUtils.calculatePortHeight();

  const handleClick = (event: Konva.KonvaEventObject<MouseEvent>) => {
    if (activeVisualizerTool !== ToolType.POINTER) {
      return;
    }

    setCodeModuleInstancesConnectionEditorState({
      ...codeModuleInstancesConnectionEditorState,
      [props.isOutput ? 'output' : 'input']: {
        codeModuleInstanceUuid: props.codeModuleInstanceDto.uuid,
        codeModuleName: props.codeModuleInstanceDto.codeModule.name,
        portName: props.codeModuleEndpointDto.name
      }
    });
  };

  const handleMouseDown = (event: Konva.KonvaEventObject<MouseEvent>) => event.cancelBubble = true;

  return (
    <Group
      x={(props.isOutput) ? spacing.codeModuleInstance.port.radius : undefined}
      y={props.index * portHeight}
    >
      <Text
        fill={colors.text.primary}
        text={props.codeModuleEndpointDto.name}
        width={textWidth}
        align={(props.isOutput) ? 'right' : 'left'}
        x={(!props.isOutput)
          ? spacing.codeModuleInstance.paddingHorizontal + spacing.codeModuleInstance.port.radius
          : undefined}
        y={spacing.codeModuleInstance.port.margin / 2}
        fontSize={spacing.codeModuleInstance.port.fontSize}
      />
      <Ellipse
        fill={colors.secondary.main}
        radiusX={spacing.codeModuleInstance.port.radius}
        radiusY={spacing.codeModuleInstance.port.radius}
        x={(props.isOutput)
          ? spacing.codeModuleInstance.paddingHorizontal + textWidth + spacing.codeModuleInstance.port.radius
          : undefined}
        y={CodeModuleInstanceUtils.calculatePortHeight() / 2}
        onClick={handleClick}
        onMouseDown={handleMouseDown}
      />
    </Group>
  );
}

export default CodeModuleInstancePort;
