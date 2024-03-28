import { QueryClient, useMutation, useQueryClient } from 'react-query';
import {
  AlignShapesDto,
  BlueprintComponentDto,
  ComponentDto,
  CreateShapeDto,
  DeleteShapesDto,
  ElementType,
  PositionShapesDto,
  ReshapeShapesDto,
  ScaleOrigin,
  ShapeIdentifierDto,
  UpdateShapeNameDto,
  UpdateShapesPropertiesDto,
  WorkspaceDesignerDto
} from '../../../../gen/api-client';
import { QueryKeys } from '../../../enums/query-keys.enum';
import { ShapeService } from '../services/shape.service';
import { v4 as generateUuid } from 'uuid';
import Konva from 'konva';
import React from 'react';
import { useStore } from '../../../store';
import { ToolType } from '../../../enums/tool-type.enum';
import { ComponentUtils } from '../component.utils';

export interface SaveShapeMutation {
  componentUuid: string;
  createShapeDto: CreateShapeDto
}

export interface UpdateShapeNameMutation {
  shapeUuid: string;
  updateShapeNameDto: UpdateShapeNameDto;
}

export function useSaveShape(
  workspaceDto: WorkspaceDesignerDto
) {
  const queryClient = useQueryClient();
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceDto.uuid;
  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const setSelectedElementUuids = useStore(state => state.setSelectedComponentUuids);
  const setActiveDesignerTool = useStore(state => state.setActiveDesignerTool);

  return useMutation('useSaveShapeMutation',
    (saveShapeMutation: SaveShapeMutation) => ShapeService.saveShape(
      saveShapeMutation.componentUuid,
      saveShapeMutation.createShapeDto
    ),
    {
      onMutate: async (saveShapeMutation) => {
        await queryClient.cancelQueries(queryKey);
        const workspaceSnapshot = queryClient.getQueryData<WorkspaceDesignerDto | undefined>(queryKey);

        if (workspaceSnapshot === undefined) {
          return;
        }

        const properties = saveShapeMutation.createShapeDto.properties;
        if (properties.height < 0) {
          properties.height *= -1;
          saveShapeMutation.createShapeDto.positionY -= properties.height;
        }

        if (properties.width < 0) {
          properties.width *= -1;
          saveShapeMutation.createShapeDto.positionX -= properties.width;
        }

        queryClient.setQueryData(queryKey, {
          ...workspaceSnapshot,
          views: workspaceSnapshot.views.map(view => (view.uuid !== activeViewUuid)
          ? view
          : {
            ...view,
              components: view.components.map(component => (component.uuid !== saveShapeMutation.componentUuid)
              ? component
              : {
                ...component,
                  elements: [
                    {
                      element: {
                        uuid: generateUuid(),
                        name: '...',
                        ...saveShapeMutation.createShapeDto
                      },
                      type: ElementType.Shape
                    },
                    ...component.elements
                  ]
                })
            })
        })
      },
      onSettled: async (shapeDto, error, variables, context) => {
        await queryClient.invalidateQueries(queryKey);
        setSelectedElementUuids([shapeDto.uuid]);
        setActiveDesignerTool(ToolType.POINTER);
      }
    })
}

export function useScaleShapes(
  queryClient: QueryClient,
  selectedShapesRef: React.MutableRefObject<Konva.Group>,
  selectedShapeIdentifiers: ShapeIdentifierDto[],
  scaleOrigin: ScaleOrigin,
  positionBeforeTransform: {x: number, y: number},
  workspaceUuid: string
) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (scale: {x: number, y: number}) => ShapeService.scaleShapes({
      shapeIdentifiers: selectedShapeIdentifiers,
      scaleOrigin,
      scaleX: scale.x,
      scaleY: scale.y
    }),
    {
      onMutate: async (scale) => await queryClient.cancelQueries(queryKey),
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKey);
        selectedShapesRef.current.position(positionBeforeTransform);
        selectedShapesRef.current.scale({x: 1, y: 1});
      }
    }
  )
}

export function useTranslateShapes(
  queryClient: QueryClient,
  selectedShapesRef: React.MutableRefObject<Konva.Group>,
  selectedShapeIdentifiers: ShapeIdentifierDto[],
  workspaceUuid: string
) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;

  return useMutation(
    (translate: {x: number, y: number}) => ShapeService.translateShapes({
      shapeIdentifiers: selectedShapeIdentifiers,
      translateX: translate.x,
      translateY: translate.y
    }),
    {
      onMutate: async (translate) => await queryClient.cancelQueries(queryKey),
      onSettled: async (data, error, variables, context) => {
        await queryClient.invalidateQueries(queryKey);
        selectedShapesRef.current.position({x: 0, y: 0});
      }
    }
  )
}

