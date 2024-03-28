import { PickType } from '@nestjs/swagger';
import { UpdateWorkspacePropertiesDto } from './update-workspace-properties.dto';

export class UpdatedWorkspacePropertiesDto extends PickType(
  UpdateWorkspacePropertiesDto,
  ['positionX', 'positionY', 'scale']
) {
}
