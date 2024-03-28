import { IdentifierLocation } from '../modules/designer/enums/identifier-location.enum';
import { PermissionToken } from '../types/permission-token.type';

export interface Permission {
  keyName: string;
  extractFrom: IdentifierLocation;
  token: PermissionToken;
  relations?: string[];
}
