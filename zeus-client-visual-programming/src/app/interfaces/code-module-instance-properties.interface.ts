import { CodeModuleInstancePortProperties } from './code-module-instance-port-properties.interface';

export interface CodeModuleInstanceProperties {
  codeModuleInstanceUuid: string;
  codeModuleName: string;
  height: number;
  mainDetailsHeight: number;
  outputsWidth: number;
  inputs: CodeModuleInstancePortProperties[];
  outputs: CodeModuleInstancePortProperties[];
}
