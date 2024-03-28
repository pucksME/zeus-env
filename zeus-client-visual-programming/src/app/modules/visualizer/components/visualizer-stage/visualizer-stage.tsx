import React, { useEffect, useRef } from 'react';

import './visualizer-stage.module.scss';
import { CodeModuleInstanceDto } from '../../../../../gen/api-client';
import { Group } from 'react-konva';
import { VisualizerStageUtils } from '../../visualizer-stage.utils';
import { VisualizerStageGroupName } from '../../enums/visualizer-stage-group-name.enum';
import Konva from 'konva';
import { useTranslateCodeModuleInstances } from '../../data/code-module-instance-data.hooks';
import { useQueryClient } from 'react-query';
import { VisualizerStageEventService } from '../../services/visualizer-stage-event.service';
import { useStore } from '../../../../store';
import { ToolType } from '../../../../enums/tool-type.enum';

export interface VisualizerStageProps {
  componentUuid: string;
  codeModuleInstanceDtos: CodeModuleInstanceDto[];
}

export function VisualizerStage(props: VisualizerStageProps) {
  const activeTool = useStore(state => state.activeVisualizerTool);
  const setCodeModuleInstancesProperties = useStore(state => state.setCodeModuleInstancesProperties);
  const queryClient = useQueryClient();
  const selectedCodeModuleInstancesRef = useRef<Konva.Group | null>(null);
  const translateCodeModuleInstances = useTranslateCodeModuleInstances(
    queryClient,
    props.componentUuid,
    selectedCodeModuleInstancesRef
  );

  useEffect(() => setCodeModuleInstancesProperties(props.codeModuleInstanceDtos.map(codeModuleInstanceDto => ({
    codeModuleInstanceUuid: codeModuleInstanceDto.uuid,
    codeModuleName: codeModuleInstanceDto.codeModule.name,
    height: 0,
    mainDetailsHeight: 0,
    outputsWidth: 0,
    inputs: codeModuleInstanceDto.codeModule.inputEndpoints.map(inputEndpoint => ({
      portName: inputEndpoint.name,
      portOffsetY: 0
    })),
    outputs: codeModuleInstanceDto.codeModule.outputEndpoints.map(outputEndpoint => ({
      portName: outputEndpoint.name,
      portOffsetY: 0
    }))
  }))), []);

  const {
    codeModuleInstances,
    selectedCodeModuleInstances
  } = VisualizerStageUtils.buildCodeModules(props.codeModuleInstanceDtos);

  const handleDragEnd = (event: Konva.KonvaEventObject<DragEvent>) =>
    VisualizerStageEventService.handleDragEnd(event, translateCodeModuleInstances.mutate);

  return (
    <Group>
      <Group name={VisualizerStageGroupName.CODE_MODULE_INSTANCES}>{codeModuleInstances}</Group>
      <Group
        ref={selectedCodeModuleInstancesRef}
        draggable={activeTool === ToolType.POINTER}
        onDragEnd={handleDragEnd}
        name={VisualizerStageGroupName.CODE_MODULE_INSTANCES_SELECTED}
      >
        {selectedCodeModuleInstances}
      </Group>
    </Group>
  );
}

export default VisualizerStage;
