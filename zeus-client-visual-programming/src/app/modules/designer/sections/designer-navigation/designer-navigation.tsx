import React from 'react';

import './designer-navigation.module.scss';
import { Button } from '@material-ui/core';
import MapIcon from '@material-ui/icons/Map';
import CategoryIcon from '@material-ui/icons/Category';
import { useProject } from '../../../project/data/project.hooks';
import { useSaveBlueprintComponent } from '../../data/blueprint-component-data.hooks';
import { useStore } from '../../../../store';
import { AppUtils } from '../../../../app.utils';
import { useDesignerWorkspace } from '../../data/designer-workspace-data.hooks';
import { useSaveComponentWithShape } from '../../data/component-data.hooks';
import { ComponentDto, ElementType } from '../../../../../gen/api-client';
import { ComponentUtils } from '../../component.utils';

export interface DesignerNavigationProps {
  projectUuid: string;
}

export function DesignerNavigation(props: DesignerNavigationProps) {

  const { projectDto } = useProject(props.projectUuid);
  const { workspaceDto } = useDesignerWorkspace((!projectDto) ? null : projectDto.workspaceUuid);
  const workspaceUuid = (!workspaceDto) ? null : projectDto.workspaceUuid;

  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const selectedElementUuids = useStore(state => state.selectedComponentUuids);

  const saveBlueprintComponent = useSaveBlueprintComponent(workspaceUuid);
  const saveComponentWithShape = useSaveComponentWithShape(workspaceUuid);

  const activeView = (!workspaceDto) ? null : workspaceDto.views.find(view => view.uuid === activeViewUuid);
  const activeViewComponents = (!activeView) ? [] : activeView.components;

  const selectedComponent = (selectedElementUuids.length !== 1)
    ? null
    : AppUtils.findInTrees<ComponentDto>(
      activeViewComponents,
      selectedElementUuids[0]
    ).node;

  let selectionContainsBlueprintComponentInstance = false;
  if (selectedComponent) {
    ComponentUtils.traverseComponentTree(
      selectedComponent,
      component => {
        if (component.isBlueprintComponentInstance) {
          selectionContainsBlueprintComponentInstance = true;
        }
        return component;
      }
    )
  }

  const selectedShapeParent = (focusedComponentUuid === null ||
    (selectedComponent !== null && selectedComponent !== undefined))
    ? null
    : AppUtils.findInTrees(activeViewComponents, focusedComponentUuid).node;

  const handleSaveBlueprintComponentClick = () => saveBlueprintComponent.mutate(selectedElementUuids[0]);
  const handleSaveComponentWithShapeClick = () => saveComponentWithShape.mutate(selectedElementUuids[0]);

  return (
    <ul style={{
      display: 'flex',
      alignItems: 'center',
      marginLeft: 15
    }}>
      <li style={{ marginRight: 5 }}>
        <Button
          size={'small'}
          disabled={!selectedComponent || selectionContainsBlueprintComponentInstance}
          onClick={handleSaveBlueprintComponentClick}
          startIcon={<MapIcon/>}
        >
          Create Blueprint
        </Button>
      </li>
      <li>
        <Button
          size={'small'}
          disabled={selectedElementUuids.length !== 1 ||
            selectedComponent !== undefined ||
            !selectedShapeParent ||
            selectedShapeParent.elements.filter(element => element.type === ElementType.Shape).length < 2 ||
            selectedShapeParent.isBlueprintComponentInstance}
          onClick={handleSaveComponentWithShapeClick}
          startIcon={<CategoryIcon/>}
        >
          Convert to Component
        </Button>
      </li>
    </ul>
  );
}

export default DesignerNavigation;
