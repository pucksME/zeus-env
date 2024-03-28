import React, { CSSProperties } from 'react';

import './code-module-details.module.scss';
import CodeModuleDetail from '../code-module-detail/code-module-detail';
import WarningIcon from '@material-ui/icons/Warning';
import SettingsIcon from '@material-ui/icons/Settings';
import EastIcon from '@material-ui/icons/East';
import WestIcon from '@material-ui/icons/West';
import { CodeModuleDto } from '../../../../../gen/api-client';

export interface CodeModuleDetailsProps {
  codeModuleDto: CodeModuleDto;
  iconStyle?: CSSProperties;
  textVariant: 'body2' | 'caption';
}

export function CodeModuleDetails(props: CodeModuleDetailsProps) {
  return (
    <div style={{display: 'flex', alignItems: 'center', flexWrap: 'wrap'}}>
      <CodeModuleDetail
        icon={WarningIcon}
        count={props.codeModuleDto.errors.length}
        title={'Errors'}
        titleOne={'Error'}
        warning={true}
        visible={props.codeModuleDto.errors.length !== 0}
        style={{marginRight: 9}}
        iconStyle={props.iconStyle}
        textVariant={props.textVariant}
      />
      <CodeModuleDetail
        icon={SettingsIcon}
        count={props.codeModuleDto.configs.length}
        title={'Configs'}
        titleOne={'Config'}
        visible={props.codeModuleDto.configs.length !== 0}
        style={{marginRight: 9}}
        iconStyle={props.iconStyle}
        textVariant={props.textVariant}
      />
      <CodeModuleDetail
        icon={EastIcon}
        count={props.codeModuleDto.inputEndpoints.length}
        title={'Inputs'}
        titleOne={'Input'}
        visible={props.codeModuleDto.inputEndpoints.length !== 0}
        style={{marginRight: 9}}
        iconStyle={props.iconStyle}
        textVariant={props.textVariant}
      />
      <CodeModuleDetail
        icon={WestIcon}
        count={props.codeModuleDto.outputEndpoints.length}
        title={'Outputs'}
        titleOne={'Output'}
        visible={props.codeModuleDto.outputEndpoints.length !== 0}
        iconStyle={props.iconStyle}
        textVariant={props.textVariant}
      />
    </div>
  );
}

export default CodeModuleDetails;
