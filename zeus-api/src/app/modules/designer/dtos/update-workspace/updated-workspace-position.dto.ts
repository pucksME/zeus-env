import { PickType } from '@nestjs/swagger';
import { UpdateWorkspacePositionDto } from './update-workspace-position.dto';

export class UpdatedWorkspacePositionDto extends PickType(
  UpdateWorkspacePositionDto, ['positionX', 'positionY'] as const
) {
}
