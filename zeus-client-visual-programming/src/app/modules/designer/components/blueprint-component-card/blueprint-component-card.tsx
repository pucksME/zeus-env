import React, { CSSProperties, useEffect, useRef, useState } from 'react';

import './blueprint-component-card.module.scss';
import { Box, IconButton, Typography } from '@material-ui/core';
import { BlueprintComponentDto } from '../../../../../gen/api-client';
import spacing from '../../../../../assets/styling/spacing.json';
import { Group, Layer, Stage } from 'react-konva';
import { DesignerStageUtils } from '../../designer-stage.utils';
import { useStore } from '../../../../store';
import Konva from 'konva';
import { AppUtils } from '../../../../app.utils';
import OpenInNewIcon from '@material-ui/icons/OpenInNew';

export interface BlueprintComponentCardProps {
  blueprintComponentDto: BlueprintComponentDto;
  length: number;
  style?: CSSProperties;
}

export function BlueprintComponentCard(props: BlueprintComponentCardProps) {

  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const setSelectedBlueprintComponent = useStore(state => state.setSelectedBlueprintComponent);
  const designerStageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);
  const setDesignerStageBlueprintComponentProperties = useStore(state => state.setDesignerStageBlueprintComponentProperties);
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const endFocusComponent = useStore(state => state.endFocusComponent);
  const selectedElementUuids = useStore(state => state.selectedComponentUuids);
  const setSelectedElementUuids = useStore(state => state.setSelectedComponentUuids);
  const textEditorState = useStore(state => state.textEditorState);
  const resetTextEditorState = useStore(state => state.resetTextEditorState);

  const blueprintContainerRef = useRef<Konva.Group | null>(null);
  const [blueprintContainerProperties, setBlueprintContainerProperties] = useState<{
    x: number,
    y: number,
    scale: number,
    active: boolean
  }>({
    x: 0,
    y: 0,
    scale: 1,
    active: false
  });

  useEffect(() => {
    if (blueprintContainerRef.current === null) {
      return;
    }

    const blueprintContainerChildren = blueprintContainerRef.current.getChildren();

    if (blueprintContainerChildren.length !== 1) {
      return;
    }

    setBlueprintContainerProperties({
      ...AppUtils.calculateContainerPropertiesToFitElements(
        {
          height: props.length,
          width: props.length
        },
        blueprintContainerChildren[0].getClientRect(),
        {
          horizontal: spacing.toolbox.cards.blueprints.previewPadding,
          vertical: spacing.toolbox.cards.blueprints.previewPadding
        }
      ),
      active: true
    });
  }, [blueprintContainerRef]);

  const buildName = () => {
    if (props.blueprintComponentDto.name.length <= 10) {
      return props.blueprintComponentDto.name;
    }
    return props.blueprintComponentDto.name.substr(0, 10) + '...';
  };

  const handleMouseDown = () => {
    if (activeViewUuid === null) {
      return;
    }
    setSelectedBlueprintComponent(props.blueprintComponentDto);
  }

  const handleOpenButtonClick = (event: React.MouseEvent) => {
    if (textEditorState.active) {
      resetTextEditorState();
    }

    if (focusedComponentUuid !== null) {
      endFocusComponent();
    }

    if (selectedElementUuids.length !== 0) {
      setSelectedElementUuids([]);
    }

    setDesignerStageBlueprintComponentProperties({
      ...designerStageBlueprintComponentProperties,
      blueprintComponentUuid: props.blueprintComponentDto.uuid,
      initialContainerPosition: {
        x: event.clientX,
        y: event.clientY
      }
    });
  };

  return (
    <Box
      style={{
        backgroundColor: '#ffffff',
        borderRadius: 15,
        height: props.length,
        width: props.length,
        position: 'relative',
        ...props.style
    }}
    boxShadow={1}
    >
      <div style={{
        position: 'absolute',
        top: 4,
        right: 4,
        zIndex: 1
      }}>
        <IconButton size={'small'} onClick={handleOpenButtonClick}>
          <OpenInNewIcon fontSize={'small'}/>
        </IconButton>
      </div>
      <Stage
        height={props.length}
        width={props.length}
        visible={blueprintContainerProperties.active}
        onMouseDown={handleMouseDown}
      >
        <Layer>
          <Group
            ref={blueprintContainerRef}
            x={blueprintContainerProperties.x}
            y={blueprintContainerProperties.y}
            scaleX={blueprintContainerProperties.scale}
            scaleY={blueprintContainerProperties.scale}
          >
            {DesignerStageUtils.buildComponentTree<BlueprintComponentDto>(
              props.blueprintComponentDto,
              blueprintContainerProperties.scale,
              true,
              null,
              null,
              false
            )}
          </Group>
        </Layer>
      </Stage>
      <Box
        style={{
          backgroundColor: '#ffffff',
          borderRadius: 15,
          height: 'auto',
          position: 'absolute',
          bottom: spacing.toolbox.cards.blueprints.previewPadding - 1,
          left: spacing.toolbox.cards.blueprints.previewPadding - 1,
          right: spacing.toolbox.cards.blueprints.previewPadding - 1,
          padding: 10,
          textAlign: 'center'
      }}
        boxShadow={1}
      >
        <Typography variant={'body2'}>{buildName()}</Typography>
      </Box>
    </Box>
  );
}

export default BlueprintComponentCard;
