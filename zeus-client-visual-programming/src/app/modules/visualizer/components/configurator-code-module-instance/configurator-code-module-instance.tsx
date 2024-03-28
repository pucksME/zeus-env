import React, { CSSProperties } from 'react';

import './configurator-code-module-instance.module.scss';
import { useConnections } from '../../data/code-module-instance-data.hooks';
import ConfiguratorCodeModuleInstanceConnection
  from '../configurator-code-module-instance-connection/configurator-code-module-instance-connection';
import { Typography } from '@material-ui/core';
import { CodeModuleInstanceDto } from '../../../../../gen/api-client';
import CodeModuleDetails from '../code-module-details/code-module-details';

export interface ConfiguratorCodeModuleInstanceProps {
  componentUuid: string;
  codeModuleInstanceDto: CodeModuleInstanceDto;
}

export function ConfiguratorCodeModuleInstance(
  props: ConfiguratorCodeModuleInstanceProps
) {
  const {isLoading, isError, codeModuleInstancesConnectionDtos, error} = useConnections(props.componentUuid);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (isError) {
    return <div>{error}</div>;
  }

  const buildNoConnections = () => <Typography
    variant={'body2'}
    color={'textPrimary'}
    style={{
      marginTop: 3,
      marginBottom: 3,
      paddingLeft: 5,
      paddingRight: 5
    }}
  >There are no connections</Typography>

  const connections = codeModuleInstancesConnectionDtos.filter(
    connection => connection.inputCodeModuleInstanceName === props.codeModuleInstanceDto.codeModule.name ||
      connection.outputCodeModuleInstanceName === props.codeModuleInstanceDto.codeModule.name
  );

  const inputConnections = connections.filter(
    connection => connection.inputCodeModuleInstanceName === props.codeModuleInstanceDto.codeModule.name
  );

  const outputConnections = connections.filter(
    connection => connection.outputCodeModuleInstanceName === props.codeModuleInstanceDto.codeModule.name
  );

  const categoryStyle: CSSProperties = {marginBottom: 10, paddingBottom: 10, borderBottom: '1px solid #eee'};

  return (
    <div style={{width: '100%'}}>
      <div style={{...categoryStyle}}>
        <Typography variant={'body1'} color={'textSecondary'}>Details</Typography>
        <Typography variant={'body2'} color={'textPrimary'} style={{marginTop: 3, marginBottom: 3}}>
          {props.codeModuleInstanceDto.codeModule.description}
        </Typography>
        <CodeModuleDetails
          codeModuleDto={props.codeModuleInstanceDto.codeModule}
          iconStyle={{fontSize: 15}}
          textVariant={'body2'}
        />
      </div>
      <div style={{...categoryStyle}}>
        <Typography variant={'body1'} color={'textSecondary'}>Input Connections</Typography>
        {(inputConnections.length !== 0)
          ? inputConnections.map(connection => <ConfiguratorCodeModuleInstanceConnection
            key={connection.uuid}
            componentUuid={props.componentUuid}
            codeModuleInstanceConnectionDto={connection}
        />)
          : buildNoConnections()}
        </div>
      <div>
        <Typography variant={'body1'} color={'textSecondary'}>Output Connections</Typography>
        {(outputConnections.length !== 0)
          ? outputConnections.map(connection => <ConfiguratorCodeModuleInstanceConnection
            key={connection.uuid}
            componentUuid={props.componentUuid}
            codeModuleInstanceConnectionDto={connection}
        />)
          : buildNoConnections()}
      </div>
    </div>
  );
}

export default ConfiguratorCodeModuleInstance;
