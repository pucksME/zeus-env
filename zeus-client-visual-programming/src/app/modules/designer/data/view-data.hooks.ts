import {QueryClient, useMutation, useQueryClient} from 'react-query';
import {
  CreateViewDto, PositionViewDto, ReshapeViewDto,
  ScaleOrigin,
  ScaleViewDto,
  UpdateViewNameDto,
  ViewDto, WorkspaceDesignerDto
} from '../../../../gen/api-client';
import { QueryKeys } from '../../../enums/query-keys.enum';
import { ViewService } from '../services/view.service';
import React from 'react';
import Konva from 'konva';

export interface UpdateViewNameMutation {
  viewUuid: string;
  updateViewNameDto: UpdateViewNameDto;
}

export interface ScaleViewMutation {
  viewUuid: string;
  scaleViewDto: ScaleViewDto;
}

export function useSaveView(
  queryClient: QueryClient,
  workspaceUuid: string,
  setSaveViewIsLoading: (saveViewIsLoading: boolean) => void,
  endSaveViewIsLoading: () => void,
  selectView: (viewDto: ViewDto) => void
) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  return useMutation(
    (createView: CreateViewDto) => ViewService.saveView(workspaceUuid, createView),
    {
      onMutate: (createView: CreateViewDto) => {
        setSaveViewIsLoading(true);
      },
      onSettled: async (viewDto, error, variables, context) => {

        endSaveViewIsLoading();

        // get workspace without adding the new view
        let workspace = queryClient.getQueryData<WorkspaceDesignerDto>(queryKey);

        if (workspace === undefined) {
          return;
        }

        const views = [...workspace.views];

        // refetch workspace
        await queryClient.invalidateQueries(queryKey, { refetchActive: true });

        // get workspace with new view
        workspace = queryClient.getQueryData<WorkspaceDesignerDto>(queryKey);

        if (workspace === undefined) {
          return;
        }

        const fetchedViews = [...workspace.views];

        // find newly added view
        const savedView = fetchedViews.find(
          fetchedView => views.find(view => view.uuid === fetchedView.uuid) === undefined
        );

        if (savedView === undefined) {
          return;
        }

        // set newly added view as active view
        selectView(savedView);

      }
    }
  );
}

export function useUpdateViewName(queryClient: QueryClient, workspaceUuid: string) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  return useMutation(
    (updateViewNameMutation: UpdateViewNameMutation) => ViewService.updateViewName(
      updateViewNameMutation.viewUuid, updateViewNameMutation.updateViewNameDto
    ),
    {
      onMutate: async (updateViewNameMutation) => {
        await queryClient.cancelQueries(queryKey);
        const workspaceSnapshot = queryClient.getQueryData<WorkspaceDesignerDto | undefined>(queryKey);

        if (workspaceSnapshot === undefined) {
          return;
        }

        queryClient.setQueryData(queryKey, {
          ...workspaceSnapshot,
          views: workspaceSnapshot.views.map(view => (view.uuid !== updateViewNameMutation.viewUuid)
            ? view
            : {
              ...view,
              name: updateViewNameMutation.updateViewNameDto.name
            })
        });
      }
    }
  );
}

export function useSetRootView(workspaceUuid: string) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  const queryClient = useQueryClient();
  return useMutation(
    (viewUuid: string) => ViewService.setRootView(viewUuid),
    {
      onMutate: async (viewUuid: string) => {
        await queryClient.cancelQueries();
        const workspaceSnapshot = queryClient.getQueryData<WorkspaceDesignerDto | undefined>(queryKey);

        if (workspaceSnapshot === undefined) {
          return;
        }

        queryClient.setQueryData(queryKey, {
          ...workspaceSnapshot,
          views: workspaceSnapshot.views.map(view => ({...view, isRoot: view.uuid === viewUuid}))
        });
      },
      onSettled: (data, error, variables, context) => queryClient.invalidateQueries(queryKey)
    }
  )
}

export function useScaleView(
  queryClient: QueryClient,
  selectedViewRef: React.MutableRefObject<Konva.Group>,
  viewUuid: string,
  scaleOrigin: ScaleOrigin,
  positionBeforeTransform: {x: number, y: number},
  workspaceUuid: string
) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  return useMutation(
    (scale: {x: number, y: number}) => ViewService.scaleView(viewUuid, {
      scaleX: scale.x,
      scaleY: scale.y,
      scaleOrigin
    }),
    {
      onMutate: async (scale) => await queryClient.cancelQueries(),
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKey);
        selectedViewRef.current.position(positionBeforeTransform);
        selectedViewRef.current.scale({x: 1, y: 1});
      }
    }
  )
}

export function useTranslateView(
  queryClient: QueryClient,
  selectedViewRef: React.MutableRefObject<Konva.Group>,
  viewUuid: string,
  workspaceUuid: string
) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  return useMutation(
    (translate: {x: number, y: number}) => ViewService.translateView(viewUuid, {translateX: translate.x, translateY: translate.y}),
    {
      onMutate: async (translate: {x: number, y: number}) => await queryClient.cancelQueries(queryKey),
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKey);
        selectedViewRef.current.position({x: 0, y: 0});
      }
    }
  )
}

export function useReshapeView(
  queryClient: QueryClient,
  setIsLoading: (isLoading: boolean) => void,
  viewUuid: string,
  workspaceUuid: string
) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  return useMutation(
    (reshapeViewDto: ReshapeViewDto) => ViewService.reshapeView(viewUuid, reshapeViewDto),
    {
      onMutate: (reshapeViewDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}

export function usePositionView(
  queryClient: QueryClient,
  setIsLoading: (isLoading: boolean) => void,
  viewUuid: string,
  workspaceUuid: string
) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  return useMutation(
    (positionViewDto: PositionViewDto) => ViewService.positionView(viewUuid, positionViewDto),
    {
      onMutate: (positionViewDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}

export function useDeleteView(
  queryClient: QueryClient,
  resetActiveView: () => void,
  workspaceUuid: string
) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  return useMutation(
    (viewUuid: string) => ViewService.deleteView(viewUuid),
    {
      onMutate: async (viewUuid) => {
        await queryClient.cancelQueries(queryKey);

        const workspaceSnapshot = queryClient.getQueryData<WorkspaceDesignerDto>(queryKey);
        if (workspaceSnapshot === undefined) {
          return;
        }
        queryClient.setQueryData(queryKey, {
          ...workspaceSnapshot,
          views: workspaceSnapshot.views.filter(view => view.uuid !== viewUuid)
        });
      },
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKey);
        resetActiveView();
      }
    }
  )
}
