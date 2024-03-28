import React, { CSSProperties } from 'react';

import './code-module-card.module.scss';
import { CodeModuleDto } from '../../../../../gen/api-client';
import { Box, IconButton, Typography } from '@material-ui/core';
import spacing from '../../../../../assets/styling/spacing.json';
import { CodeModuleUtils } from '../../code-module.utils';
import OpenInNewIcon from '@material-ui/icons/OpenInNew';
import { useStore } from '../../../../store';
import zIndices from '../../../../../assets/styling/z-indices.json';
import WarningIcon from '@material-ui/icons/Warning';
import CodeModuleDetail from '../code-module-detail/code-module-detail';
import EastIcon from '@material-ui/icons/East';
import WestIcon from '@material-ui/icons/West';
import SettingsIcon from '@material-ui/icons/Settings';
import CodeModuleDetails from '../code-module-details/code-module-details';

export interface CodeModuleCardProps {
  codeModuleDto: CodeModuleDto;
  onMouseDown?: () => void;
  style?: CSSProperties;
}

export function CodeModuleCard(props: CodeModuleCardProps) {
  const setThunderCodeEditorProperties = useStore(state => state.setThunderCodeEditorProperties);

  const handleEditCodeModuleMouseDown = (event: React.MouseEvent<HTMLButtonElement>) => event.stopPropagation();

  const handleEditCodeModuleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setThunderCodeEditorProperties({
      codeModuleUuid: props.codeModuleDto.uuid,
      initialCode: props.codeModuleDto.code,
      active: true
    })
  }

  const buildActions = () => (
    <div style={{zIndex: zIndices.codeModule.editIcon}}>
      <IconButton
        size={'small'}
        onMouseDown={handleEditCodeModuleMouseDown}
        onClick={handleEditCodeModuleClick}
      >
        <OpenInNewIcon fontSize={'small'}/>
      </IconButton>
    </div>
  );

  return (
    <Box
      style={{
        backgroundColor: '#ffffff',
        borderRadius: 15,
        marginLeft: spacing.toolbox.padding,
        marginRight: spacing.toolbox.padding,
        padding: 10,
        ...props.style
    }}
      onMouseDown={props.onMouseDown}
      boxShadow={1}
    >
      <div style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between'}}>
        <Typography variant={'body2'}>
          {props.codeModuleDto.name}
        </Typography>
        {buildActions()}
      </div>
      <div>
        <Typography variant={'caption'} color={'textSecondary'}>
          {CodeModuleUtils.buildCodeModuleDescription(props.codeModuleDto)}
        </Typography>
      </div>
      <CodeModuleDetails codeModuleDto={props.codeModuleDto} textVariant={'caption'}/>
    </Box>
  );
}

export default CodeModuleCard;
