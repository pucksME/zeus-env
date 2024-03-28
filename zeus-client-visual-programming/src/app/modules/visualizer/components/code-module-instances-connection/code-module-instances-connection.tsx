import React from 'react';

import './code-module-instances-connection.module.scss';
import { Line } from 'react-konva';
import { CodeModuleInstanceConnectionDto, CodeModuleInstanceDto } from '../../../../../gen/api-client';
import { CodeModuleInstanceUtils } from '../../code-module-instance.utils';
import colors from '../../../../../assets/styling/colors.json';
import { useStore } from '../../../../store';

export interface CodeModuleInstancesConnectionProps {
  codeModuleInstanceDtos: CodeModuleInstanceDto[];
  codeModuleInstanceConnectionDto: CodeModuleInstanceConnectionDto;
}

export function CodeModuleInstancesConnection(
  props: CodeModuleInstancesConnectionProps
) {
  const selectedCodeModuleInstanceUuids = useStore(state => state.selectedCodeModuleInstanceUuids);
  const visualizerStageProperties = useStore(state => state.visualizerStageProperties);
  const visualizerSelectionTransformerProperties = useStore(state => state.visualizerSelectionTransformerProperties);

  const buildCodeModulePortPoints: (
    codeModuleName: string,
    codeModulePortName: string,
    isOutput: boolean
  ) => [number, number] | null = (codeModuleName: string, codeModulePortName: string, isOutput: boolean) => {
    const codeModuleInstanceDto = props.codeModuleInstanceDtos.find(
      codeModuleInstanceDto => codeModuleInstanceDto.codeModule.name === codeModuleName
    );

    if (codeModuleInstanceDto === undefined) {
      return null;
    }

    const codeModuleEndpointDto = ((isOutput)
      ? codeModuleInstanceDto.codeModule.outputEndpoints
      : codeModuleInstanceDto.codeModule.inputEndpoints).find(endpointDto => endpointDto.name === codeModulePortName);

    if (codeModuleEndpointDto === undefined) {
      return null;
    }

    const portPosition = CodeModuleInstanceUtils.getPortPosition(
      codeModuleInstanceDto,
      {
        codeModuleInstanceUuid: codeModuleInstanceDto.uuid,
        codeModuleName: codeModuleInstanceDto.codeModule.name,
        portName: codeModuleEndpointDto.name
      },
      isOutput
    );

    const codeModuleInstance = props.codeModuleInstanceDtos.find(
      codeModuleInstance => codeModuleInstance.codeModule.name === codeModuleName
    );

    if (codeModuleInstance === undefined) {
      return [portPosition.x, portPosition.y];
    }

    const codeModuleInstanceIsSelected = selectedCodeModuleInstanceUuids.includes(codeModuleInstanceDto.uuid);

    if (!codeModuleInstanceIsSelected) {
      return [portPosition.x, portPosition.y];
    }

    return [
      portPosition.x - (visualizerSelectionTransformerProperties.dragOffset.x / visualizerStageProperties.scale),
      portPosition.y - (visualizerSelectionTransformerProperties.dragOffset.y / visualizerStageProperties.scale)
    ];
  };

  const inputCodeModulePortPoints = buildCodeModulePortPoints(
    props.codeModuleInstanceConnectionDto.inputCodeModuleInstanceName,
    props.codeModuleInstanceConnectionDto.inputCodeModuleInstancePortName,
    false
  );

  if (inputCodeModulePortPoints === null) {
    return null;
  }

  const outputCodeModulePortPoints = buildCodeModulePortPoints(
    props.codeModuleInstanceConnectionDto.outputCodeModuleInstanceName,
    props.codeModuleInstanceConnectionDto.outputCodeModuleInstancePortName,
    true
  );

  if (outputCodeModulePortPoints === null) {
    return null;
  }

  return (
    <Line
      points={[
        ...inputCodeModulePortPoints,
        ...CodeModuleInstanceUtils.calculateCurvePoints(inputCodeModulePortPoints, outputCodeModulePortPoints),
        ...outputCodeModulePortPoints
      ]}
      bezier={true}
      stroke={colors.secondary.main}
      strokeWidth={2}
      strokeScaleEnabled={true}
    />
  );
}

export default CodeModuleInstancesConnection;
