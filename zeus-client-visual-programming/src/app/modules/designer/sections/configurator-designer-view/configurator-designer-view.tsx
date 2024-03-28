import React from 'react';

import './configurator-designer-view.module.scss';
import { useStore } from '../../../../store';
import { ToolType } from '../../../../enums/tool-type.enum';
import ConfiguratorPreviewView from '../../components/configurator-preview-view/configurator-preview-view';
import ConfiguratorSectionHeader from '../../components/configurator-section-header/configurator-section-header';
import TuneIcon from '@material-ui/icons/Tune';
import ConfiguratorRow from '../../../../components/configurator-row/configurator-row';
import ConfiguratorColumn from '../../../../components/configurator-column/configurator-column';
import ConfiguratorViewDimensions from '../../components/configurator-view-dimensions/configurator-view-dimensions';
import ConfiguratorViewPosition from '../../components/configurator-view-position/configurator-view-position';
import ConfiguratorActions from '../../components/configurator-actions/configurator-actions';
import { useQueryClient } from 'react-query';
import ConfiguratorActionsViewDelete
  from '../../components/configurator-actions-view-delete/configurator-actions-view-delete';
import { WorkspaceDesignerDto } from '../../../../../gen/api-client';

export interface ConfiguratorDesignerViewProps {
  workspace: WorkspaceDesignerDto;
}

export function ConfiguratorDesignerView(props: ConfiguratorDesignerViewProps) {

  const activeTool = useStore(state => state.activeDesignerTool);
  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const transformActiveView = useStore(state => state.transformActiveDesignerView);
  const selectedComponentUuids = useStore(state => state.selectedComponentUuids);

  const view = activeViewUuid !== null
    ? props.workspace.views.find(view => view.uuid === activeViewUuid)
    : undefined;

  if (activeTool !== ToolType.POINTER ||
    !transformActiveView ||
    view === undefined ||
    selectedComponentUuids.length !== 0) {
    return null;
  }

  return (
    <div>
      <ConfiguratorPreviewView view={view}/>

      <ConfiguratorActions>
        <ConfiguratorActionsViewDelete viewUuid={view.uuid} workspaceUuid={props.workspace.uuid}/>
      </ConfiguratorActions>

      <ConfiguratorRow header={<ConfiguratorSectionHeader icon={TuneIcon} title={'General'}/>}>
        <ConfiguratorColumn>
          <ConfiguratorViewPosition workspaceUuid={props.workspace.uuid} view={view}/>
        </ConfiguratorColumn>
        <ConfiguratorColumn>
          <ConfiguratorViewDimensions workspaceUuid={props.workspace.uuid} view={view}/>
        </ConfiguratorColumn>
      </ConfiguratorRow>
    </div>
  );
}

export default ConfiguratorDesignerView;
