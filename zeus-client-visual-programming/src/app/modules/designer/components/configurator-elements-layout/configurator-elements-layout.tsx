import React from 'react';

import './configurator-elements-layout.module.scss';
import AlignHorizontalLeftIcon from '@material-ui/icons/AlignHorizontalLeft';
import AlignHorizontalCenterIcon from '@material-ui/icons/AlignHorizontalCenter';
import AlignHorizontalRightIcon from '@material-ui/icons/AlignHorizontalRight';
import AlignVerticalTopIcon from '@material-ui/icons/AlignVerticalTop';
import AlignVerticalCenterIcon from '@material-ui/icons/AlignVerticalCenter';
import AlignVerticalBottomIcon from '@material-ui/icons/AlignVerticalBottom';
import { Button, ButtonGroup } from '@material-ui/core';
import { Alignment } from '../../../../../gen/api-client';
import { useAlignElements } from '../../data/component-data.hooks';
import { useStore } from '../../../../store';
import { useAlignBlueprintElements } from '../../data/blueprint-component-data.hooks';

export interface ConfiguratorShapesLayoutProps {
  elementUuids: string[];
  workspaceUuid: string;
}

export function ConfiguratorElementsLayout(props: ConfiguratorShapesLayoutProps) {

  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const selectedElementsProperties = useStore(state => state.selectedElementsProperties);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);

  const alignElements = useAlignElements(props.workspaceUuid);
  const alignBlueprintElements = useAlignBlueprintElements(props.workspaceUuid);

  const handleClick = (alignment: Alignment) =>
    (!stageBlueprintComponentProperties.active ? alignElements : alignBlueprintElements).mutate({
    parentComponentUuid: (!stageBlueprintComponentProperties.active)
      ? focusedComponentUuid
      : (focusedComponentUuid === null)
        ? stageBlueprintComponentProperties.blueprintComponentUuid
        : focusedComponentUuid,
    elementsProperties: {
      height: selectedElementsProperties.height,
      width: selectedElementsProperties.width,
      x: selectedElementsProperties.x,
      y: selectedElementsProperties.y
    },
    elements: selectedElementsProperties.elements.map(element => ({
      elementUuid: element.elementUuid,
      elementProperties: {
        height: element.height,
        width: element.width,
        x: element.x,
        y: element.y
      }
    })),
    alignment
  });

  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
      <ButtonGroup size={'small'} variant={'text'}>
        <Button onClick={() => handleClick(Alignment.HorizontalLeft)}>
          <AlignHorizontalLeftIcon fontSize={'small'} />
        </Button>
        <Button onClick={() => handleClick(Alignment.HorizontalCenter)}>
          <AlignHorizontalCenterIcon fontSize={'small'} />
        </Button>
        <Button onClick={() => handleClick(Alignment.HorizontalRight)}>
          <AlignHorizontalRightIcon fontSize={'small'} />
        </Button>
      </ButtonGroup>

      <ButtonGroup size={'small'} variant={'text'}>
        <Button onClick={() => handleClick(Alignment.VerticalTop)}>
          <AlignVerticalTopIcon fontSize={'small'} />
        </Button>
        <Button onClick={() => handleClick(Alignment.VerticalCenter)}>
          <AlignVerticalCenterIcon fontSize={'small'} />
        </Button>
        <Button onClick={() => handleClick(Alignment.VerticalBottom)}>
          <AlignVerticalBottomIcon fontSize={'small'} />
        </Button>
      </ButtonGroup>
    </div>
  );
}

export default ConfiguratorElementsLayout;
