import React, { useEffect } from 'react';
import Konva from 'konva';
import { useStore } from '../../../../store';
import { VisualizerStageGroupName } from '../../enums/visualizer-stage-group-name.enum';

export function useSynchronizeVisualizerTransformer(
  stageRef: React.MutableRefObject<Konva.Stage | null>,
  transformerRef: React.MutableRefObject<Konva.Transformer | null>
) {
  const selectedCodeModuleInstanceUuids = useStore(state => state.selectedCodeModuleInstanceUuids);

  return useEffect(() => {
    if (transformerRef.current === null) {
      return;
    }

    if (selectedCodeModuleInstanceUuids.length === 0) {
      transformerRef.current.nodes([]);
      return;
    }

    const selectedCodeModuleInstancesGroup = stageRef.current.findOne(
      '.' + VisualizerStageGroupName.CODE_MODULE_INSTANCES_SELECTED
    );

    if (selectedCodeModuleInstancesGroup === undefined) {
      return;
    }

    transformerRef.current.nodes([selectedCodeModuleInstancesGroup]);
  }, [selectedCodeModuleInstanceUuids]);
}
