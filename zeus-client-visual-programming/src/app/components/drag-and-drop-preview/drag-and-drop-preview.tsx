import React, { useEffect, useState } from 'react';

import './drag-and-drop-preview.module.scss';
import { Layer, Stage } from 'react-konva';
import Konva from 'konva';
import zIndices from '../../../assets/styling/z-indices.json';
import spacing from '../../../assets/styling/spacing.json';

export interface BlueprintComponentDragAndDropPreviewProps {
  children: React.ReactNode;
  properties: {height: number, width: number, scale: number};
  active: boolean;
  onDrop: (event: Konva.KonvaEventObject<MouseEvent>) => void;
}

export function DragAndDropPreview(
  props: BlueprintComponentDragAndDropPreviewProps
) {

  const [position, setPosition] = useState<{x: number, y: number}>({x: 0, y: 0});

  useEffect(() => {
    const mouseMoveEventListener = (event) => setPosition({x: event.x, y: event.y});
    window.addEventListener('mousemove', mouseMoveEventListener);
    return () => window.removeEventListener('mousemove', mouseMoveEventListener);
  }, []);

  return (!props.active) ? null : (
    <div style={{
      position: 'absolute',
      top: position.y - spacing.navigation.height - props.properties.height,
      left: position.x - props.properties.width,
      zIndex: zIndices.dragAndDropPreview.preview
    }}>
      <Stage
        height={props.properties.height * 2}
        width={props.properties.width * 2}
        x={props.properties.width}
        y={props.properties.height}
        scaleX={props.properties.scale}
        scaleY={props.properties.scale}
        onMouseUp={props.onDrop}
      >
        <Layer>
          {props.children}
        </Layer>
      </Stage>
    </div>
  );
}

export default DragAndDropPreview;
