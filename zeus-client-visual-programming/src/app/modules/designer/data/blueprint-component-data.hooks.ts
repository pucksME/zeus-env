import { useMutation, useQuery, useQueryClient } from 'react-query';
import {
  AlignElementsDto,
  BlueprintComponentDto, BlueprintElementDto,
  ComponentDto,
  ElementType,
  InstantiateBlueprintComponentDto,
  PositionElementsDto,
  ReshapeElementsDto,
  ScaleOrigin,
  UpdateBlueprintComponentNameDto,
  UpdateElementsPropertiesDto,
  WorkspaceDesignerDto
} from '../../../../gen/api-client';
import { QueryKeys } from '../../../enums/query-keys.enum';
import { BlueprintComponentService } from '../services/blueprint-component.service';
import { BlueprintComponentUtils } from '../blueprint-component.utils';
import { useStore } from '../../../store';
import { ComponentUtils } from '../component.utils';
import React from 'react';
import Konva from 'konva';
import { StageMode } from '../../../enums/stage-mode.enum';
import { UpdateElementSortingMutation } from './component-data.hooks';

interface UpdateBlueprintComponentNameMutation {
  blueprintComponentUuid: string;
  updateBlueprintComponentNameDto: UpdateBlueprintComponentNameDto;
}

export function useSaveBlueprintComponent(
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const queryKeyBlueprintComponents = QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid;
  const queryKeyWorkspace = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (componentUuid: string) => BlueprintComponentService.saveBlueprintComponent(componentUuid),
    {
      onMutate: async (componentUuid) => {

        if (!activeViewUuid) {
          return;
        }

        await queryClient.cancelQueries(queryKeyBlueprintComponents);
        await queryClient.cancelQueries(queryKeyWorkspace);

        const blueprintComponentsSnapshot = queryClient.getQueryData<BlueprintComponentDto[] | undefined>(
          queryKeyBlueprintComponents
        );

        const workspaceSnapshot = queryClient.getQueryData<WorkspaceDesignerDto | undefined>(queryKeyWorkspace);

        if (blueprintComponentsSnapshot === undefined || workspaceSnapshot === undefined) {
          return;
        }

        const activeView = workspaceSnapshot.views.find(view => view.uuid === activeViewUuid);

        if (activeView === undefined) {
          return;
        }

        const component = activeView.components.find(component => component.uuid === componentUuid);

        if (component === undefined) {
          return;
        }

        queryClient.setQueryData<BlueprintComponentDto[]>(
          queryKeyBlueprintComponents,
          [
            ComponentUtils.mapToBlueprintComponentDto(component),
            ...blueprintComponentsSnapshot
          ]);

      },
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKeyBlueprintComponents);
        await queryClient.invalidateQueries(queryKeyWorkspace);
      }
    }
  )
}

export function useBlueprintComponentsWorkspace(workspaceUuid: string) {
  const {
    isLoading,
    isError,
    data,
    error
  } = useQuery(
    QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid,
    () => BlueprintComponentService.getAllInWorkspace(workspaceUuid)
  );
  return {isLoading, isError, blueprintComponentDtos: data, error};
}

