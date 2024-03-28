import React from 'react';

import './configurator-code-module-instance-connection.module.scss';
import { CodeModuleInstanceConnectionDto } from '../../../../../gen/api-client';
import { IconButton, Typography } from '@material-ui/core';
import EastIcon from '@material-ui/icons/East';
import CloseIcon from '@material-ui/icons/Close';
import { useDeleteConnection } from '../../data/code-module-instance-data.hooks';

export interface ConfiguratorCodeModuleInstanceConnectionProps {
  componentUuid: string;
  codeModuleInstanceConnectionDto: CodeModuleInstanceConnectionDto;
}

export function ConfiguratorCodeModuleInstanceConnection(
  props: ConfiguratorCodeModuleInstanceConnectionProps
) {
  const deleteConnection = useDeleteConnection(props.componentUuid);
  const handleClick = () => deleteConnection.mutate(props.codeModuleInstanceConnectionDto.uuid);

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      paddingLeft: 5,
      paddingRight: 5
    }}>
      <div style={{display: 'flex', alignItems: 'center'}}>
        <Typography variant={'body2'} color={'textPrimary'}>{props.codeModuleInstanceConnectionDto.outputCodeModuleInstanceName}.{props.codeModuleInstanceConnectionDto.outputCodeModuleInstancePortName}</Typography>
        <EastIcon style={{fontSize: 15, marginLeft: 10, marginRight: 10}} color={'secondary'}/>
        <Typography variant={'body2'} color={'textPrimary'}>{props.codeModuleInstanceConnectionDto.inputCodeModuleInstanceName}.{props.codeModuleInstanceConnectionDto.inputCodeModuleInstancePortName}</Typography>
      </div>
      <IconButton size={'small'} onClick={handleClick}>
        <CloseIcon fontSize={'small'}/>
      </IconButton>
    </div>
  );
}

export default ConfiguratorCodeModuleInstanceConnection;
