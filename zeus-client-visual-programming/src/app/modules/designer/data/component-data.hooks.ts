import { QueryClient, useMutation, useQueryClient } from 'react-query';
import { ComponentService } from '../services/component.service';
import {
  AlignElementsDto,
  ComponentDto,
  CreateComponentDto, ElementDto,
  ElementType,
  PositionElementsDto,
  ReshapeElementsDto,
  ScaleOrigin,
  UpdateComponentNameDto,
  UpdateElementSortingDto,
  UpdateElementsPropertiesDto,
  WorkspaceDesignerDto
} from '../../../../gen/api-client';
import { ToolType } from '../../../enums/tool-type.enum';
import { QueryKeys } from '../../../enums/query-keys.enum';
import { v4 as generateUuid } from 'uuid';
import React from 'react';
import Konva from 'konva';
import { useStore } from '../../../store';
import { AppUtils } from '../../../app.utils';
import { ComponentUtils } from '../component.utils';

export interface SaveComponentMutation {
  viewUuid: string;
  createComponentDto: CreateComponentDto;
}

export interface UpdateElementSortingMutation {
  oldSorting: number;
  updateElementSortingDto: UpdateElementSortingDto;
}

export interface UpdateComponentNameMutation {
  componentUuid: string;
  updateComponentNameDto: UpdateComponentNameDto;
}

export function useSaveComponent(
  workspaceDto: WorkspaceDesignerDto
) {
  const queryClient = useQueryClient();
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceDto.uuid;

  return useMutation('useSaveComponent',
    (saveComponentMutation: SaveComponentMutation) => ComponentService.saveComponent(
      saveComponentMutation.viewUuid,
      saveComponentMutation.createComponentDto
    ),
    {
      onMutate: async (saveComponentMutation) => {

        await queryClient.cancelQueries(queryKey);

        const workspaceSnapshot = queryClient.getQueryData<WorkspaceDesignerDto>(queryKey);

        if (workspaceSnapshot === undefined) {
          return undefined;
        }

        const viewIndex = workspaceSnapshot.views.findIndex(view => view.uuid === saveComponentMutation.viewUuid);

        if (viewIndex === -1) {
          return undefined;
        }

        const properties = saveComponentMutation.createComponentDto.shapes[0].properties;
        if (properties.height < 0) {
          properties.height *= -1;
          saveComponentMutation.createComponentDto.positionY -= properties.height;
        }

        if (properties.width < 0) {
          properties.width *= -1;
          saveComponentMutation.createComponentDto.positionX -= properties.width;
        }

        workspaceSnapshot.views[viewIndex].components = [
          {
            uuid: generateUuid(),
            name: '...',
            positionX: saveComponentMutation.createComponentDto.positionX,
            positionY: saveComponentMutation.createComponentDto.positionY,
            sorting: -1,
            isBlueprintComponentInstance: false,
            elements: saveComponentMutation.createComponentDto.shapes.map((createShapeDto, index) => ({
              element: {
                uuid: generateUuid(),
                name: '...',
                isMutated: false,
                sorting: index,
                ...createShapeDto
              },
              type: ElementType.Shape
            })),
          },
          ...workspaceSnapshot.views[viewIndex].components
        ];

        queryClient.setQueryData<WorkspaceDesignerDto>(queryKey, {
          ...workspaceSnapshot,
          views: workspaceSnapshot.views.map((view, index) => ({
            ...view,
            components: index === viewIndex
              ? workspaceSnapshot.views[viewIndex].components
              : view.components
          }))
        });

        return workspaceSnapshot;
      },
      onError: (err, variables, context: WorkspaceDesignerDto) => {
        if (context === undefined) {
          return;
        }
        queryClient.setQueryData<WorkspaceDesignerDto>(queryKey, context);
      },
      onSettled: async (componentDto, error, variables, context) => {
        // refetch workspace
        await queryClient.invalidateQueries(queryKey);
        const workspace = queryClient.getQueryData<WorkspaceDesignerDto>(queryKey);

        const activeViewUuid = useStore.getState().activeDesignerViewUuid;
        const setSelectedComponentUuids = useStore.getState().setSelectedComponentUuids;
        const setActiveTool = useStore.getState().setActiveDesignerTool;

        if (workspace === undefined || activeViewUuid === null || activeViewUuid !== variables.viewUuid) {
          return;
        }

        // find active view
        const activeViewWithSavedComponent = workspace.views.find(view => view.uuid === activeViewUuid);

        if (activeViewWithSavedComponent === undefined) {
          return;
        }

        // select component
        setSelectedComponentUuids([componentDto.uuid]);
        setActiveTool(ToolType.POINTER);
      }
    }
  );
}