export function usePositionShapes(
  queryClient: QueryClient,
  refreshSelectedShapeUuids: () => void,
  setIsLoading: (isLoading: boolean) => void,
  workspaceUuid: string
) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  return useMutation(
    (positionShapesDto: PositionShapesDto) => ShapeService.positionShapes(positionShapesDto),
    {
      onMutate: (positionShapesDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        refreshSelectedShapeUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}

export function useReshapeShapes(
  queryClient: QueryClient,
  refreshSelectedShapeUuids: () => void,
  setIsLoading: (isLoading: boolean) => void,
  workspaceUuid: string
) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  return useMutation(
    (reshapeShapesDto: ReshapeShapesDto) => ShapeService.reshapeShapes(reshapeShapesDto),
    {
      onMutate: (reshapeShapesDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        refreshSelectedShapeUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}

export function useAlignShapes(
  queryClient: QueryClient,
  refreshSelectedShapeUuids: () => void,
  setIsLoading: (isLoading: boolean) => void,
  workspaceUuid: string
) {
  const queryKey = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  return useMutation(
    (alignShapesDto: AlignShapesDto) => ShapeService.alignShapes(alignShapesDto),
    {
      onMutate: (alignShapesDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKey);
        refreshSelectedShapeUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}

export function useUpdateShapesProperties(
  workspaceUuid: string
) {
  const queryClient = useQueryClient();
  const setIsLoading = useStore(state => state.setConfiguratorIsLoading);
  const stageBlueprintComponentProperties = useStore.getState().designerStageBlueprintComponentProperties;
  const queryKeyWorkspace = QueryKeys.DESIGNER_WORKSPACE + workspaceUuid;
  const queryKeyBlueprintComponents = QueryKeys.BLUEPRINT_COMPONENTS + workspaceUuid;
  const refreshSelectedElementUuids = useStore(state => state.refreshSelectedComponentUuids);
  return useMutation(
    (updateShapesPropertiesDto: UpdateShapesPropertiesDto) =>
      ShapeService.updateShapesProperties(updateShapesPropertiesDto),
    {
      onMutate: (updateShapesPropertiesDto) => setTimeout(() => setIsLoading(true), 500),
      onSettled: async (data, error, variables, context: NodeJS.Timeout) => {
        await queryClient.invalidateQueries(queryKeyWorkspace);

        if (stageBlueprintComponentProperties.active) {
          await queryClient.invalidateQueries(queryKeyBlueprintComponents);
        }

        refreshSelectedElementUuids();
        clearTimeout(context);
        setIsLoading(false);
      }
    }
  )
}

export function useUpdateShapeName(
  queryClient: QueryClient,
  workspaceUuid: string,
  viewUuid: string,
) {
  const designerStageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);
  const queryKey = ((!designerStageBlueprintComponentProperties.active)
    ? QueryKeys.DESIGNER_WORKSPACE
    : QueryKeys.BLUEPRINT_COMPONENTS) + workspaceUuid;

  return useMutation(
    (updateShapeNameMutation: UpdateShapeNameMutation) =>
      ShapeService.updateShapeName(updateShapeNameMutation.shapeUuid, updateShapeNameMutation.updateShapeNameDto),
    {
      onMutate: async (updateShapeNameMutation) => {
        await queryClient.cancelQueries(queryKey);

        const replaceShapeOperation = (shape) => (shape.uuid !== updateShapeNameMutation.shapeUuid)
          ? shape
          : {...shape, name: updateShapeNameMutation.updateShapeNameDto.name};

        if (designerStageBlueprintComponentProperties.active) {
          const workspaceSnapshot = queryClient.getQueryData<BlueprintComponentDto[] | undefined>(queryKey);

          if (workspaceSnapshot === undefined) {
            return;
          }

          queryClient.setQueryData<BlueprintComponentDto[]>(
            queryKey,
            ComponentUtils.replaceShapesInComponentTrees<BlueprintComponentDto>(
              workspaceSnapshot,
              replaceShapeOperation
            )
          )
          return;
        }

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
                components: ComponentUtils.replaceShapesInComponentTrees<ComponentDto>(
                  view.components,
                  replaceShapeOperation
                )
              })
        });
      },
      onSettled: (data, error, variables, context) => queryClient.invalidateQueries(queryKey)
    }
  )
}
