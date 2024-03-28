import React from 'react';

import './toolbox-designer-blueprint-components.module.scss';
import {
  useBlueprintComponentsWorkspace,
  useInstantiateBlueprintComponent
} from '../../data/blueprint-component-data.hooks';
import BlueprintComponentCard
  from '../blueprint-component-card/blueprint-component-card';
import spacing from '../../../../../assets/styling/spacing.json';
import DragAndDropPreview
  from '../../../../components/drag-and-drop-preview/drag-and-drop-preview';
import { BlueprintComponentDto, ComponentDto, WorkspaceDesignerDto } from '../../../../../gen/api-client';
import { useStore } from '../../../../store';
import Konva from 'konva';
import { DesignerStageUtils } from '../../designer-stage.utils';
import { AppUtils } from '../../../../app.utils';
import { Typography } from '@material-ui/core';

export interface ToolboxDesignerBlueprintComponentsProps {
  workspaceDto: WorkspaceDesignerDto;
}

const cardWidth = (spacing.toolbox.width - (spacing.toolbox.padding * 2) - spacing.toolbox.cards.margin) / 2;

export function ToolboxDesignerBlueprintComponents(
  props: ToolboxDesignerBlueprintComponentsProps
) {

  const stageProperties = useStore(state => state.designerStageProperties);
  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const selectedBlueprintComponent = useStore(state => state.selectedBlueprintComponent);
  const instantiateBlueprintComponent = useInstantiateBlueprintComponent(props.workspaceDto.uuid);
  const resetSelectedBlueprintComponent = useStore(state => state.resetSelectedBlueprintComponent);
  const {isLoading, isError, blueprintComponentDtos, error} = useBlueprintComponentsWorkspace(props.workspaceDto.uuid);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (isError) {
    return <div>{error}</div>;
  }

  const view = (activeViewUuid !== null)
    ? props.workspaceDto.views.find(view => view.uuid === activeViewUuid)
    : undefined;

  const handleInstantiateBlueprintComponent = (event: Konva.KonvaEventObject<MouseEvent>) => {
    resetSelectedBlueprintComponent();

    if (view === undefined) {
      return;
    }

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
    positionX -= view.positionX;

    positionY -= stageProperties.y;
    positionY /= stageProperties.scale;
    positionY -= view.positionY;

    if (focusedComponentUuid === null) {
      instantiateBlueprintComponent.mutate({
        viewUuid: view.uuid,
        blueprintComponentUuid: selectedBlueprintComponent.uuid,
        positionX,
        positionY
      });
      return;
    }

    const { node, pathCoordinates } = AppUtils.findInTrees<ComponentDto>(view.components, focusedComponentUuid);

    if (node === undefined || node.isBlueprintComponentInstance) {
      return;
    }

    instantiateBlueprintComponent.mutate({
      viewUuid: view.uuid,
      parentComponentUuid: node.uuid,
      blueprintComponentUuid: selectedBlueprintComponent.uuid,
      positionX: positionX - (pathCoordinates.x + node.positionX),
      positionY: positionY - (pathCoordinates.y + node.positionY)
    });
  }

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'space-between',
      flexWrap: 'wrap',
      padding: spacing.toolbox.padding
    }}>
      {(blueprintComponentDtos.length !== 0)
        ? blueprintComponentDtos.map((blueprintComponentDto, index) => <BlueprintComponentCard
          key={blueprintComponentDto.uuid}
          blueprintComponentDto={blueprintComponentDto}
          length={cardWidth}
          style={{
            marginBottom: (index < blueprintComponentDtos.length - 2) ? spacing.toolbox.cards.margin : 0
          }}
        />)
        : <div style={{ paddingLeft: spacing.toolbox.padding, paddingRight: spacing.toolbox.padding }}>
          <Typography variant={'body2'} color={'textSecondary'}>There are no blueprint components</Typography>
      </div>}
      <DragAndDropPreview
        properties={stageProperties}
        active={selectedBlueprintComponent !== null}
        onDrop={handleInstantiateBlueprintComponent}
      >
        {(selectedBlueprintComponent === null)
          ? null
          : DesignerStageUtils.buildComponentTree<BlueprintComponentDto>(
            selectedBlueprintComponent,
            stageProperties.scale,
            true
          )}
      </DragAndDropPreview>
    </div>
  );
}

export default ToolboxDesignerBlueprintComponents;