export function useInstantiateBlueprintComponent(
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const queryKeyWorkspace = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  const queryKeyBlueprintComponents = QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid;

  return useMutation(
    (instantiateBlueprintComponentDto: InstantiateBlueprintComponentDto) =>
      BlueprintComponentService.instantiateBlueprintComponent(instantiateBlueprintComponentDto),
    {
      onMutate: async (instantiateBlueprintComponentDto) => {
        await queryClient.cancelQueries(queryKeyWorkspace);
        const workspaceSnapshot: WorkspaceDesignerDto | undefined = queryClient.getQueryData<WorkspaceDesignerDto>(queryKeyWorkspace);

        if (workspaceSnapshot === undefined) {
          return;
        }

        const blueprintComponentsSnapshot: BlueprintComponentDto[] | undefined = queryClient.getQueryData<BlueprintComponentDto[]>(queryKeyBlueprintComponents);

        if (blueprintComponentsSnapshot === undefined) {
          return;
        }

        const blueprintComponent = blueprintComponentsSnapshot.find(blueprintComponent => blueprintComponent.uuid === instantiateBlueprintComponentDto.blueprintComponentUuid);

        if (blueprintComponent === undefined) {
          return;
        }

        const blueprintComponentInstance = BlueprintComponentUtils.mapToComponentDto(
          blueprintComponent,
          {
            x: instantiateBlueprintComponentDto.positionX,
            y: instantiateBlueprintComponentDto.positionY
          }
          );

        queryClient.setQueryData(queryKeyWorkspace, {
          ...workspaceSnapshot,
          views: workspaceSnapshot.views.map(view => (view.uuid !== instantiateBlueprintComponentDto.viewUuid)
            ? view
            : {
            ...view,
              components: (instantiateBlueprintComponentDto.parentComponentUuid === undefined)
                ? [blueprintComponentInstance, ...view.components]
                : ComponentUtils.traverseComponentTrees<ComponentDto>(
                  view.components,
                  (component) => (component.uuid !== instantiateBlueprintComponentDto.parentComponentUuid)
                    ? component
                    : {
                    ...component,
                      elements: [
                        {
                          element: blueprintComponentInstance,
                          type: ElementType.Component
                        },
                        ...component.elements
                      ]
                  }
                )
            })
        });
      },
      onSettled: (data, error, variables, context) => queryClient.invalidateQueries(queryKeyWorkspace)
    }
  );
}

export function useUpdateBlueprintComponentName(workspaceUuid: string) {
  const queryClient = useQueryClient();
  const queryKey = QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid;

  return useMutation(
    (updateBlueprintComponentNameMutation: UpdateBlueprintComponentNameMutation) =>
      BlueprintComponentService.updateName(
        updateBlueprintComponentNameMutation.blueprintComponentUuid,
        updateBlueprintComponentNameMutation.updateBlueprintComponentNameDto
      ),
    {
      onMutate: async (updateBlueprintComponentNameMutation) => {
        await queryClient.cancelQueries(queryKey);
        const blueprintComponentsSnapshot = queryClient.getQueryData<BlueprintComponentDto[] | undefined>(queryKey);

        if (blueprintComponentsSnapshot === undefined) {
          return;
        }

        queryClient.setQueryData<BlueprintComponentDto[]>(
          queryKey,
          ComponentUtils.traverseComponentTrees<BlueprintComponentDto>(
            blueprintComponentsSnapshot,
            (blueprintComponent) =>
              (blueprintComponent.uuid !== updateBlueprintComponentNameMutation.blueprintComponentUuid)
                ? blueprintComponent
                : { ...blueprintComponent, name: updateBlueprintComponentNameMutation.updateBlueprintComponentNameDto.name }
          )
        );
      },
      onSettled: (data, error, variables, context) => queryClient.invalidateQueries(queryKey)
    }
  )
}

export function useScaleBlueprintElements(
  selectedBlueprintComponentsRef: React.MutableRefObject<Konva.Group>,
  scaleOrigin: ScaleOrigin,
  positionBeforeTransform: { x: number, y: number },
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const stageBlueprintComponentProperties = useStore.getState().designerStageBlueprintComponentProperties;
  const focusedComponentUuid = useStore.getState().focusedComponentUuid;
  const selectedElementUuids = useStore.getState().selectedComponentUuids;
  const selectedElementsProperties = useStore.getState().selectedElementsProperties;
  const refreshSelectedElementUuids = useStore.getState().refreshSelectedComponentUuids;
  const queryKeyBlueprintComponents = QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid;
  const queryKeyWorkspace = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (scale: { x: number, y: number }) => BlueprintComponentService.scaleBlueprintElements({
      parentComponentUuid: (focusedComponentUuid === null)
        ? stageBlueprintComponentProperties.blueprintComponentUuid
        : focusedComponentUuid,
      elementUuids: selectedElementUuids,
      elementsProperties: {
        height: selectedElementsProperties.height,
        width: selectedElementsProperties.width,
        x: selectedElementsProperties.x,
        y: selectedElementsProperties.y
      },
      scaleOrigin,
      scaleX: scale.x,
      scaleY: scale.y
    }),
    {
      onMutate: async (scale) => {
        await queryClient.cancelQueries(queryKeyBlueprintComponents);
        await queryClient.cancelQueries(queryKeyWorkspace);
      },
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKeyBlueprintComponents);
        selectedBlueprintComponentsRef.current.position(positionBeforeTransform);
        selectedBlueprintComponentsRef.current.scale({ x: 1, y: 1 });
        await queryClient.invalidateQueries(queryKeyWorkspace);
        refreshSelectedElementUuids();
      }
    }
  );
}

