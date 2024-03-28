import { ApiPropertyOptional, IntersectionType, PickType } from '@nestjs/swagger';
import { ProjectDto } from './project.dto';
import { WorkspaceDesignerDto } from '../../designer/dtos/workspace-designer.dto';
import { IsOptional, IsString } from 'class-validator';

export class CreateProjectDto extends IntersectionType(
  PickType(ProjectDto, ['name'] as const),
  PickType(WorkspaceDesignerDto, ['type'] as const)
) {

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  description: string;

}
