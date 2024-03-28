import { useMutation, useQuery, useQueryClient } from 'react-query';
import { ProjectService } from '../services/project.service';
import { QueryKeys } from '../../../enums/query-keys.enum';
import { ExportProjectDto } from '../../../../gen/api-client';

export function useProject(projectUuid: string) {
  const {
    isLoading,
    isError,
    data,
    error
  } = useQuery(
    QueryKeys.PROJECT + projectUuid,
    () => ProjectService.getProject(projectUuid),
    {enabled: projectUuid !== undefined}
  );
  return { isLoading, isError, projectDto: data, error };
}

export function useProjects() {
  const {
    isLoading,
    isError,
    data,
    error
  } = useQuery(QueryKeys.PROJECTS, () => ProjectService.getProjects());
  return { isLoading, isError, projectGalleryDtos: data, error };
}

export function useExportProject(projectUuid: string) {
  const queryClient = useQueryClient();
  return useMutation(
    (exportProjectDto: ExportProjectDto) => ProjectService.exportProject(projectUuid, exportProjectDto),
    {
      onSettled: async (data, error, variables, context) =>
        await queryClient.invalidateQueries(QueryKeys.EXPORTED_PROJECTS)
    }
  )
}

export function useDeleteExportedProject(projectUuid: string) {
  const queryClient = useQueryClient();
  return useMutation(
    () => ProjectService.deleteExportedProject(projectUuid),
    {
      onSettled: async (data, error, variables, constex) =>
        await queryClient.invalidateQueries(QueryKeys.EXPORTED_PROJECTS)
    }
  )
}

export function useExportedProjects() {
  const {
    isLoading,
    isError,
    data,
    error
  } = useQuery(
    QueryKeys.EXPORTED_PROJECTS,
    () => ProjectService.findExportedProjects()
  );
  return {isLoading, isError, exportedProjectDtos: data, error};
}
