import React from 'react';

import './toolbar-visualizer-options.module.scss';
import ToolbarMenuItem from '../../../../components/toolbar-menu-item/toolbar-menu-item';
import NearMeIcon from '@material-ui/icons/NearMe';
import PanToolIcon from '@material-ui/icons/PanTool';
import { useStore } from '../../../../store';
import { ToolType } from '../../../../enums/tool-type.enum';
import ArrowBackIcon from '@material-ui/icons/ArrowBack';
import { Link, useRouteMatch } from 'react-router-dom';

/* eslint-disable-next-line */
export interface ToolbarVisualizerOptionsProps {}

export function ToolbarVisualizerOptions(props: ToolbarVisualizerOptionsProps) {
  const activeTool = useStore(state => state.activeVisualizerTool);
  const setActiveTool = useStore(state => state.setActiveVisualizerTool);
  const codeModuleInstancesConnectionEditorState = useStore(state => state.codeModuleInstancesConnectionEditorState);
  const setCodeModuleInstancesConnectionEditorState = useStore(state => state.setCodeModuleInstancesConnectionEditorState);
  const visualizerMatch = useRouteMatch('/project/:projectUuid/component/:componentUuid');

  const handleClick = (toolType: ToolType) => {
    if (toolType !== ToolType.POINTER &&
      (codeModuleInstancesConnectionEditorState.input !== null ||
        codeModuleInstancesConnectionEditorState.output !== null)) {
      setCodeModuleInstancesConnectionEditorState({input: null, output: null});
    }

    setActiveTool(toolType);
  };

  return (
    <div className={'height-100-percent'}>
      <ToolbarMenuItem
        icon={NearMeIcon}
        onClick={() => handleClick(ToolType.POINTER)}
        active={activeTool === ToolType.POINTER}
      />
      <ToolbarMenuItem
        icon={PanToolIcon}
        onClick={() => handleClick(ToolType.NAVIGATOR)}
        active={activeTool === ToolType.NAVIGATOR}
      />
      <Link to={`/project/${visualizerMatch.params['projectUuid']}`} style={{position: 'absolute', bottom: 0}}>
        <ToolbarMenuItem icon={ArrowBackIcon}/>
      </Link>
    </div>
  );
}

export default ToolbarVisualizerOptions;
