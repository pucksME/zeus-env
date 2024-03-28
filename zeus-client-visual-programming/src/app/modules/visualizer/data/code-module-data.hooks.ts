import { useMutation, useQuery, useQueryClient } from 'react-query';
import { QueryKeys } from '../../../enums/query-keys.enum';
import { CodeModuleService } from '../services/code-module.service';

export function useProjectCodeModules(projectUuid: string) {
  const {
    isLoading,
    isError,
    data,
    error
  } = useQuery(
    QueryKeys.PROJECT_CODE_MODULES + projectUuid,
    () => CodeModuleService.getProjectCodeModules(projectUuid)
  );
  return {isLoading, isError, codeModuleDtos: data, error};
}

export function useSystemCodeModules() {
  const {
    isLoading,
    isError,
    data,
    error
  } = useQuery(
    QueryKeys.SYSTEM_CODE_MODULES,
    () => CodeModuleService.getSystemCodeModules()
  );
  return {isLoading, isError, codeModuleDtos: data, error};
}

export function useSaveCodeModule(
  projectUuid: string
) {
  const queryClient = useQueryClient();
  const queryKeyProjectCodeModules = QueryKeys.PROJECT_CODE_MODULES + projectUuid;
  return useMutation(
    () => CodeModuleService.save(projectUuid),
    {
      onSettled: async (data, error, variables, context) =>
        await queryClient.invalidateQueries(queryKeyProjectCodeModules)
    }
  )
}

export function useDeleteCodeModule(
  projectUuid: string,
  componentUuid: string
) {
  const queryClient = useQueryClient();
  const queryKeyProjectCodeModules = QueryKeys.PROJECT_CODE_MODULES + projectUuid;
  const queryKeyWorkspace = QueryKeys.VISUALIZER_WORKSPACE + componentUuid;
  return useMutation(
    (codeModuleUuid: string) => CodeModuleService.delete(codeModuleUuid),
    {
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKeyProjectCodeModules);
        await queryClient.invalidateQueries(queryKeyWorkspace);
      }
    }
  )
}
