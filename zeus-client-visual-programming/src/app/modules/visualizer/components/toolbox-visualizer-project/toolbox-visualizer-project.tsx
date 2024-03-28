import React from 'react';

import './toolbox-visualizer-project.module.scss';
import { useProjectCodeModules, useSaveCodeModule } from '../../data/code-module-data.hooks';
import ToolboxList from '../../../../components/toolbox-list/toolbox-list';
import CodeModuleCard from '../code-module-card/code-module-card';
import { IconButton, Typography } from '@material-ui/core';
import spacing from '../../../../../assets/styling/spacing.json';
import { CodeModuleDto } from '../../../../../gen/api-client';
import { QueryKeys } from '../../../../enums/query-keys.enum';
import { QueryKeysCodeModules } from '../../data/code-module-instance-data.hooks';
import AddIcon from '@material-ui/icons/Add';

export interface ToolboxVisualizerProjectProps {
  projectUuid: string;
  onSelectCodeModule: (codeModuleDto: CodeModuleDto, queryKeyCodeModules: QueryKeysCodeModules) => void;
}

export function ToolboxVisualizerProject(props: ToolboxVisualizerProjectProps) {
  const {isLoading, isError, codeModuleDtos, error} = useProjectCodeModules(props.projectUuid);
  const saveCodeModule = useSaveCodeModule(props.projectUuid);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (isError) {
    return <div>{error['message']}</div>
  }

  const buildToolboxListActions = () => (
    <div>
      <IconButton
        onClick={() => saveCodeModule.mutate()}
        size={'small'}
      >
        <AddIcon fontSize={'small'}/>
      </IconButton>
    </div>
  )

  return (
    <ToolboxList title={'Code Modules'} actions={buildToolboxListActions()}>
      {
        (codeModuleDtos.length !== 0)
          ? codeModuleDtos.map((codeModuleDto, index) => <CodeModuleCard
            key={index}
            codeModuleDto={codeModuleDto}
            style={{ marginBottom: (index !== codeModuleDtos.length - 1) ? 10 : 0}}
            onMouseDown={() => props.onSelectCodeModule(codeModuleDto, QueryKeys.PROJECT_CODE_MODULES)}
          />)
          : <div style={{ paddingLeft: spacing.toolbox.padding, paddingRight: spacing.toolbox.padding }}>
            <Typography variant={'body2'} color={'textSecondary'}>This project has no code modules</Typography>
          </div>
      }
    </ToolboxList>
  );
}

export default ToolboxVisualizerProject;
