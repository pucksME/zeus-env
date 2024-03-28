import React from 'react';

import './elements-manager.module.scss';
import { IconButton, Typography } from '@material-ui/core';
import ComponentIcon from '@material-ui/icons/Category';
import FocusParentComponentIcon from '@material-ui/icons/ArrowUpward';
import ToolboxList from '../../../../components/toolbox-list/toolbox-list';
import {
  BlueprintComponentDto,
  BlueprintElementDto,
  ComponentDto,
  ElementDto,
  ElementType,
  ShapeDto
} from '../../../../../gen/api-client';
import ToolboxListItem from '../../../../components/toolbox-list-item/toolbox-list-item';
import spacing from '../../../../../assets/styling/spacing.json';
import { useStore } from '../../../../store';
import { ToolType } from '../../../../enums/tool-type.enum';
import { Draggable, Droppable, DropResult, ResponderProvided } from 'react-beautiful-dnd';
import { useUpdateComponentName, useUpdateElementSorting } from '../../data/component-data.hooks';
import { useQueryClient } from 'react-query';
import { DesignerStageUtils } from '../../designer-stage.utils';
import FocusModeIcon from '../../../../components/focus-mode-icon/focus-mode-icon';
import { Element } from '../../interfaces/element.interface';
import { useUpdateShapeName } from '../../data/shape-data.hooks';
import { AppUtils } from '../../../../app.utils';
import {
  useUpdateBlueprintComponentName,
  useUpdateBlueprintElementSorting
} from '../../data/blueprint-component-data.hooks';
import BlueprintComponentIcon from '@material-ui/icons/Map';

export interface ElementsManagerProps {
  workspaceUuid: string;
  components: (ComponentDto | BlueprintComponentDto)[];
  focusedComponent: ComponentDto | BlueprintComponentDto | null;
  elements: (ElementDto | BlueprintElementDto)[] | null;
  defaultTitle: string;
}

