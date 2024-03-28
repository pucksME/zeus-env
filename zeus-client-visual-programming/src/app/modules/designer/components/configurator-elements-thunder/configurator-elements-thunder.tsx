import React from 'react';

import './configurator-elements-thunder.module.scss';
import { ComponentTreeNodeElement } from '../../component.utils';
import { ComponentDto } from '../../../../../gen/api-client';
import { Button } from '@material-ui/core';
import ArrowForwardIcon from '@material-ui/icons/ArrowForward';
import { Link, useRouteMatch } from 'react-router-dom';
export interface ConfiguratorElementsThunderProps {
  element: ComponentTreeNodeElement<ComponentDto>;
}

export function ConfiguratorElementsThunder(
  props: ConfiguratorElementsThunderProps
) {
  const designerMatch = useRouteMatch('/project/:projectUuid');

  return (
    <Link
      to={`/project/${designerMatch.params['projectUuid']}/component/${props.element.element.uuid}`}
      style={{textDecoration: 'none', width: '100%'}}
    >
      <Button
        color={'secondary'}
        endIcon={<ArrowForwardIcon/>}
        style={{width: '100%'}}
        variant={'contained'}
      >
        Open Thunder Editor
      </Button>
    </Link>
  );
}

export default ConfiguratorElementsThunder;
