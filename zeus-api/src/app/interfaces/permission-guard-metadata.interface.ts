import { Permission } from './permission.interface';
import { PermissionGuardMode } from '../enums/permission-guard-mode.enum';

export interface PermissionGuardMetadata {
  permissions: Permission[];
  mode: PermissionGuardMode;
}
