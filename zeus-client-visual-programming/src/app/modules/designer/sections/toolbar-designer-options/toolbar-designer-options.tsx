import React from 'react';

import './toolbar-designer-options.module.scss';
import { useStore } from '../../../../store';
import ToolbarMenuItem from '../../../../components/toolbar-menu-item/toolbar-menu-item';
import NearMeIcon from '@material-ui/icons/NearMe';
import { ToolType } from '../../../../enums/tool-type.enum';
import PanToolIcon from '@material-ui/icons/PanTool';
import ToolbarFormSelector from '../../components/toolbar-form-selector/toolbar-form-selector';

/* eslint-disable-next-line */
export interface ToolbarDesignerOptionsProps {}

export function ToolbarDesignerOptions(props: ToolbarDesignerOptionsProps) {

  const activeTool = useStore(state => state.activeDesignerTool);
  const setActiveTool = useStore(state => state.setActiveDesignerTool);

  return (
    <div className={'height-100-percent'}>
      <ToolbarMenuItem
        icon={NearMeIcon}
        onClick={() => setActiveTool(ToolType.POINTER)}
        active={activeTool === ToolType.POINTER}
      />
      <ToolbarMenuItem
        icon={PanToolIcon}
        offsetX={-5}
        onClick={() => setActiveTool(ToolType.NAVIGATOR)}
        active={activeTool === ToolType.NAVIGATOR}
      />
      <ToolbarFormSelector />
    </div>
  );
}

export default ToolbarDesignerOptions;
