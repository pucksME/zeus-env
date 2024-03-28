import React from 'react';

import './designer.module.scss';
import Toolbar from '../../../../sections/toolbar/toolbar';
import Workspace from '../../../../sections/workspace/workspace';
import { useDesignerWorkspace } from '../../data/designer-workspace-data.hooks';
import Configurator from '../../../../sections/configurator/configurator';
import { useStore } from '../../../../store';
import { ToolType } from '../../../../enums/tool-type.enum';
import ConfiguratorDesignerView from '../../sections/configurator-designer-view/configurator-designer-view';
import ConfiguratorDesignerElements
  from '../../sections/configurator-designer-elements/configurator-designer-elements';
import Toolbox from '../../../../sections/toolbox/toolbox';
import ToolboxDesignerWorkspace from '../../components/toolbox-designer-workspace/toolbox-designer-workspace';
import ToolboxDesignerBlueprintComponents
  from '../../components/toolbox-designer-blueprint-components/toolbox-designer-blueprint-components';
import ToolbarDesignerOptions from '../../sections/toolbar-designer-options/toolbar-designer-options';
import WorkspaceDesigner from '../../sections/workspace-designer/workspace-designer';
import WorkspaceDesignerBlueprintComponent
  from '../../components/workspace-designer-blueprint-component/workspace-designer-blueprint-component';

export interface DesignerProps {
  workspaceUuid: string;
}

export function Designer(props: DesignerProps) {

  const { isLoading, isError, workspaceDto, error } = useDesignerWorkspace(props.workspaceUuid);
  const activeTool = useStore(state => state.activeDesignerTool);
  const transformActiveView = useStore(state => state.transformActiveDesignerView);
  const selectedComponentUuids = useStore(state => state.selectedComponentUuids);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (isError) {
    return <div>{error}</div>;
  }

  return (
    <div
      className={'height-100-percent'}
      style={{
        display: 'flex',
        alignItems: 'flex-start'
      }}>
      <Toolbar>
        <ToolbarDesignerOptions/>
      </Toolbar>
      <Toolbox
        defaultPageName={'workspace'}
        pages={[
          {
            name: 'workspace',
            content: <ToolboxDesignerWorkspace workspaceDto={workspaceDto}/>
          },
          {
            name: 'blueprints',
            content: <ToolboxDesignerBlueprintComponents workspaceDto={workspaceDto}/>
          }
        ]}
      />
      <Workspace>
        <WorkspaceDesigner workspace={workspaceDto}/>
      </Workspace>
      <Configurator
        visible={activeTool === ToolType.POINTER && (transformActiveView || selectedComponentUuids.length !== 0)}
      >
        <ConfiguratorDesignerView workspace={workspaceDto}/>
        <ConfiguratorDesignerElements workspace={workspaceDto}/>
      </Configurator>
      <WorkspaceDesignerBlueprintComponent workspaceDto={workspaceDto}/>
    </div>
  );
}

export default Designer;