export function useReshapeBlueprintElements(
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore.getState().setConfiguratorIsLoading;
  const refreshSelectedElementUuids = useStore.getState().refreshSelectedComponentUuids;

  const queryKeyBlueprintComponents = QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid;
  const queryKeyWorkspace = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (reshapeElementsDto: ReshapeElementsDto) =>
      BlueprintComponentService.reshapeBlueprintElements(reshapeElementsDto),
    {
      onMutate: (reshapeElementsDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKeyBlueprintComponents);
        await queryClient.invalidateQueries(queryKeyWorkspace);
        refreshSelectedElementUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}

export function useTranslateBlueprintElements(
  selectedComponentsRef: React.MutableRefObject<Konva.Group>,
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const focusedComponentUuid = useStore.getState().focusedComponentUuid;
  const selectedElementUuids = useStore.getState().selectedComponentUuids;
  const refreshSelectedElementUuids = useStore.getState().refreshSelectedComponentUuids;
  const stageBlueprintComponentProperties = useStore.getState().designerStageBlueprintComponentProperties;
  const queryKeyBlueprintComponents = QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid;
  const queryKeyWorkspace = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (translate: {x: number, y: number}) => BlueprintComponentService.translateBlueprintElements({
      parentComponentUuid: (focusedComponentUuid === null)
        ? stageBlueprintComponentProperties.blueprintComponentUuid
        : focusedComponentUuid,
      elementUuids: selectedElementUuids,
      translateX: translate.x,
      translateY: translate.y
    }),
    {
      onMutate: async (translate) => await queryClient.cancelQueries(queryKeyBlueprintComponents),
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKeyBlueprintComponents);
        selectedComponentsRef.current.position({ x: 0, y: 0 });
        await queryClient.invalidateQueries(queryKeyWorkspace);
        refreshSelectedElementUuids();
      }
    }
  )
}

export function usePositionBlueprintElements(workspaceUuid: string) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore.getState().setConfiguratorIsLoading;
  const refreshSelectedElementUuids = useStore.getState().refreshSelectedComponentUuids;
  const queryKeyBlueprintComponents = QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid;
  const queryKeyWorkspace = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (positionElementsDto: PositionElementsDto) =>
      BlueprintComponentService.positionBlueprintElements(positionElementsDto),
    {
      onMutate: (positonElementsDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKeyBlueprintComponents);
        await queryClient.invalidateQueries(queryKeyWorkspace);
        refreshSelectedElementUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}

export function useAlignBlueprintElements(
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore.getState().setConfiguratorIsLoading;
  const refreshSelectedElementUuids = useStore.getState().refreshSelectedComponentUuids;
  const queryKeyBlueprintComponents = QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid;
  const queryKeyWorksapce = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (alignElementsDto: AlignElementsDto) =>
      BlueprintComponentService.alignBlueprintElements(alignElementsDto),
    {
      onMutate: (alignElementsDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKeyBlueprintComponents);
        await queryClient.invalidateQueries(queryKeyWorksapce);
        refreshSelectedElementUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  );
}

