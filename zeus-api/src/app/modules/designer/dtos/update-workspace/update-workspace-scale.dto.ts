import { PickType } from '@nestjs/swagger';
import { WorkspaceDesignerDto } from '../workspace-designer.dto';

export class UpdateWorkspaceScaleDto extends PickType(WorkspaceDesignerDto, ['scale'] as const) {
}
