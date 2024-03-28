import Konva from 'konva';
import { useStore } from '../../../../store';
import { useEffect } from 'react';
import { DesignerStageGroupName } from '../../enums/designer-stage-group-name.enum';
import { ComponentDto, WorkspaceDesignerDto } from '../../../../../gen/api-client';
import { DesignerUtils } from '../../designer.utils';

export function useSynchronizeDesignerTransformer(
  workspaceDto: WorkspaceDesignerDto,
  stageRef: React.MutableRefObject<Konva.Stage | null>,
  transformerRef: React.MutableRefObject<Konva.Transformer | null>
) {
  const transformActiveView = useStore(state => state.transformActiveDesignerView);
  const selectedElementUuids = useStore(state => state.selectedComponentUuids);
  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const stageProperties = useStore(state => state.designerStageProperties);

  return useEffect(() => {
    if (stageRef.current === null || transformerRef.current === null) {
      return;
    }

    if ((!transformActiveView && selectedElementUuids.length === 0)) {
      transformerRef.current.nodes([]);
      return;
    }

    if (activeViewUuid === null) {
      return;
    }

    const activeView = workspaceDto.views.find(view => view.uuid === activeViewUuid);

    if (activeView === undefined) {
      return;
    }

    const viewGroup = stageRef.current.findOne<Konva.Group>('#' + activeViewUuid);

    if (viewGroup === undefined) {
      return;
    }

    if (transformActiveView) {
      transformerRef.current.nodes(
        [...viewGroup.getChildren((node) => node.name() === 'view-background')]
      );
      return;
    }

    transformerRef.current.nodes([...viewGroup.getChildren(
      node => node instanceof Konva.Group && node.name() === DesignerStageGroupName.COMPONENTS_SELECTED
    )]);

    DesignerUtils.updateSelectedElementsProperties<ComponentDto>(
      stageProperties,
      transformerRef.current.nodes(),
      activeView.components,
      activeView
    );
  }, [activeViewUuid, transformActiveView, selectedElementUuids]);
}
