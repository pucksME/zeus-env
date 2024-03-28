import React from 'react';

import './toolbox-designer-workspace.module.scss';
import { useStore } from '../../../../store';
import spacing from '../../../../../assets/styling/spacing.json';
import { Button } from '@material-ui/core';
import ExitFocusModeIcon from '@material-ui/icons/ArrowBack';
import WorkspaceManager from '../workspace-manager/workspace-manager';
import ElementsManager from '../elements-manager/elements-manager';
import {
  BlueprintComponentDto,
  BlueprintElementDto,
  ComponentDto,
  ElementDto,
  WorkspaceDesignerDto
} from '../../../../../gen/api-client';
import { AppUtils } from '../../../../app.utils';
import { ComponentUtils } from '../../component.utils';
import { useBlueprintComponentsWorkspace } from '../../data/blueprint-component-data.hooks';

export interface ToolboxDesignerWorkspaceProps {
  workspaceDto: WorkspaceDesignerDto;
}

export function ToolboxDesignerWorkspace(props: ToolboxDesignerWorkspaceProps) {

  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const endFocusComponent = useStore(state => state.endFocusComponent);
  const transformActiveView = useStore(state => state.transformActiveDesignerView);
  const setTransformActiveView = useStore(state => state.setTransformActiveDesignerView);
  const designerStageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);
  const {isLoading, isError, blueprintComponentDtos} = useBlueprintComponentsWorkspace(props.workspaceDto.uuid);

  if (isLoading || isError) {
    return null;
  }

  let activeView = (activeViewUuid === null)
    ? null
    : props.workspaceDto.views.find(view => view.uuid === activeViewUuid);

  if (activeView === undefined) {
    activeView = null;
  }

  let focusedComponent: ComponentDto | BlueprintComponentDto | null | undefined = null;
  let elements: (ElementDto | BlueprintElementDto)[] | null = null;
  let blueprintComponent: BlueprintComponentDto | undefined = undefined;

  if (!designerStageBlueprintComponentProperties.active && activeView !== null) {
    focusedComponent = (focusedComponentUuid === null)
      ? null
      : AppUtils.findInTrees<ComponentDto>(activeView.components, focusedComponentUuid).node;

    if (focusedComponent === undefined) {
      focusedComponent = null;
    }

    elements = (focusedComponent === null)
      ? ComponentUtils.mapComponentsToElements<ComponentDto>(activeView.components)
      : focusedComponent.elements;
  }

  if (designerStageBlueprintComponentProperties.active) {
    blueprintComponent = blueprintComponentDtos.find(blueprintComponent =>
      blueprintComponent.uuid === designerStageBlueprintComponentProperties.blueprintComponentUuid);

    focusedComponent = (focusedComponentUuid === null || blueprintComponent === undefined)
      ? null
      : AppUtils.findInTree<BlueprintComponentDto>(blueprintComponent, focusedComponentUuid).node;

    if (focusedComponent === undefined) {
      focusedComponent = null;
    }

    elements = (focusedComponent === null) ? blueprintComponent.elements : focusedComponent.elements;
  }

  if (elements !== null) {
    elements = ComponentUtils.sortElements<ElementDto | BlueprintElementDto>(elements);
  }

  const handleExitFocusMode = () => {
    if (focusedComponent !== null) {
      endFocusComponent();
    }

    if (transformActiveView) {
      setTransformActiveView(false);
    }
  }

  const getExitFocusModeButton = () => {
    return <div style={{paddingTop: spacing.toolbox.list.header.paddingTop, textAlign: 'center'}}>
      <Button
        color={'secondary'}
        variant={'contained'}
        startIcon={<ExitFocusModeIcon/>}
        onClick={handleExitFocusMode}
      >
        Exit Focus Mode
      </Button>
    </div>
  }

  const buildDefaultTitle = () => {
    if (blueprintComponent !== undefined) {
      return blueprintComponent.name;
    }

    if (activeView !== null) {
      return activeView.name;
    }

    return '';
  }

  return (
    <div>
      {(transformActiveView || focusedComponent !== null) ? getExitFocusModeButton() : null}
      {(!designerStageBlueprintComponentProperties.active) ? <WorkspaceManager workspace={props.workspaceDto} /> : null}
      <ElementsManager
        workspaceUuid={props.workspaceDto.uuid}
        components={(!designerStageBlueprintComponentProperties.active)
          ? (activeView === null) ? [] : activeView.components
          : (blueprintComponent === undefined) ? [] : ComponentUtils.getDirectChildComponents(blueprintComponent)}
        focusedComponent={focusedComponent}
        elements={elements}
        defaultTitle={buildDefaultTitle()}
      />
    </div>
  );
}

export default ToolboxDesignerWorkspace;