export function useScaleElements(
  selectedComponentsRef: React.MutableRefObject<Konva.Group>,
  scaleOrigin: ScaleOrigin,
  positionBeforeTransform: { x: number, y: number },
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const selectedElementUuids = useStore(state => state.selectedComponentUuids);
  const selectedElementsProperties = useStore(state => state.selectedElementsProperties);
  const refreshSelectedElementUuids = useStore.getState().refreshSelectedComponentUuids;
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (scale: { x: number, y: number }) => ComponentService.scaleElements({
      parentComponentUuid: focusedComponentUuid,
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
        await queryClient.cancelQueries(queryKey);
      },

      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKey);
        refreshSelectedElementUuids();
        selectedComponentsRef.current.position(positionBeforeTransform);
        selectedComponentsRef.current.scale({ x: 1, y: 1 });
      }
    }
  );
}

export function useTranslateElements(
  selectedComponentsRef: React.MutableRefObject<Konva.Group>,
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const selectedElementUuids = useStore(state => state.selectedComponentUuids);
  const refreshSelectedElementUuids = useStore(state => state.refreshSelectedComponentUuids);
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (translate: { x: number, y: number }) => ComponentService.translateElements({
      parentComponentUuid: focusedComponentUuid,
      elementUuids: selectedElementUuids,
      translateX: translate.x,
      translateY: translate.y
    }),
    {
      onMutate: async (translate) => await queryClient.cancelQueries(queryKey),
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKey);
        refreshSelectedElementUuids();
        selectedComponentsRef.current.position({ x: 0, y: 0 });
      }
    }
  );
}

export function useDeleteElements(workspaceUuid: string) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore(state => state.setConfiguratorIsLoading);
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const selectedElementUuids = useStore(state => state.selectedComponentUuids);
  const setSelectedElementUuids = useStore(state => state.setSelectedComponentUuids);
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(() => ComponentService.deleteElements({
      parentComponentUuid: focusedComponentUuid,
      elementUuids: selectedElementUuids
  }),
    {
      onMutate: () => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        setSelectedElementUuids([]);
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  );
}

export function usePositionElements(workspaceUuid: string) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore(state => state.setConfiguratorIsLoading);
  const refreshSelectedElementUuids = useStore(state => state.refreshSelectedComponentUuids);
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (positionElementsDto: PositionElementsDto) => ComponentService.positionElements(positionElementsDto),
    {
      onMutate: (positionElementsDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        refreshSelectedElementUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  );
}

