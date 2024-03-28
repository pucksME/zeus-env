import React from 'react';

import './project.module.scss';
import Designer from '../../../designer/pages/designer/designer';
import { useParams } from 'react-router-dom';
import { useProject } from '../../data/project.hooks';
import Visualizer from '../../../visualizer/pages/visualizer/visualizer';

/* eslint-disable-next-line */
export interface ProjectProps {
}

export function Project(props: ProjectProps) {

  const params = useParams();
  const projectUuid = params['projectUuid'];
  const componentUuid = params['componentUuid'];
  const { isLoading, isError, error, projectDto } = useProject(projectUuid);

  if (isLoading) {
    return <div>Loading</div>;
  }

  if (isError) {
    return <div>{error}</div>;
  }

  return <div
    className={'height-100-percent'}
    style={{position: 'relative', overflow: 'hidden'}}
  >
    {(componentUuid === undefined)
      ? <Designer workspaceUuid={projectDto.workspaceUuid} />
      : <Visualizer projectUuid={projectUuid} componentUuid={componentUuid}/>}
  </div>


}

export default Project;
