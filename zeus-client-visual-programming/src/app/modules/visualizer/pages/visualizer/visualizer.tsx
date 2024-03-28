import React, { useState } from 'react';

import './visualizer.module.scss';
import Toolbar from '../../../../sections/toolbar/toolbar';
import Toolbox from '../../../../sections/toolbox/toolbox';
import Workspace from '../../../../sections/workspace/workspace';
import WorkspaceVisualizer from '../../sections/workspace-visualizer/workspace-visualizer';
import Configurator from '../../../../sections/configurator/configurator';
import { useVisualizerWorkspace } from '../../data/visualizer-workspace-data.hooks';
import ToolboxVisualizerProject from '../../components/toolbox-visualizer-project/toolbox-visualizer-project';
import ToolboxVisualizerSystem from '../../components/toolbox-visualizer-system/toolbox-visualizer-system';
import { CodeModuleDto } from '../../../../../gen/api-client';
import { useStore } from '../../../../store';
import Konva from 'konva';
import spacing from '../../../../../assets/styling/spacing.json';
import { QueryKeysCodeModules, useInstantiateCodeModule } from '../../data/code-module-instance-data.hooks';
import { useQueryClient } from 'react-query';
import DragAndDropPreview from '../../../../components/drag-and-drop-preview/drag-and-drop-preview';
import CodeModuleInstance from '../../components/code-module-instance/code-module-instance';
import ToolbarVisualizerOptions from '../../sections/toolbar-visualizer-options/toolbar-visualizer-options';
import ThunderCodeEditor from '../../components/thunder-code-editor/thunder-code-editor';
import { ToolType } from '../../../../enums/tool-type.enum';
import ConfiguratorVisualizerCodeModuleInstances
  from '../../sections/configurator-visualizer-code-module-instances/configurator-visualizer-code-module-instances';

export interface VisualizerProps {
  projectUuid: string;
  componentUuid: string;
}

export function Visualizer(props: VisualizerProps) {

  const {isLoading, isError, workspaceVisualizerDto, error} = useVisualizerWorkspace(props.componentUuid);
  const activeTool = useStore(state => state.activeVisualizerTool);
  const selectedCodeModuleInstanceUuids = useStore(state => state.selectedCodeModuleInstanceUuids);
  const stageProperties = useStore(state => state.visualizerStageProperties);
  const selectedCodeModule = useStore(state => state.selectedCodeModule);
  const setSelectedCodeModule = useStore(state => state.setSelectedCodeModule);
  const resetSelectedCodeModule = useStore(state => state.resetSelectedCodeModule);
  const thunderCodeEditorsProperties = useStore(state => state.thunderCodeEditorsProperties);
  const queryClient = useQueryClient();
  const instantiateCodeModule = useInstantiateCodeModule(queryClient, props.componentUuid);

  const [selectedQueryKeyCodeModules, setSelectedQueryKeyCodeModules] =
    useState<QueryKeysCodeModules | null>(null);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (isError) {
    return <div>{error['message']}</div>;
  }

  const handleSelectCodeModule = (codeModuleDto: CodeModuleDto, queryKeyCodeModules: QueryKeysCodeModules) => {
    setSelectedCodeModule(codeModuleDto);
    setSelectedQueryKeyCodeModules(queryKeyCodeModules);
  }

  const handleInstantiateCodeModule = (event: Konva.KonvaEventObject<MouseEvent>) => {
    const codeModule = selectedCodeModule;
    const queryKey = selectedQueryKeyCodeModules;
    resetSelectedCodeModule();
    setSelectedQueryKeyCodeModules(null);

    let positionX = event.evt.x - (spacing.toolbar.width + spacing.toolbox.width);

    if (positionX < 0) {
      return;
    }

    let positionY = event.evt.y - spacing.navigation.height;

    if (positionY < 0) {
      return;
    }

    positionX -= stageProperties.x;
    positionX /= stageProperties.scale;

    positionY -= stageProperties.y;
    positionY /= stageProperties.scale;

    instantiateCodeModule.mutate({
      queryKeyCodeModules: queryKey,
      createCodeModuleInstanceDto: {
        codeModuleUuid: codeModule.uuid,
        positionX,
        positionY
      }
    })
  }

  return (
    <div
      className={'height-100-percent'}
      style={{
        display: 'flex',
        alignItems: 'flex-start'
      }}>
      <Toolbar>
        <ToolbarVisualizerOptions/>
      </Toolbar>
      <Toolbox
        pages={[
          {
            name: 'project',
            content: <ToolboxVisualizerProject
              projectUuid={props.projectUuid}
              onSelectCodeModule={handleSelectCodeModule}
            />
          },
          {
            name: 'system',
            content: <ToolboxVisualizerSystem
              onSelectCodeModule={handleSelectCodeModule}
            />
          }
      ]}
        defaultPageName={'project'}
      />
      {thunderCodeEditorsProperties.map(thunderCodeEditorProperties =>
        <ThunderCodeEditor
          key={thunderCodeEditorProperties.codeModuleUuid}
          projectUuid={props.projectUuid}
          componentUuid={props.componentUuid}
          thunderCodeEditorProperties={thunderCodeEditorProperties}
        />
      )}
      <Workspace>
        <WorkspaceVisualizer
          componentUuid={props.componentUuid}
          workspaceVisualizerDto={workspaceVisualizerDto}
        />
      </Workspace>
      <Configurator visible={activeTool === ToolType.POINTER && selectedCodeModuleInstanceUuids.length !== 0}>
        <ConfiguratorVisualizerCodeModuleInstances
          componentUuid={props.componentUuid}
          workspace={workspaceVisualizerDto}
        />
      </Configurator>
      <DragAndDropPreview
        properties={stageProperties}
        active={selectedCodeModule !== null}
        onDrop={handleInstantiateCodeModule}
      >
        {
          (selectedCodeModule === null)
            ? null
            : <CodeModuleInstance codeModuleInstanceDto={{
              uuid: selectedCodeModule.uuid,
              flowDescription: '',
              positionX: 0,
              positionY: 0,
              codeModule: {...selectedCodeModule}
            }}/>
        }
      </DragAndDropPreview>
    </div>
  );
}

export default Visualizer;
