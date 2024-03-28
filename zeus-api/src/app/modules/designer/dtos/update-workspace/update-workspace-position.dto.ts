import { PickType } from '@nestjs/swagger';
import { WorkspaceDesignerDto } from '../workspace-designer.dto';

export class UpdateWorkspacePositionDto extends PickType(WorkspaceDesignerDto, ['positionX', 'positionY'] as const) {
}
