import React, { CSSProperties, useEffect, useState } from 'react';

import './window.module.scss';
import spacing from '../../../assets/styling/spacing.json';
import zIndices from '../../../assets/styling/z-indices.json';
import { Box, IconButton, Typography } from '@material-ui/core';
import colors from '../../../assets/styling/colors.json';
import Input, { InputChangeEvent } from '../input/input';
import { DesignerUtils } from '../../modules/designer/designer.utils';
import EditIcon from '@material-ui/icons/Edit';
import CloseIcon from '@material-ui/icons/Close';
import ResizeIcon from '@material-ui/icons/AspectRatio';

export interface WindowProps {
  id: string;
  title: string;
  visible: boolean;
  position: { x: number, y: number };
  onChangePosition: (position: { x: number, y: number }) => void;
  initialContentHeight?: number;
  initialContentWidth?: number;
  minContentHeight?: number;
  minContentWidth?: number;
  contentRef?: React.MutableRefObject<HTMLDivElement>
  actions?: React.ReactNode;
  onClose?: () => void;
  onMouseDownMove?: () => void;
  onResize?: (height: number, width: number) => void;
  onChangeTitle?: (event: InputChangeEvent) => void;
  onSaveTitle?: () => void;
  contentStyle?: CSSProperties;
  onContentScroll?: (event: React.UIEvent<HTMLDivElement>) => void;
  children: React.ReactNode;
  overlay?: React.ReactNode;
}

enum MouseDownMode {
  MOVE,
  RESIZE
}

export function Window(props: WindowProps) {
  const minDimensions = {
    height: (props.minContentHeight !== undefined) ? props.minContentHeight : 0,
    width: (props.minContentWidth !== undefined) ? props.minContentWidth : 0
  };
  const [dimensions, setDimensions] = useState<{ height: number, width: number }>({
    height: (props.initialContentHeight !== undefined && props.initialContentHeight >= minDimensions.height)
      ? props.initialContentHeight
      : minDimensions.height,
    width: (props.initialContentWidth !== undefined && props.initialContentWidth >= minDimensions.width)
      ? props.initialContentWidth
      : minDimensions.width,
  });
  const [visible, setVisible] = useState<boolean>(props.visible);
  const [editTitleModeEnabled, setEditTitleModeEnabled] = useState<boolean>(false);
  const [mouseDownPosition, setMouseDownPosition] = useState<{
    x: number,
    y: number,
    mode: MouseDownMode
  } | null>(null);

  const topPanelHeight = spacing.window.topPanelHeight;
  const resizeIconOffset = 20;

  useEffect(() => {
    const mouseMoveListener = (event) => {
      if (mouseDownPosition === null) {
        return;
      }

      if (props.onMouseDownMove !== undefined) {
        props.onMouseDownMove();
      }

      if (mouseDownPosition.mode === MouseDownMode.MOVE) {
        props.onChangePosition({
          x: props.position.x + event.x - mouseDownPosition.x,
          y: props.position.y + event.y - mouseDownPosition.y
        });
      }

      if (mouseDownPosition.mode === MouseDownMode.RESIZE) {
        const height = event.y - spacing.navigation.height - props.position.y - topPanelHeight + resizeIconOffset;
        const width = event.x - props.position.x + resizeIconOffset;

        if (height < minDimensions.height || width < minDimensions.width) {
          setMouseDownPosition(null);
          return;
        }

        setDimensions({height, width});

        if (props.onResize !== undefined) {
          props.onResize(height, width);
        }
      }
    };

    window.addEventListener('mousemove', mouseMoveListener);
    return () => window.removeEventListener('mousemove', mouseMoveListener);
  }, [mouseDownPosition]);

  useEffect(() => setEditTitleModeEnabled(false), [props.id]);

  const handleClose = () => {
    setVisible(false);

    if (props.onClose !== undefined) {
      props.onClose();
    }
  }

  if (!visible) {
    return null;
  }

  const handleMouseDownResize = (event: React.MouseEvent<HTMLButtonElement>) => {
    event.stopPropagation();
    setMouseDownPosition({ x: event.clientX, y: event.clientY, mode: MouseDownMode.RESIZE});
  };

  const handleSaveTitle = () => {
    setEditTitleModeEnabled(false);
    props.onSaveTitle();
  }

  return (
    <div style={{
      position: 'absolute',
      left: props.position.x,
      top: props.position.y,
      zIndex: zIndices.window.window
    }}>
      <Box
        style={{
          backgroundColor: '#ffffff',
          borderRadius: 10,
          overflow: 'hidden'
        }}
        boxShadow={1}
        onMouseDown={(event) =>
          setMouseDownPosition({ x: event.clientX, y: event.clientY, mode: MouseDownMode.MOVE })}
        onMouseUp={() => setMouseDownPosition(null)}
      >
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          height: topPanelHeight,
          backgroundColor: colors.background_light,
          paddingLeft: 15,
          paddingRight: 15
        }}>
          {(!editTitleModeEnabled)
            ? <Typography
              style={{
                cursor: 'default'
              }}
              color={'textPrimary'}
              variant={'body1'}
            >
              {props.title}
            </Typography>
            : <div style={{marginRight: 10, position: 'relative', width: '100%'}}>
              <Input
                style={{width: '100%'}}
                value={props.title}
                onChange={props.onChangeTitle}
                onSubmit={handleSaveTitle}
              />
              <div style={{position: 'absolute', right: 0, top: 5}}>
                {DesignerUtils.buildNameEditingActions(
                  handleSaveTitle,
                  () => setEditTitleModeEnabled(false)
                )}
              </div>
            </div>}
          <div style={{display: 'flex', alignItems: 'center'}}>
            {(props.onSaveTitle === undefined || editTitleModeEnabled)
              ? null
              : <IconButton size={'small'} onClick={() => setEditTitleModeEnabled(true)}>
                <EditIcon fontSize={'small'} />
              </IconButton>}
            {props.actions}
            <IconButton size={'small'} onClick={handleClose}>
              <CloseIcon fontSize={'small'}/>
            </IconButton>
          </div>
        </div>
        <div style={{
          position: 'absolute',
          bottom: 10,
          right: 10,
          zIndex: zIndices.window.windowResizeButton
        }}>
          <IconButton
            size={'small'}
            style={{cursor: 'se-resize'}}
            onMouseDown={handleMouseDownResize}
            onMouseUp={(event) => setMouseDownPosition(null)}
          >
            <ResizeIcon fontSize={'small'}/>
          </IconButton>
        </div>
        <div
          ref={props.contentRef}
          style={{
            ...props.contentStyle,
            height: dimensions.height,
            width: dimensions.width
        }}
          onScroll={props.onContentScroll}
        >
          {props.children}
        </div>
      </Box>
      <div style={{
        position: 'absolute',
        left: 0,
        top: 0,
        zIndex: zIndices.window.overlay
      }}>{props.overlay}</div>
    </div>
  );
}

export default Window;
