import { useQuery } from 'react-query';
import { QueryKeys } from '../../../enums/query-keys.enum';
import { DesignerWorkspaceService } from '../services/designer-workspace.service';

export function useDesignerWorkspace(workspaceUuid: string | null) {
  const {
    isLoading,
    isError,
    data,
    error
  } = useQuery(
    QueryKeys.DESIGNER_WORKSPACE + workspaceUuid,
    () => DesignerWorkspaceService.get(workspaceUuid),
    { enabled: workspaceUuid !== null }
  );
  return { isLoading, isError, workspaceDto: data, error };
}
