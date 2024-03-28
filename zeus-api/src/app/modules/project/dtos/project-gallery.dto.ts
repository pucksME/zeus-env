import { ApiProperty, IntersectionType, PickType } from '@nestjs/swagger';
import { ProjectDto } from './project.dto';
import { WorkspaceDesignerDto } from '../../designer/dtos/workspace-designer.dto';
import { ViewDto } from '../../designer/dtos/view.dto';

export class ProjectGalleryDto extends IntersectionType(
  PickType(ProjectDto, ['uuid', 'name', 'description'] as const),
  PickType(WorkspaceDesignerDto, ['type'] as const)
) {

  @ApiProperty()
  view: ViewDto;

}
