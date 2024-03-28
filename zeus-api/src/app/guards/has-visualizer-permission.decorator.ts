import { Permission } from '../interfaces/permission.interface';
import { PermissionGuardMetadata } from '../interfaces/permission-guard-metadata.interface';
import { PermissionGuardMode } from '../enums/permission-guard-mode.enum';
import { SetMetadata } from '@nestjs/common';
import { MetadataKeys } from '../metadata-keys.enum';

export const HasVisualizerPermission = (...permissions: Permission[]) => {

  const permissionGuardMetaData: PermissionGuardMetadata = {
    mode: PermissionGuardMode.VISUALIZER,
    permissions
  };

  return SetMetadata(MetadataKeys.PERMISSION_GUARD, permissionGuardMetaData);

}
