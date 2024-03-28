import { useQuery } from 'react-query';
import { QueryKeys } from '../../../enums/query-keys.enum';
import { VisualizerWorkspaceService } from '../services/visualizer-workspace.service';

export function useVisualizerWorkspace(componentUuid: string) {
  const {
    isLoading,
    isError,
    data,
    error
  } = useQuery(
    QueryKeys.VISUALIZER_WORKSPACE + componentUuid,
    () => VisualizerWorkspaceService.getWithComponentUuid(componentUuid)
  );
  return {isLoading, isError, workspaceVisualizerDto: data, error};
}
