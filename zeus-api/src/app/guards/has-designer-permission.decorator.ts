import { SetMetadata } from '@nestjs/common';
import { MetadataKeys } from '../metadata-keys.enum';
import { Permission } from '../interfaces/permission.interface';
import { PermissionGuardMetadata } from '../interfaces/permission-guard-metadata.interface';
import { PermissionGuardMode } from '../enums/permission-guard-mode.enum';

export const HasDesignerPermission = (...permissions: Permission[]) => {

  const permissionGuardMetadata: PermissionGuardMetadata = {
    mode: PermissionGuardMode.DESIGNER,
    permissions
  };

  return SetMetadata(MetadataKeys.PERMISSION_GUARD, permissionGuardMetadata);

};
