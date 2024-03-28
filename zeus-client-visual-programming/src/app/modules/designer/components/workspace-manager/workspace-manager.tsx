import React, {useState} from 'react';

import './workspace-manager.module.scss';
import {UpdateWorkspacePropertiesDto, ViewDto, ViewType, WorkspaceDesignerDto} from '../../../../../gen/api-client';
import {CircularProgress, IconButton, Typography} from '@material-ui/core';
import AddIcon from '@material-ui/icons/Add';
import ToolboxList from '../../../../components/toolbox-list/toolbox-list';
import ToolboxListItem from '../../../../components/toolbox-list-item/toolbox-list-item';
import DashboardIcon from '@material-ui/icons/Dashboard';
import {useQueryClient} from 'react-query';
import spacing from '../../../../../assets/styling/spacing.json';
import {DesignerWorkspaceService} from '../../services/designer-workspace.service';
import {useStore} from '../../../../store';
import {AppUtils} from '../../../../app.utils';
import {useSaveView, useSetRootView, useUpdateViewName} from '../../data/view-data.hooks';
import FocusModeIcon from '../../../../components/focus-mode-icon/focus-mode-icon';
import {ToolType} from '../../../../enums/tool-type.enum';
import HomeIcon from '@material-ui/icons/Home';
import HomeOutlinedIcon from '@material-ui/icons/HomeOutlined';

export interface WorkspaceManagerProps {
  workspace: WorkspaceDesignerDto;
}

const synchronizeWorkspaceProperties = AppUtils.debounce(DesignerWorkspaceService.updateProperties, 1000);

export function WorkspaceManager(props: WorkspaceManagerProps) {

  const activeTool = useStore(state => state.activeDesignerTool);
  const stageProperties = useStore(state => state.designerStageProperties);
  const setStageProperties = useStore(state => state.setDesignerStageProperties);
  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const setActiveViewUuid = useStore(state => state.setActiveDesignerViewUuid);
  const selectedComponentUuids = useStore(state => state.selectedComponentUuids);
  const setSelectedComponentUuids = useStore(state => state.setSelectedComponentUuids);
  const transformActiveView = useStore(state => state.transformActiveDesignerView);
  const setTransformActiveView = useStore(state => state.setTransformActiveDesignerView);
  const textEditorState = useStore(state => state.textEditorState);
  const resetTextEditorState = useStore(state => state.resetTextEditorState);

  const [saveViewIsLoading, setSaveViewIsLoading] = useState(false);
  const endSaveViewIsLoading = () => setTimeout(() => setSaveViewIsLoading(false), 1000);

  const queryClient = useQueryClient();

  const selectView = (view: ViewDto) => {
    // remove selection by re-selecting the currently active view
    if (activeViewUuid === view.uuid) {
      setActiveViewUuid(null);
      return;
    }

    // calculate the margin depending on the relation
    // between the view's and the stage's widths
    const margin = 100 * view.width / stageProperties.width;
    // compute a scale where the calculated margin is not violated
    const scaleTo = stageProperties.width / ((2 * margin) + view.width);

    const updateWorkspacePropertiesDto: UpdateWorkspacePropertiesDto = {
      positionX: -(view.positionX - margin) * scaleTo,
      positionY: -(view.positionY - margin) * scaleTo,
      scale: scaleTo
    };

    setStageProperties({
      ...stageProperties,
      x: updateWorkspacePropertiesDto.positionX,
      y: updateWorkspacePropertiesDto.positionY,
      scale: updateWorkspacePropertiesDto.scale
    });

    setActiveViewUuid(view.uuid);
    synchronizeWorkspaceProperties(props.workspace.uuid, updateWorkspacePropertiesDto);

  };

  const handleFocusButtonClick = () => setTransformActiveView(!transformActiveView);

  const saveView = useSaveView(
    queryClient, props.workspace.uuid, setSaveViewIsLoading, endSaveViewIsLoading, selectView
  );

  const updateViewName = useUpdateViewName(queryClient, props.workspace.uuid);
  const handleListItemSaveTitle = (viewUuid: string, title: string) => updateViewName.mutate({
    viewUuid, updateViewNameDto: { name: title }
  });

  const setRootView = useSetRootView(props.workspace.uuid);

  const buildToolboxListActions = () => (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', height: 26, minWidth: 26 }}>
      {!saveViewIsLoading
        ? <IconButton onClick={() => saveView.mutate({ type: ViewType.Page })} size={'small'}>
          <AddIcon fontSize={'small'} />
        </IconButton>
        : <CircularProgress size={20} thickness={5} />}
    </div>
  );

  const buildToolboxListItemActions = (view: ViewDto) => {
    if (activeTool !== ToolType.POINTER || view.uuid !== activeViewUuid) {
      return [];
    }

    return [
      ((view.isRoot)
        ? <IconButton key={0} size={'small'} disabled={true}>
          <HomeIcon fontSize={'small'}/>
      </IconButton>
        : <IconButton key={0} size={'small'} onClick={() => setRootView.mutate(view.uuid)}>
          <HomeOutlinedIcon fontSize={'small'}/>
        </IconButton>),
      <IconButton key={1} size={'small'} onClick={handleFocusButtonClick}>
        <FocusModeIcon focused={transformActiveView} fontSize={'small'}/>
      </IconButton>
    ];
  }

  return (
    <ToolboxList title={'Views'} actions={buildToolboxListActions()}>
      {
        props.workspace.views.length !== 0
          ? props.workspace.views.map(currentView => <ToolboxListItem
            key={currentView.uuid}
            title={currentView.name}
            titleEditable={activeTool === ToolType.POINTER && currentView.uuid === activeViewUuid}
            onSaveTitle={(title) => handleListItemSaveTitle(currentView.uuid, title)}
            icon={<DashboardIcon color={'secondary'} fontSize={'small'} />}
            onClick={() => selectView(currentView)}
            disabled={activeTool !== ToolType.POINTER}
            selected={activeViewUuid === currentView.uuid}
            actions={buildToolboxListItemActions(currentView)}
          />)
          : <div style={{ paddingLeft: spacing.toolbox.padding, paddingRight: spacing.toolbox.padding }}>
            <Typography variant={'body2'} color={'textSecondary'}>This workspace has no views</Typography>
          </div>
      }
    </ToolboxList>
  );
}

export default WorkspaceManager;
