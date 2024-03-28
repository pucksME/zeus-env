import React from 'react';

import './toolbox-visualizer-system.module.scss';
import { useSystemCodeModules } from '../../data/code-module-data.hooks';
import ToolboxList from '../../../../components/toolbox-list/toolbox-list';
import CodeModuleCard from '../code-module-card/code-module-card';
import spacing from '../../../../../assets/styling/spacing.json';
import { Typography } from '@material-ui/core';
import { CodeModuleDto } from '../../../../../gen/api-client';
import { QueryKeys } from '../../../../enums/query-keys.enum';
import { QueryKeysCodeModules } from '../../data/code-module-instance-data.hooks';

export interface ToolboxVisualizerSystemProps {
  onSelectCodeModule: (codeModuleDto: CodeModuleDto, queryKeyCodeModules: QueryKeysCodeModules) => void;
}

export function ToolboxVisualizerSystem(props: ToolboxVisualizerSystemProps) {

  const {isLoading, isError, codeModuleDtos, error} = useSystemCodeModules();

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (isError) {
    return <div>{error['message']}</div>
  }

  return (
    <ToolboxList title={'Code Modules'}>
      {
        (codeModuleDtos.length !== 0)
          ? codeModuleDtos.map((codeModuleDto, index) => <CodeModuleCard
            key={index}
            codeModuleDto={codeModuleDto}
            style={{ marginBottom: (index !== codeModuleDtos.length - 1) ? 10 : 0}}
            onMouseDown={() => props.onSelectCodeModule(codeModuleDto, QueryKeys.SYSTEM_CODE_MODULES)}
          />)
          : <div style={{ paddingLeft: spacing.toolbox.padding, paddingRight: spacing.toolbox.padding }}>
            <Typography variant={'body2'} color={'textSecondary'}>This project has no code modules</Typography>
          </div>
      }
    </ToolboxList>
  );
}

export default ToolboxVisualizerSystem;
