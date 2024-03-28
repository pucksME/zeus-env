import React from 'react';

import './configurator-preview-view.module.scss';
import { ViewDto } from '../../../../../gen/api-client';
import spacing from '../../../../../assets/styling/spacing.json';
import { Group, Layer, Rect, Stage } from 'react-konva';

export interface ConfiguratorPreviewViewProps {
  view: ViewDto;
}

export function ConfiguratorPreviewView(props: ConfiguratorPreviewViewProps) {

  const scaleTo = Math.min(
    (spacing.configurator.previewHeight - (2 * spacing.configurator.previewPadding)) / props.view.height,
    (spacing.configurator.width - (2 * spacing.configurator.previewPadding)) / props.view.width
  );

  return (
    <Stage height={spacing.configurator.previewHeight} width={spacing.configurator.width}>
      <Layer>
        <Rect height={spacing.configurator.previewHeight} width={spacing.configurator.width} fill={'#ffffff'} />
        <Group
          x={(spacing.configurator.width - (props.view.width * scaleTo)) / 2}
          y={(spacing.configurator.previewHeight - (props.view.height * scaleTo)) / 2}
          scaleX={scaleTo}
          scaleY={scaleTo}
        >
          <Rect height={props.view.height} width={props.view.width} fill={'#f7f7f7'} />
        </Group>
      </Layer>
    </Stage>
  );
}

export default ConfiguratorPreviewView;
