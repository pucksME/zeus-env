import React, { useEffect } from 'react';
import Konva from 'konva';
import { useStore } from '../../../../store';
import { BlueprintComponentDto } from '../../../../../gen/api-client';
import { DesignerUtils } from '../../designer.utils';
import { DesignerStageGroupName } from '../../enums/designer-stage-group-name.enum';
import { ComponentUtils } from '../../component.utils';

export function useSynchronizeDesignerBlueprintComponentTransformer(
  blueprintComponent: BlueprintComponentDto,
  stageRef: React.MutableRefObject<Konva.Stage | null>,
  transformerRef: React.MutableRefObject<Konva.Transformer | null>
) {
  const stageProperties = useStore.getState().designerStageBlueprintComponentProperties;
  const selectedElementUuids = useStore.getState().selectedComponentUuids;
  const transformActiveView = useStore(state => state.transformActiveDesignerView);

  return useEffect(() => {
    if (stageRef.current === null || transformerRef.current === null || !blueprintComponent) {
      return;
    }

    if (!transformActiveView && selectedElementUuids.length === 0) {
      transformerRef.current.nodes([]);
      return;
    }

    const selectedComponentsGroup = stageRef.current.findOne<Konva.Group>(
      '.' + DesignerStageGroupName.COMPONENTS_SELECTED
    );

    if (selectedComponentsGroup === undefined) {
      return;
    }

    transformerRef.current.nodes([selectedComponentsGroup]);

    DesignerUtils.updateSelectedElementsProperties<BlueprintComponentDto>(
      stageProperties,
      transformerRef.current.nodes(),
      ComponentUtils.getDirectChildComponents(blueprintComponent)
    );
  }, [blueprintComponent, selectedElementUuids]);
}
