import React, { CSSProperties, useEffect, useRef, useState } from 'react';

import './project-gallery-item.module.scss';
import { Layer, Stage } from 'react-konva';
import { Box, Button, IconButton, Typography } from '@material-ui/core';
import { DesignerStageUtils } from '../../../designer/designer-stage.utils';
import { ProjectGalleryDto, UpdateProjectDto } from '../../../../../gen/api-client';
import { Link } from 'react-router-dom';
import ArrowBackIcon from '@material-ui/icons/ArrowBack';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import SaveIcon from '@material-ui/icons/Save';
import EmptyArtboardIcon from '@material-ui/icons/DesignServices';
import { useMutation, useQueryClient } from 'react-query';
import { QueryKeys } from '../../../../enums/query-keys.enum';
import { ProjectService } from '../../services/project.service';
import Input from '../../../../components/input/input';

export interface ProjectGalleryItemProps {
  projectGalleryDto: ProjectGalleryDto;
  style?: CSSProperties;
}

interface UpdateProjectMutation {
  projectUuid: string;
  updateProjectDto: UpdateProjectDto;
}

export function ProjectGalleryItem(props: ProjectGalleryItemProps) {

  const previewHeight = 400;
  const previewWidth = 300;
  const scale = props.projectGalleryDto.view !== null ? previewWidth / props.projectGalleryDto.view.width : 1;

  // TODO: display information if view is null (project newly added) or if there are no components
  const componentGroups = props.projectGalleryDto.view !== null
    ? DesignerStageUtils.buildComponentTrees(
      props.projectGalleryDto.view.components,
      scale,
      props.projectGalleryDto.view
    ).components
    : [];

  const emptyWorkspaceMessageHeight = 150;
  const [emptyWorkspaceMessageMarginTop, setEmptyWorkspaceMessageMarginTop] = useState(0);
  const [isInSettingsMode, setIsInSettingsMode] = useState(false);
  const [updateProjectData, setUpdateProjectData] = useState<UpdateProjectDto>({
    name: props.projectGalleryDto.name,
    description: props.projectGalleryDto.description
  });

  const detailsBoxRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (detailsBoxRef.current === null) {
      return;
    }

    setEmptyWorkspaceMessageMarginTop(
      (((previewHeight + (2 * 15)) - detailsBoxRef.current.offsetHeight) - emptyWorkspaceMessageHeight) / 2
    );
  }, [detailsBoxRef]);

  const queryClient = useQueryClient();
  const queryKey = QueryKeys.PROJECTS;
  const deleteProject = useMutation(
    (projectUuid: string) => ProjectService.deleteProject(projectUuid),
    {
      onMutate: async (projectUuid) => {
        await queryClient.cancelQueries(queryKey);

        const projectsSnapshot = queryClient.getQueryData<ProjectGalleryDto[]>(queryKey);

        if (projectsSnapshot === undefined) {
          return;
        }

        queryClient.setQueryData(queryKey, projectsSnapshot.filter(project => project.uuid !== projectUuid));
      },
      onSettled: (data, error, variables, context) =>
        queryClient.invalidateQueries(queryKey)
    }
  );

  const updateProject = useMutation(
    (updateProjectMutation: UpdateProjectMutation) => ProjectService.updateProject(updateProjectMutation.projectUuid, updateProjectMutation.updateProjectDto),
    {
      onMutate: async (updateProjectMutation) => {
        await queryClient.cancelQueries(queryKey);

        const projectsSnapshot = queryClient.getQueryData<ProjectGalleryDto[]>(queryKey);

        if (projectsSnapshot === undefined) {
          return;
        }

        queryClient.setQueryData(queryKey, projectsSnapshot.map(project => {
          if (project.uuid !== updateProjectMutation.projectUuid) {
            return project;
          }

          return {
            ...project,
            name: updateProjectMutation.updateProjectDto.name,
            description: updateProjectMutation.updateProjectDto.description
          };
        }));

        setIsInSettingsMode(false);
      },
      onSettled: (data, error, variables, context) =>
        queryClient.invalidateQueries(queryKey)
    }
  );

  const handleDeleteProjectButtonClick = () => deleteProject.mutate(props.projectGalleryDto.uuid);
  const handleUpdateProjectButtonClick = () => updateProject.mutate({
    projectUuid: props.projectGalleryDto.uuid,
    updateProjectDto: {
      name: updateProjectData.name,
      description: updateProjectData.description !== '' ? updateProjectData.description : null
    }
  });
  return (
    <Box
      style={{
        backgroundColor: '#ffffff',
        borderRadius: 15,
        overflow: 'hidden',
        padding: 15,
        position: 'relative',
        width: previewWidth,
        zIndex: 0,
        ...props.style
      }}
      boxShadow={1}>
      <Stage height={previewHeight} width={previewWidth}>
        <Layer scaleX={scale} scaleY={scale}>
          {componentGroups}
        </Layer>
      </Stage>
      {componentGroups.length === 0
        ? <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          height: emptyWorkspaceMessageHeight,
          marginTop: emptyWorkspaceMessageMarginTop,
          width: '100%',
          position: 'absolute',
          left: 0,
          top: 0,
          zIndex: 1
        }}>
          <div style={{
            textAlign: 'center'
          }}>
            <EmptyArtboardIcon color={'secondary'} style={{ fontSize: 40 }} />
            <Typography
              color={'textSecondary'}
              style={{ marginTop: 5 }}
              variant={'body1'}>
              This project's main<br />view is empty.
            </Typography>
          </div>
        </div>
        : null}

      <div
        ref={detailsBoxRef}
        style={{
          position: 'absolute',
          bottom: 15,
          left: 15,
          zIndex: 2
        }}
      >
        <Box
          style={{
            backgroundColor: '#ffffff',
            borderRadius: 15,
            boxSizing: 'border-box',
            padding: 25,
            paddingBottom: 20,
            width: previewWidth
          }}
          boxShadow={1}
        >
          {!isInSettingsMode
            ? <div>
              <Typography color={'textPrimary'} variant={'subtitle1'}>{props.projectGalleryDto.name}</Typography>
              <Typography color={'textSecondary'} variant={'body2'}>
                {props.projectGalleryDto.description !== null
                  ? props.projectGalleryDto.description
                  : 'This project does not have a description.'}
              </Typography>
              <div style={{ marginTop: 5 }}>
                <Button onClick={() => setIsInSettingsMode(true)} size={'small'}>Settings</Button>
                <Link style={{ textDecoration: 'none' }} to={'project/' + props.projectGalleryDto.uuid}>
                  <Button size={'small'}>Open</Button>
                </Link>
              </div>
            </div>
            : <div>
              <div style={{textAlign: 'right'}}>
                <IconButton onClick={() => setIsInSettingsMode(false)}>
                  <ArrowBackIcon />
                </IconButton>
              </div>
              <Typography color={'textSecondary'} variant={'body2'}>Name</Typography>
              <Input
                style={{width: '100%', marginBottom: 5, marginTop: 2}}
                placeholder={'Name'}
                value={updateProjectData.name}
                onChange={(event) => setUpdateProjectData({...updateProjectData, name: event.value as string})}
              />

              <Typography color={'textSecondary'} variant={'body2'}>Description</Typography>
              <Input
                style={{width: '100%', marginTop: 2}}
                placeholder={'Description'}
                value={updateProjectData.description}
                onChange={(event) => setUpdateProjectData({...updateProjectData, description: event.value as string})}
              />
              <div style={{ marginTop: 10, textAlign: 'right' }}>
                <Button
                  onClick={handleDeleteProjectButtonClick}
                  startIcon={<DeleteForeverIcon />}
                >
                  Delete
                </Button>

                <Button
                  disabled={updateProjectData.name === props.projectGalleryDto.name &&
                  updateProjectData.description === props.projectGalleryDto.description}
                  onClick={handleUpdateProjectButtonClick}
                  startIcon={<SaveIcon />}
                >
                  Save
                </Button>
              </div>
            </div>}
        </Box>
      </div>


    </Box>
  );
}

export default ProjectGalleryItem;
