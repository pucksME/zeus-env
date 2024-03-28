import { CodeModuleInstancePort } from './code-module-instance-port.interface';

export interface CodeModuleInstancesConnection {
  input: CodeModuleInstancePort;
  output: CodeModuleInstancePort;
}
