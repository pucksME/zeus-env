import React from 'react';
import './toolbar.module.scss';
import NearMeIcon from '@material-ui/icons/NearMe';
import PanToolIcon from '@material-ui/icons/PanTool';
import colors from '../../../assets/styling/colors.json';
import spacing from '../../../assets/styling/spacing.json';
import ToolbarMenuItem from '../../components/toolbar-menu-item/toolbar-menu-item';
import ToolbarFormSelector from '../../modules/designer/components/toolbar-form-selector/toolbar-form-selector';
import { useStore } from '../../store';
import { ToolType } from '../../enums/tool-type.enum';

export interface DesignerToolbarProps {
  children: React.ReactNode;
}

export function Toolbar(props: DesignerToolbarProps) {

  return (
    <div
      className={'height-100-percent'}
      style={{
        backgroundColor: colors.background_dark,
        boxSizing: 'border-box',
        flexShrink: 0,
        // paddingBottom: spacing.toolbar.paddingVertical,
        // paddingTop: spacing.toolbar.paddingVertical,
        width: spacing.toolbar.width
      }}
    >
      {props.children}
    </div>
  );

}

export default Toolbar;