export function ElementsManager(props: ElementsManagerProps) {
  const activeTool = useStore(state => state.activeDesignerTool);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);
  const selectedComponentUuids = useStore(state => state.selectedComponentUuids);
  const setSelectedComponentUuids = useStore(state => state.setSelectedComponentUuids);
  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const focusComponent = useStore(state => state.focusComponent);
  const endFocusComponent = useStore(state => state.endFocusComponent);
  const queryClient = useQueryClient();

  const updateElementSorting = useUpdateElementSorting(props.workspaceUuid);
  const updateBlueprintElementSorting = useUpdateBlueprintElementSorting(props.workspaceUuid);
  const updateComponentName = useUpdateComponentName(queryClient, props.workspaceUuid, activeViewUuid);
  const updateShapeName = useUpdateShapeName(queryClient, props.workspaceUuid, activeViewUuid);
  const updateBlueprintComponentName = useUpdateBlueprintComponentName(props.workspaceUuid);

  if (props.elements === null) {
    return null;
  }

  const focusedComponentParent = (focusedComponentUuid === null)
    ? null
    : AppUtils.findParentInTrees(props.components, focusedComponentUuid);

  const buildToolboxListTitle = () => (props.focusedComponent === null) ? props.defaultTitle : props.focusedComponent.name;

  const buildToolboxListActions = () => (props.focusedComponent === null)
    ? null
    : <IconButton
      size={'small'}
      onClick={() => (focusedComponentParent === undefined)
        ? endFocusComponent()
        : focusComponent(focusedComponentParent.uuid)}
    >
      <FocusParentComponentIcon fontSize={'small'}/>
  </IconButton>;

  const handleDragEnd = (result: DropResult, provider: ResponderProvided) => {

    if (result.destination === null) {
      return;
    }

    (!stageBlueprintComponentProperties.active ? updateElementSorting : updateBlueprintElementSorting).mutate({
      oldSorting: result.source.index,
      updateElementSortingDto: {
        parentComponentUuid: (!stageBlueprintComponentProperties.active)
          ? focusedComponentUuid
          : (focusedComponentUuid === null)
            ? stageBlueprintComponentProperties.blueprintComponentUuid
            : focusedComponentUuid,
        elementUuid: result.draggableId,
        sorting: result.destination.index
      }
    });
  };

  const handleListItemSaveTitle = (element: ElementDto | BlueprintElementDto, title: string) =>
    (element.type === ElementType.Component)
      ? (!stageBlueprintComponentProperties.active)
        ? updateComponentName.mutate({
          componentUuid: element.element.uuid,
          updateComponentNameDto: { name: title }
        })
        : updateBlueprintComponentName.mutate({
          blueprintComponentUuid: element.element.uuid,
          updateBlueprintComponentNameDto: { name: title }
        })
      : updateShapeName.mutate({
        shapeUuid: element.element.uuid,
        updateShapeNameDto: { name: title }
      });

  const handleToolboxListItemClick = (element: ElementDto | BlueprintElementDto) => {

    if (selectedComponentUuids.length === 1 && selectedComponentUuids.includes(element.element.uuid)) {
      setSelectedComponentUuids([]);
      return;
    }

    setSelectedComponentUuids([element.element.uuid]);

  };

  const handleFocusButtonClick = (componentUuid: string) => {
    if (focusedComponentUuid === componentUuid) {
      endFocusComponent();
      return;
    }

    focusComponent(componentUuid);
  };

  const buildToolboxListItemIcon = (element: ElementDto | BlueprintElementDto): React.ReactElement => {
    if (element.type === ElementType.Shape) {
      return DesignerStageUtils.buildShapeIcon(element.element as ShapeDto);
    }

    const component = element.element as (ComponentDto | BlueprintComponentDto);

    if (component['isBlueprintComponentInstance']) {
      return <BlueprintComponentIcon color={'secondary'} fontSize={'small'}/>;
    }

    return <ComponentIcon color={'secondary'} fontSize={'small'}/>;
  };

  return (
    <ToolboxList
      title={buildToolboxListTitle()}
      actions={buildToolboxListActions()}
      onDragEnd={handleDragEnd}
    >
      <Droppable droppableId={'elements'}>
        {
          (provided, snapshot) =>
            <div ref={provided.innerRef} {...provided.droppableProps}>
              {props.elements.length !== 0
                ? props.elements.map((currentElement, index) => {
                  const element = currentElement.element;
                  return <Draggable
                    key={element.uuid}
                    draggableId={element.uuid}
                    index={index}
                    isDragDisabled={props.focusedComponent !== null &&
                      props.focusedComponent['isBlueprintComponentInstance']}>
                    {(provided, snapshot) => <div
                      ref={provided.innerRef} {...provided.draggableProps} {...provided.dragHandleProps}>
                      <ToolboxListItem
                        title={element.name}
                        titleEditable={activeTool === ToolType.POINTER &&
                          selectedComponentUuids.includes(element.uuid) &&
                          (props.focusedComponent === null || !props.focusedComponent['isBlueprintComponentInstance'])}
                        onSaveTitle={(title) => handleListItemSaveTitle(currentElement, title)}
                        icon={buildToolboxListItemIcon(currentElement)}
                        onClick={() => handleToolboxListItemClick(currentElement)}
                        disabled={activeTool !== ToolType.POINTER}
                        selected={selectedComponentUuids.includes(element.uuid)}
                        actions={(activeTool !== ToolType.POINTER ||
                          currentElement.type !== ElementType.Component ||
                          !selectedComponentUuids.includes(element.uuid))
                          ? null
                          : <div style={{ display: 'inline-block' }}>
                            <IconButton size={'small'} onClick={() => handleFocusButtonClick(element.uuid)}>
                              <FocusModeIcon focused={element.uuid === focusedComponentUuid} fontSize={'small'} />
                            </IconButton>
                          </div>}
                      />
                    </div>}
                  </Draggable>;
                })
                : <div style={{ paddingLeft: spacing.toolbox.padding, paddingRight: spacing.toolbox.padding }}>
                  <Typography variant={'body2'} color={'textSecondary'}>The selected view has no elements</Typography>
                </div>}
              {provided.placeholder}
            </div>
        }
      </Droppable>
    </ToolboxList>
  );
}

export default ElementsManager;
