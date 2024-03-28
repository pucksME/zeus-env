import { PartialType, PickType } from '@nestjs/swagger';
import { ProjectDto } from './project.dto';

export class UpdateProjectDto extends PartialType(PickType(ProjectDto, ['name', 'description'] as const)) {

}