export function useReshapeElements(
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore(state => state.setConfiguratorIsLoading);
  const refreshSelectedElementUuids = useStore(state => state.refreshSelectedComponentUuids);
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (reshapeElementsDto: ReshapeElementsDto) => ComponentService.reshapeElements(reshapeElementsDto),
    {
      onMutate: (reshapeElementsDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        refreshSelectedElementUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  );
}

export function useUpdateElementsProperties(
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore(state => state.setConfiguratorIsLoading);
  const refreshSelectedElementUuids = useStore(state => state.refreshSelectedComponentUuids);
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (updateElementsPropertiesDto: UpdateElementsPropertiesDto) =>
      ComponentService.updateElementsProperties(updateElementsPropertiesDto),
    {
      onMutate: (updateElementsPropertiesDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        refreshSelectedElementUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  );
}

export function useAlignElements(
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore(state => state.setConfiguratorIsLoading);
  const refreshSelectedElementUuids = useStore(state => state.refreshSelectedComponentUuids);
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (alignElementsDto: AlignElementsDto) => ComponentService.alignElements(alignElementsDto),
    {
      onMutate: (alignElementsDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        refreshSelectedElementUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  );
}

export function useUpdateElementSorting(
  workspaceUuid: string,
) {
  const queryClient = useQueryClient();
  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (updateElementSortingMutation: UpdateElementSortingMutation) =>
      ComponentService.updateElementSorting(updateElementSortingMutation.updateElementSortingDto),
    {
      onMutate: async (updateComponentSortingMutation) => {
        await queryClient.cancelQueries(queryKey);

        const workspaceSnapshot = queryClient.getQueryData<WorkspaceDesignerDto | undefined>(queryKey);

        if (workspaceSnapshot === undefined) {
          return;
        }

        if (activeViewUuid === null) {
          return;
        }

        queryClient.setQueryData(queryKey, {
          ...workspaceSnapshot,
          views: workspaceSnapshot.views.map(view => (view.uuid !== activeViewUuid)
            ? view
            : {
            ...view,
              components: (focusedComponentUuid === null)
                ? ComponentUtils.sortComponents<ComponentDto>(
                    view.components,
                    updateComponentSortingMutation.updateElementSortingDto.elementUuid,
                    updateComponentSortingMutation.oldSorting,
                    updateComponentSortingMutation.updateElementSortingDto.sorting
                )
                : ComponentUtils.traverseComponentTrees(
                  view.components,
                  (component) => (component.uuid !== focusedComponentUuid)
                    ? component
                    : {
                      ...component,
                      elements: ComponentUtils.sortComponentElements<ElementDto>(
                        component.elements,
                        updateComponentSortingMutation.updateElementSortingDto.elementUuid,
                        updateComponentSortingMutation.oldSorting,
                        updateComponentSortingMutation.updateElementSortingDto.sorting
                      )
                  }
                )
          })
        });
      },
      onSettled: (data, error, variables, context) => queryClient.invalidateQueries(queryKey)
    }
  );
}

export function useUpdateComponentName(
  queryClient: QueryClient,
  workspaceUuid: string,
  viewUuid: string
) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (updateComponentNameMutation: UpdateComponentNameMutation) => ComponentService.updateComponentName(
      updateComponentNameMutation.componentUuid, updateComponentNameMutation.updateComponentNameDto
    ),
    {
      onMutate: async (updateComponentNameMutation) => {
        await queryClient.cancelQueries(queryKey);
        const workspaceSnapshot = queryClient.getQueryData<WorkspaceDesignerDto | undefined>(queryKey);

        if (workspaceSnapshot === undefined) {
          return;
        }

        queryClient.setQueryData(queryKey, {
          ...workspaceSnapshot,
          views: workspaceSnapshot.views.map(view => (view.uuid !== viewUuid)
            ? view
            : {
              ...view,
              components: view.components.map(component => (component.uuid !== updateComponentNameMutation.componentUuid)
                ? component
                : { ...component, name: updateComponentNameMutation.updateComponentNameDto.name })
            })
        });
      },
      onSettled: (data, error, variables, context) => queryClient.invalidateQueries(queryKey)
    }
  );
}

export function useSaveComponentWithShape(workspaceUuid: string) {
  const queryClient = useQueryClient();
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  const setIsLoading = useStore(state => state.setNavigationIsLoading);
  const setSelectedElementUuids = useStore(state => state.setSelectedComponentUuids);

  return useMutation(
    (shapeUuid: string) => ComponentService.saveWithShape(shapeUuid),
    {
      onMutate: (shapeUuid) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (componentDto, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        clearTimeout(context);
        setIsLoading(false);
        if (!error) {
          setSelectedElementUuids([componentDto.uuid]);
        }
      }
    }
  )
}

export function useResetMutations(workspaceUuid: string) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore.getState().setConfiguratorIsLoading;
  const focusedComponentUuid = useStore.getState().focusedComponentUuid;
  const selectedElementUuids = useStore.getState().selectedComponentUuids;
  const refreshSelectedElementUuids = useStore.getState().refreshSelectedComponentUuids;
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    () => ComponentService.resetMutations({
      parentComponentUuid: focusedComponentUuid,
      elementUuids: selectedElementUuids
    }),
    {
      onMutate: () => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        refreshSelectedElementUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}
