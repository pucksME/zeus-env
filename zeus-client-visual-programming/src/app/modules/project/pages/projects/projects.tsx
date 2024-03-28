import React, { CSSProperties, useEffect, useState } from 'react';

import './projects.module.scss';
import { useProjects } from '../../data/project.hooks';
import colors from '../../../../../assets/styling/colors.json';
import { Button, makeStyles, Popover, TextField, Typography } from '@material-ui/core';
import AddIcon from '@material-ui/icons/Add';
import DashboardCustomizeIcon from '@material-ui/icons/DashboardCustomize';
import ProjectGalleryItem from '../../components/project-gallery-item/project-gallery-item';
import { useMutation, useQueryClient } from 'react-query';
import { ProjectService } from '../../services/project.service';
import { CreateProjectDto, ProjectGalleryDto, WorkspaceType } from '../../../../../gen/api-client';
import { QueryKeys } from '../../../../enums/query-keys.enum';
import { v4 as generateUuid } from 'uuid';
import { useStore } from '../../../../store';
import Input from '../../../../components/input/input';

/* eslint-disable-next-line */
export interface ProjectsProps {
}

const useStyles = makeStyles({
  paper: {
    borderRadius: 15,
    width: 300
  }
});

export function Projects(props: ProjectsProps) {

  // resetting the designer's state when visiting this page from another project
  const resetDesignerState = useStore(state => state.resetDesignerState);
  useEffect(() => resetDesignerState(), []);

  const classes = useStyles();
  const [anchorElementCreateProject, setAnchorElementCreateProject] = useState<HTMLButtonElement | null>(null);
  const [createProjectData, setCreateProjectData] = useState<{ name: string, description: string }>({
    name: '',
    description: ''
  });
  const { isLoading, isError, projectGalleryDtos, error } = useProjects();

  const queryClient = useQueryClient();
  const queryKey = QueryKeys.PROJECTS;
  const saveProject = useMutation(
    (createProjectDto: CreateProjectDto) => ProjectService.saveProject(createProjectDto),
    {
      onMutate: async (createProjectDto) => {
        await queryClient.cancelQueries(queryKey);
        const projectsSnapshot = queryClient.getQueryData<ProjectGalleryDto[]>(queryKey);

        if (projectsSnapshot === undefined) {
          return;
        }

        queryClient.setQueryData(queryKey, [...projectsSnapshot, {
          uuid: generateUuid(),
          name: createProjectDto.name,
          description: createProjectDto.description,
          type: createProjectDto.type,
          view: null
        }]);
      },
      onSettled: (data, error, variables, context) =>
        queryClient.invalidateQueries(queryKey)
    }
  );

  if (isLoading) {
    return <div>Loading</div>;
  }

  if (isError) {
    return <div>{error}</div>;
  }

  const handleCreateProjectButtonClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorElementCreateProject(event.currentTarget);
  };

  const handleCreateProjectPopoverClose = () => setAnchorElementCreateProject(null);

  const handleSaveProjectButtonClick = () => {
    saveProject.mutate({
      name: createProjectData.name,
      description: createProjectData.description !== '' ? createProjectData.description : null,
      type: WorkspaceType.Desktop
    });

    setCreateProjectData({name: '', description: ''});
    handleCreateProjectPopoverClose();
  };

  const projectCountInLastRow = projectGalleryDtos.length % 3 === 0 ? 3 : projectGalleryDtos.length % 3;

  const chunkProjects = (projectGalleryDtos: ProjectGalleryDto[]): ProjectGalleryDto[][] => {
    const chunks: ProjectGalleryDto[][] = [];
    for (let i = 0; i < projectGalleryDtos.length; i += 3) {
      chunks.push(projectGalleryDtos.slice(i, i + 3));
    }
    return chunks;
  };

  const buildProjectRow = (projectGalleryDtos: ProjectGalleryDto[], key: number, style: CSSProperties): JSX.Element =>
    <div key={key}>{projectGalleryDtos.map((project, index) =>
      <ProjectGalleryItem
        key={project.uuid}
        projectGalleryDto={project}
        style={{
          display: 'inline-block',
          marginRight: (index < (projectGalleryDtos.length - 1)) ? 50 : 0,
          ...style
        }}
      />
    )}</div>;

  const chunkedProjects = chunkProjects(projectGalleryDtos);
  return (
    <div style={{
      marginLeft: 'auto',
      marginRight: 'auto',
      width: (3 * (300 + (2 * 15))) + (2 * 50) + (2 * 50),
      paddingTop: 100,
      paddingBottom: 100
    }}>
      <div style={{ textAlign: 'right' }}>
        <Button onClick={handleCreateProjectButtonClick} startIcon={<AddIcon />}>New Project</Button>
        <Popover
          classes={{ paper: classes.paper }}
          anchorEl={anchorElementCreateProject}
          anchorOrigin={{ horizontal: 'left', vertical: 'bottom' }}
          onClose={handleCreateProjectPopoverClose}
          open={anchorElementCreateProject !== null}
        >
          <div style={{ padding: 25, paddingBottom: 20 }}>
            <Input
              placeholder={'Name'}
              style={{width: '100%'}}
              value={createProjectData.name}
              onChange={(event) => setCreateProjectData({...createProjectData, name: event.value as string})}
            />
            <Input
              placeholder={'Description'}
              style={{marginTop: 10, width: '100%'}}
              value={createProjectData.description}
              onChange={(event) => setCreateProjectData({...createProjectData, description: event.value as string})}
            />
            <div style={{ marginTop: 10, textAlign: 'right' }}>
              <Button onClick={handleCreateProjectPopoverClose}>Cancel</Button>
              <Button
                disabled={createProjectData.name.length < 3 || // TODO: also implement validation for API
                createProjectData.name.length > 25 ||
                createProjectData.description.length > 200
                }
                onClick={handleSaveProjectButtonClick}
              >
                Create Project
              </Button>
            </div>
          </div>
        </Popover>
      </div>
      <div style={{
        backgroundColor: colors.background_light,
        borderRadius: 15,
        boxSizing: 'border-box',
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'center',
        marginTop: 10,
        padding: 50
      }}>
        <div style={{width: '100%'}}>
          {
            projectGalleryDtos.length !== 0
              ? chunkedProjects.map((project, index) =>
                buildProjectRow(
                  project,
                  index,
                  { marginBottom: (index < (chunkedProjects.length - 1)) ? 50 : 0 }
                ))
              : <div
                style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 400, width: '100%' }}>
                <div style={{ textAlign: 'center' }}>
                  <DashboardCustomizeIcon style={{ color: colors.text.secondary, fontSize: 65 }} />
                  <Typography color={'textSecondary'} style={{ marginTop: 5 }} variant={'h5'}>There are no projects to
                    show.</Typography>
                </div>
              </div>
          }
        </div>


      </div>
    </div>
  );
}

export default Projects;
