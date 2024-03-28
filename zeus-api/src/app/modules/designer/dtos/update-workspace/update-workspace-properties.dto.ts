import { IntersectionType, PartialType } from '@nestjs/swagger';
import { UpdateWorkspacePositionDto } from './update-workspace-position.dto';
import { UpdateWorkspaceScaleDto } from './update-workspace-scale.dto';

export class UpdateWorkspacePropertiesDto extends PartialType(
  IntersectionType(UpdateWorkspacePositionDto, UpdateWorkspaceScaleDto)
) {

}
