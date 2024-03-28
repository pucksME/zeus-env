import React, { useEffect, useRef, useState } from 'react';

import './code-module-instance.module.scss';
import { Group, Line, Rect, Text } from 'react-konva';
import { CodeModuleInstanceDto } from '../../../../../gen/api-client';
import colors from '../../../../../assets/styling/colors.json';
import spacing from '../../../../../assets/styling/spacing.json';
import CodeModuleInstancePort from '../code-module-instance-port/code-module-instance-port';
import Konva from 'konva';
import { useStore } from '../../../../store';
import { CodeModuleInstanceUtils } from '../../code-module-instance.utils';

export interface CodeModuleInstanceProps {
  codeModuleInstanceDto: CodeModuleInstanceDto;
}

export function CodeModuleInstance(props: CodeModuleInstanceProps) {
  const visualizerStageProperties = useStore(state => state.visualizerStageProperties);
  const widthPadding = spacing.codeModuleInstance.width - (2 * spacing.codeModuleInstance.paddingHorizontal);
  const outputsGroupRef = useRef<Konva.Group | null>(null);
  const [outputsWidth, setOutputsWidth] = useState<number>(0);

  const mainDetailsHeight = CodeModuleInstanceUtils.calculateMainDetailsHeight();
  const delimiterHeight = CodeModuleInstanceUtils.calculateDelimiterHeight();
  const height = mainDetailsHeight +
    delimiterHeight +
    CodeModuleInstanceUtils.calculatePortsHeight(props.codeModuleInstanceDto) +
    spacing.codeModuleInstance.paddingVertical;

  useEffect(() => {
    if (outputsGroupRef.current === null) {
      return;
    }

    setOutputsWidth(outputsGroupRef.current.getClientRect().width / visualizerStageProperties.scale);
  }, [outputsGroupRef.current]);

  const buildPorts = (codeModuleInstanceDto: CodeModuleInstanceDto, isOutput = false) => ((isOutput)
    ? codeModuleInstanceDto.codeModule.outputEndpoints
    : codeModuleInstanceDto.codeModule.inputEndpoints).map(
    (codeModuleEndpointDto, index) => <CodeModuleInstancePort
      key={index}
      index={index}
      codeModuleInstanceDto={codeModuleInstanceDto}
      codeModuleEndpointDto={codeModuleEndpointDto}
      isOutput={isOutput}
    />
  );

  return (
    <Group
      id={props.codeModuleInstanceDto.uuid}
      x={props.codeModuleInstanceDto.positionX}
      y={props.codeModuleInstanceDto.positionY}
    >
      <Rect
        fill={'#ffffff'}
        width={spacing.codeModuleInstance.width}
        height={height}
        cornerRadius={15}
      />
      <Group
        x={spacing.codeModuleInstance.paddingHorizontal}
        y={spacing.codeModuleInstance.paddingVertical}
      >
        <Text
          fill={colors.text.primary}
          width={widthPadding}
          text={props.codeModuleInstanceDto.codeModule.name}
          fontSize={spacing.codeModuleInstance.nameFontSize}
        />
        <Text
          fill={colors.text.secondary}
          y={spacing.codeModuleInstance.nameFontSize + spacing.codeModuleInstance.descriptionMargin}
          width={widthPadding}
          text={props.codeModuleInstanceDto.codeModule.description}
          fontSize={spacing.codeModuleInstance.descriptionFontSize}
        />
      </Group>
      <Group y={mainDetailsHeight}>
        <Line
          y={spacing.codeModuleInstance.paddingVertical}
          stroke={colors.border_light}
          strokeWidth={spacing.codeModuleInstance.delimiterHeight}
          strokeScaleEnabled={false}
          points={[0, 0, spacing.codeModuleInstance.width, 0]}
        />
      </Group>
      <Group y={mainDetailsHeight + delimiterHeight}>
        <Group>
          {buildPorts(props.codeModuleInstanceDto)}
        </Group>
        <Group
          ref={outputsGroupRef}
          x={spacing.codeModuleInstance.width - outputsWidth}
        >
          {buildPorts(props.codeModuleInstanceDto, true)}
        </Group>
      </Group>

    </Group>
  );
}

export default CodeModuleInstance;