export function useUpdateBlueprintElementsProperties(
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore.getState().setConfiguratorIsLoading;
  const refreshSelectedElementUuids = useStore.getState().refreshSelectedComponentUuids;
  const queryKeyBlueprintComponents = QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid;
  const queryKeyWorkspace = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (updateElementsPropertiesDto: UpdateElementsPropertiesDto) =>
      BlueprintComponentService.updateBlueprintElementsProperties(updateElementsPropertiesDto),
    {
      onMutate: (updateElementsPropertiesDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKeyBlueprintComponents);
        await queryClient.invalidateQueries(queryKeyWorkspace);
        refreshSelectedElementUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}

export function useDeleteBlueprintElements(
  workspaceUuid: string,
  stageMode: StageMode.DESIGNER | StageMode.DESIGNER_BLUEPRINT_COMPONENT
) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore.getState().setConfiguratorIsLoading;
  const focusedComponentUuid = useStore.getState().focusedComponentUuid;
  const selectedElementUuids = useStore.getState().selectedComponentUuids;
  const setSelectedElementUuids = useStore.getState().setSelectedComponentUuids;
  const stageBlueprintComponentProperties = useStore.getState().designerStageBlueprintComponentProperties;
  const resetStageBlueprintComponentProperties = useStore.getState().resetDesignerStageBlueprintComponentProperties;
  const queryKeyBlueprintComponents = QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid;
  const queryKeyWorkspace = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    () => BlueprintComponentService.deleteBlueprintElements({
      parentComponentUuid: (stageMode === StageMode.DESIGNER)
        ? null
        : (focusedComponentUuid === null)
          ? stageBlueprintComponentProperties.blueprintComponentUuid
          : focusedComponentUuid,
      elementUuids: (stageMode === StageMode.DESIGNER)
        ? [stageBlueprintComponentProperties.blueprintComponentUuid]
        : selectedElementUuids
    }),
    {
      onMutate: () => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        setSelectedElementUuids([]);

        if (stageMode === StageMode.DESIGNER && stageBlueprintComponentProperties.blueprintComponentUuid !== null) {
          resetStageBlueprintComponentProperties();
        }

        await queryClient.invalidateQueries(queryKeyBlueprintComponents);
        await queryClient.invalidateQueries(queryKeyWorkspace);
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}

export function useUpdateBlueprintElementSorting(
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const queryKeyBlueprintComponents = QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid;
  const queryKeyWorkspace = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (updateElementSortingMutation: UpdateElementSortingMutation) =>
      BlueprintComponentService.updateBlueprintElementSorting(updateElementSortingMutation.updateElementSortingDto),
    {
      onMutate: async (updateElementSortingMutation) => {
        await queryClient.cancelQueries(queryKeyBlueprintComponents);

        const blueprintComponentsSnapshot = queryClient.getQueryData<BlueprintComponentDto[] | undefined>(
          queryKeyBlueprintComponents
        );

        if (blueprintComponentsSnapshot === undefined) {
          return;
        }

        queryClient.setQueryData(
          queryKeyBlueprintComponents,
          ComponentUtils.traverseComponentTrees<BlueprintComponentDto>(
            blueprintComponentsSnapshot,
            (blueprintComponent) =>
              (blueprintComponent.uuid !== (
                focusedComponentUuid === null
                  ? stageBlueprintComponentProperties.blueprintComponentUuid
                  : focusedComponentUuid
              ))
                ? blueprintComponent
                : {
                  ...blueprintComponent,
                  elements: ComponentUtils.sortComponentElements<BlueprintElementDto>(
                    blueprintComponent.elements,
                    updateElementSortingMutation.updateElementSortingDto.elementUuid,
                    updateElementSortingMutation.oldSorting,
                    updateElementSortingMutation.updateElementSortingDto.sorting
                  )
                }
            ));
      },
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKeyBlueprintComponents);
        await queryClient.invalidateQueries(queryKeyWorkspace);
      }
    }
  )
}
