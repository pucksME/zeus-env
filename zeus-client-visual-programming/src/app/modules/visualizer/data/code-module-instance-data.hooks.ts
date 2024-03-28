import { QueryClient, useMutation, useQuery, useQueryClient } from 'react-query';
import { QueryKeys } from '../../../enums/query-keys.enum';
import {
  CodeModuleDto,
  CreateCodeModuleInstanceDto,
  DeleteCodeModuleInstancesDto,
  WorkspaceVisualizerDto
} from '../../../../gen/api-client';
import { CodeModuleInstanceService } from '../services/code-module-instance.service';
import {v4 as generateUuid} from 'uuid';
import { useStore } from '../../../store';
import React from 'react';
import Konva from 'konva';
import { CodeModuleInstancesConnection } from '../../../interfaces/code-module-instances-connection.interface';

export type QueryKeysCodeModules = QueryKeys.SYSTEM_CODE_MODULES | QueryKeys.PROJECT_CODE_MODULES;

export interface InstantiateCodeModuleMutation {
  queryKeyCodeModules: QueryKeysCodeModules;
  createCodeModuleInstanceDto: CreateCodeModuleInstanceDto;
}

export function useInstantiateCodeModule(
  queryClient: QueryClient,
  componentUuid: string
) {
  const queryKeyWorkspace = QueryKeys.VISUALIZER_WORKSPACE + componentUuid;
  return useMutation(
    (instantiateCodeModuleMutation: InstantiateCodeModuleMutation) =>
      CodeModuleInstanceService.instantiateCodeModule(
        componentUuid, instantiateCodeModuleMutation.createCodeModuleInstanceDto
      ),
    {
      onMutate: async (instantiateCodeModuleMutation) => {
        await queryClient.cancelQueries(queryKeyWorkspace);
        const {queryKeyCodeModules, createCodeModuleInstanceDto} = instantiateCodeModuleMutation;
        const workspaceSnapshot: WorkspaceVisualizerDto | undefined =
          queryClient.getQueryData<WorkspaceVisualizerDto>(queryKeyWorkspace);

        if (workspaceSnapshot === undefined) {
          return;
        }

        const codeModulesSnapshot: CodeModuleDto[] | undefined =
          queryClient.getQueryData<CodeModuleDto[]>(queryKeyCodeModules);

        if (codeModulesSnapshot === undefined) {
          return;
        }

        const codeModule = codeModulesSnapshot.find(
          codeModuleDto => codeModuleDto.uuid === createCodeModuleInstanceDto.codeModuleUuid
        );

        if (codeModule === undefined) {
          return;
        }

        queryClient.setQueryData(queryKeyWorkspace, {
          ...workspaceSnapshot,
          codeModuleInstances: [
            {
              uuid: generateUuid(),
              name: codeModule.name,
              inputEndpoints: codeModule.inputEndpoints,
              outputEndpoints: codeModule.outputEndpoints,
              codeModuleUuid: codeModule.uuid,
              flowDescription: '',
              positionX: createCodeModuleInstanceDto.positionX,
              positionY: createCodeModuleInstanceDto.positionY
            },
            ...workspaceSnapshot.codeModuleInstances
          ]
        })
      },
      onSettled: (data, error, variables, context) => queryClient.invalidateQueries(queryKeyWorkspace)
    }
  )
}

export function useTranslateCodeModuleInstances(
  queryClient: QueryClient,
  componentUuid: string,
  selectedCodeModuleInstancesRef: React.MutableRefObject<Konva.Group>
) {
  const queryKey = QueryKeys.VISUALIZER_WORKSPACE + componentUuid;
  const selectedCodeModuleInstanceUuids = useStore(state => state.selectedCodeModuleInstanceUuids);
  const setVisualizerSelectionTransformerProperties = useStore(state => state.setVisualizerSelectionTransformerProperties);

  return useMutation(
    (translate: {x: number, y: number}) => CodeModuleInstanceService.translateCodeModuleInstances({
      codeModuleInstanceUuids: selectedCodeModuleInstanceUuids,
      translateX: translate.x,
      translateY: translate.y
    }),
    {
      onMutate: async (translate) => await queryClient.cancelQueries(queryKey),
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKey);
        selectedCodeModuleInstancesRef.current.position({x: 0, y: 0});
        setVisualizerSelectionTransformerProperties({
          position: {x: 0, y: 0},
          dragOffset: {x: 0, y: 0}
        });
      }
    }
  )
}

export function useDeleteCodeModuleInstances(componentUuid: string) {
  const queryClient = useQueryClient();
  const queryKey = QueryKeys.VISUALIZER_WORKSPACE + componentUuid;
  const setIsLoading = useStore(state => state.setConfiguratorIsLoading);
  const setSelectedCodeModuleInstanceUuids = useStore(state => state.setSelectedCodeModuleInstanceUuids);

  return useMutation(
    (deleteCodeModuleInstancesDto: DeleteCodeModuleInstancesDto) =>
      CodeModuleInstanceService.deleteCodeModuleInstances(deleteCodeModuleInstancesDto),
    {
      onMutate: () => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, errors, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        setSelectedCodeModuleInstanceUuids([]);
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}

export function useSaveConnection(componentUuid: string) {
  const queryClient = useQueryClient();
  const queryKeyVisualizerWorkspace = QueryKeys.VISUALIZER_WORKSPACE + componentUuid;
  const queryKeyVisualizerWorkspaceConnections = QueryKeys.VISUALIZER_WORKSPACE_CONNECTIONS + componentUuid;

  return useMutation(
    (codeModuleInstancesConnection: CodeModuleInstancesConnection) => CodeModuleInstanceService.saveConnection(
      componentUuid,
      {
        uuid: generateUuid(),
        inputCodeModuleInstanceName: codeModuleInstancesConnection.input.codeModuleName,
        inputCodeModuleInstancePortName: codeModuleInstancesConnection.input.portName,
        outputCodeModuleInstanceName: codeModuleInstancesConnection.output.codeModuleName,
        outputCodeModuleInstancePortName: codeModuleInstancesConnection.output.portName
      }
    ),
    {
      onMutate: async (codeModuleInstancesConnection) => await queryClient.cancelQueries(queryKeyVisualizerWorkspace),
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKeyVisualizerWorkspace);
        await queryClient.invalidateQueries(queryKeyVisualizerWorkspaceConnections);
      }
    }
  )
}

export function useConnections(componentUuid: string) {
  const {
    isLoading,
    isError,
    data,
    error
  } = useQuery(
    QueryKeys.VISUALIZER_WORKSPACE_CONNECTIONS + componentUuid,
    () => CodeModuleInstanceService.getConnections(componentUuid)
  );
  return {isLoading, isError, codeModuleInstancesConnectionDtos: data, error};
}

export function useDeleteConnection(componentUuid: string) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore(state => state.setConfiguratorIsLoading);
  const queryKey = QueryKeys.VISUALIZER_WORKSPACE_CONNECTIONS + componentUuid;
  return useMutation(
    (codeModuleInstanceConnectionUuid: string) => CodeModuleInstanceService.deleteConnection(codeModuleInstanceConnectionUuid),
    {
      onMutate: () => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}
