import React, { useEffect, useState } from 'react';

import './configurator-preview-elements.module.scss';
import spacing from '../../../../../assets/styling/spacing.json';
import { Group, Layer, Rect, Stage } from 'react-konva';
import { DesignerStageUtils } from '../../designer-stage.utils';
import { BlueprintComponentDto, ComponentDto, ViewDto } from '../../../../../gen/api-client';
import { useStore } from '../../../../store';
import { AppUtils } from '../../../../app.utils';

export interface ConfiguratorPreviewElementsProps {
  components: ComponentDto[] | BlueprintComponentDto[]
}

export function ConfiguratorPreviewElements(
  props: ConfiguratorPreviewElementsProps
) {

  const [containerProperties, setContainerProperties] = useState<{
    x: number,
    y: number,
    scale: number
  } | null>(null);

  const selectedElementsProperties = useStore(state => state.selectedElementsProperties);

  useEffect(() => {
    setContainerProperties(AppUtils.calculateContainerPropertiesToFitElements(
      {
        height: spacing.configurator.previewHeight,
        width: spacing.configurator.width
      },
      {
        height: selectedElementsProperties.height,
        width: selectedElementsProperties.width,
        x: selectedElementsProperties.positionRelativeToView.x,
        y: selectedElementsProperties.positionRelativeToView.y
      },
      {
        horizontal: spacing.configurator.previewPadding,
        vertical: spacing.configurator.previewPadding
      }
    ));
  }, [selectedElementsProperties]);

  const { selectedComponents } = DesignerStageUtils.buildComponentTrees<ComponentDto | BlueprintComponentDto>(
    props.components,
    1,
    null,
    false
  );

  return (
    <Stage height={spacing.configurator.previewHeight} width={spacing.configurator.width}>
      <Layer>
        <Rect height={spacing.configurator.previewHeight} width={spacing.configurator.width}
              fill={'#ffffff'} />
        {(containerProperties === null)
          ? null
          : <Group
            x={containerProperties.x}
            y={containerProperties.y}
            scaleX={containerProperties.scale}
            scaleY={containerProperties.scale}
        >
          {selectedComponents}
        </Group>}
      </Layer>
    </Stage>
  );
}

export default ConfiguratorPreviewElements;
