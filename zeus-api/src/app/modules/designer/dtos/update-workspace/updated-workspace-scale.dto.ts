import { PickType } from '@nestjs/swagger';
import { UpdateWorkspaceScaleDto } from './update-workspace-scale.dto';

export class UpdatedWorkspaceScaleDto extends PickType(UpdateWorkspaceScaleDto, ['scale'] as const) {
}
