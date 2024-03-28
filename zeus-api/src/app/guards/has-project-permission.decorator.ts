import { SetMetadata } from '@nestjs/common';
import { Permission } from '../interfaces/permission.interface';
import { MetadataKeys } from '../metadata-keys.enum';
import { PermissionGuardMetadata } from '../interfaces/permission-guard-metadata.interface';
import { PermissionGuardMode } from '../enums/permission-guard-mode.enum';

export const HasProjectPermission = (...permissions: Permission[]) => {

  const permissionGuardMetadata: PermissionGuardMetadata = {
    mode: PermissionGuardMode.PROJECT,
    permissions
  };

  return SetMetadata(MetadataKeys.PERMISSION_GUARD, permissionGuardMetadata);

};
