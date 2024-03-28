import { useEffect } from 'react';
import { DesignerUtils } from '../../modules/designer/designer.utils';
import { AppUtils } from '../../app.utils';
import { StageProperties } from '../../interfaces/stage-properties.interface';
import { StageDimensions } from '../../modules/designer/interfaces/stage-dimensions.interface';

export function useSynchronizeStageDimensions(
  initialStageProperties: {positionX: number, positionY: number, scale: number},
  setStageProperties: (stageProperties: StageProperties) => void,
  setStageDimensions: (stageDimensions: StageDimensions) => void | undefined
) {
  return useEffect(() => {
    if (setStageDimensions === undefined) {
      return;
    }

    // init stage properties
    setStageProperties({
      ...DesignerUtils.getDocumentStageDimensions(),
      x: initialStageProperties.positionX,
      y: initialStageProperties.positionY,
      scale: initialStageProperties.scale
    });

    // Adapt stage dimensions on resize
    const updateStageDimensions = AppUtils.debounce(
      () => setStageDimensions(DesignerUtils.getDocumentStageDimensions())
    );

    window.addEventListener('resize', updateStageDimensions);

    return () => window.removeEventListener('resize', updateStageDimensions);
  }, []);
}
