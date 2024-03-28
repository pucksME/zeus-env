import { ProjectPermissionToken } from '../modules/project/enums/project-permission-token.enum';
import { DesignerPermissionToken } from '../modules/designer/enums/designer-permission-token.enum';
import { VisualizerPermissionToken } from '../modules/visualizer/enums/visualizer-permission-token.enum';

export type PermissionToken = ProjectPermissionToken | DesignerPermissionToken | VisualizerPermissionToken;
