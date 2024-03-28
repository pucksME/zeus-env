import Konva from 'konva';
import { useStore } from '../../../store';

export abstract class VisualizerSelectionTransformerEventService {
  static handleDragStart(event: Konva.KonvaEventObject<DragEvent>) {
    useStore.getState().setVisualizerSelectionTransformerProperties({
      ...useStore.getState().visualizerSelectionTransformerProperties,
      position: {
        x: event.target.x(),
        y: event.target.y()
      }
    });
  }

  static handleDragMove(event: Konva.KonvaEventObject<DragEvent>) {
    useStore.getState().setVisualizerSelectionTransformerProperties({
      ...useStore.getState().visualizerSelectionTransformerProperties,
      dragOffset: {
        x: useStore.getState().visualizerSelectionTransformerProperties.position.x - event.target.x(),
        y: useStore.getState().visualizerSelectionTransformerProperties.position.y - event.target.y()
      }
    });
  }
}
