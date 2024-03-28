import React, { useState } from 'react';

import './toolbar-form-selector.module.scss';
import CategoryIcon from '@material-ui/icons/Category';
import ChangeHistoryIcon from '@material-ui/icons/ChangeHistory';
import ToolbarMenuItem from '../../../../components/toolbar-menu-item/toolbar-menu-item';
import CropSquareIcon from '@material-ui/icons/CropSquare';
import RadioButtonUncheckedIcon from '@material-ui/icons/RadioButtonUnchecked';
import TitleIcon from '@material-ui/icons/Title';
import { SvgIcon } from '@material-ui/core';
import { useStore } from '../../../../store';
import { ToolType } from '../../../../enums/tool-type.enum';
import { DesignerUtils } from '../../designer.utils';

/* eslint-disable-next-line */
export interface ToolbarFormSelectorProps {
}

export function ToolbarFormSelector(props: ToolbarFormSelectorProps) {

  const [activeForm, setActiveForm] = useState<ToolType>(null);
  const activeTool = useStore(state => state.activeDesignerTool);
  const setActiveTool = useStore(state => state.setActiveDesignerTool);

  const getSvgIconType = (designerToolType: ToolType): (typeof SvgIcon) | undefined => {
    switch (designerToolType) {
      case ToolType.TRIANGLE_FORM_CREATOR:
        return ChangeHistoryIcon;
      case ToolType.RECTANGLE_FORM_CREATOR:
        return CropSquareIcon;
      case ToolType.CIRCLE_FORM_CREATOR:
        return RadioButtonUncheckedIcon;
      case ToolType.TEXT_FORM_CREATOR:
        return TitleIcon;
      default:
        return undefined;
    }
  };

  const handleMenuItemClick = (designerToolType: ToolType) => {
    setActiveForm(designerToolType);
    setActiveTool(designerToolType);
  };

  return (
    <ToolbarMenuItem
      icon={CategoryIcon}
      iconFontSize={25}
      indicatorIcon={getSvgIconType(activeForm)}
      onClick={activeForm !== null ? () => setActiveTool(activeForm) : undefined}
      active={DesignerUtils.isFormCreatorToolType(activeTool)}
    >
      <ToolbarMenuItem
        icon={CropSquareIcon}
        onClick={() => handleMenuItemClick(ToolType.RECTANGLE_FORM_CREATOR)}
        active={activeTool === ToolType.RECTANGLE_FORM_CREATOR}
      />
      <ToolbarMenuItem
        icon={RadioButtonUncheckedIcon}
        onClick={() => handleMenuItemClick(ToolType.CIRCLE_FORM_CREATOR)}
        active={activeTool === ToolType.CIRCLE_FORM_CREATOR}
      />
      <ToolbarMenuItem
        icon={TitleIcon}
        onClick={() => handleMenuItemClick(ToolType.TEXT_FORM_CREATOR)}
        active={activeTool === ToolType.TEXT_FORM_CREATOR}
      />
    </ToolbarMenuItem>
  );
}

export default ToolbarFormSelector;
