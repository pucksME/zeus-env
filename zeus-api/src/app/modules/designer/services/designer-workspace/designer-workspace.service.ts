import { Injectable, InternalServerErrorException, NotFoundException } from '@nestjs/common';
import { DesignerWorkspaceDataService } from '../../data/designer-workspace-data/designer-workspace-data.service';
import { WorkspaceDesignerDto } from '../../dtos/workspace-designer.dto';
import { DesignerWorkspaceUtils } from '../../designer-workspace.utils';
import { UserProjectAssignment } from '../../../user/entities/user-project-assignment.entity';
import { UpdateWorkspacePositionDto } from '../../dtos/update-workspace/update-workspace-position.dto';
import { UpdatedWorkspacePositionDto } from '../../dtos/update-workspace/updated-workspace-position.dto';
import { UserProjectAssignmentDataService } from '../../../user/data/user-project-assignment-data/user-project-assignment-data.service';
import { UpdateWorkspaceScaleDto } from '../../dtos/update-workspace/update-workspace-scale.dto';
import { UpdatedWorkspaceScaleDto } from '../../dtos/update-workspace/updated-workspace-scale.dto';
import { UpdateWorkspacePropertiesDto } from '../../dtos/update-workspace/update-workspace-properties.dto';
import { UpdatedWorkspacePropertiesDto } from '../../dtos/update-workspace/updated-workspace-properties.dto';
import { ComponentDataService } from '../../data/component-data/component-data.service';
import { BlueprintComponentDataService } from '../../data/blueprint-component-data/blueprint-component-data.service';
import { ComponentUtils } from '../../component.utils';

@Injectable()
export class DesignerWorkspaceService {

  constructor(
    private readonly workspaceDataService: DesignerWorkspaceDataService,
    private readonly userProjectAssignmentDataService: UserProjectAssignmentDataService,
    private readonly componentDataService: ComponentDataService,
    private readonly blueprintComponentDataService: BlueprintComponentDataService
  ) {
  }

  async find(
    workspaceUuid: string,
    requestingUserProjectAssignment: UserProjectAssignment
  ): Promise<WorkspaceDesignerDto> {

    const workspace = await this.workspaceDataService.find(
      workspaceUuid,
      [
        'project',
        'project.userAssignments',
        'project.userAssignments.user',
        'views'
      ]
    );

    if (workspace === undefined) {
      throw new NotFoundException(`There was no workspace with uuid ${workspaceUuid}`);
    }

    if (!workspace.project) {
      throw new InternalServerErrorException(`The workspace with uuid ${workspaceUuid} had no project`);
    }

    let componentTrees = await this.componentDataService.findTrees([
      'view',
      'blueprintComponent',
      'shapes',
      'shapeMutations',
      'shapeMutations.shape',
      'componentMutations',
      'componentMutations.blueprintComponent'
    ]);

    const blueprintComponentTrees = await this.blueprintComponentDataService.findTrees(['shapes']);

    componentTrees = ComponentUtils.injectBlueprintComponents(componentTrees, blueprintComponentTrees);

    for (const view of workspace.views) {
      view.components = [];
    }

    for (const componentTree of componentTrees) {
      const viewIndex = workspace.views.findIndex(view => view.uuid === componentTree.view.uuid);

      if (viewIndex === -1) {
        continue;
      }

      workspace.views[viewIndex].components.push(componentTree);
    }

    return DesignerWorkspaceUtils.buildWorkspaceDto(
      workspace,
      {
        positionX: requestingUserProjectAssignment.designerPositionX,
        positionY: requestingUserProjectAssignment.designerPositionY,
        scale: requestingUserProjectAssignment.designerScale
      }
    );

  }

  async updateProperties(
    updateWorkspacePropertiesDto: UpdateWorkspacePropertiesDto,
    requestingUserProjectAssignment: UserProjectAssignment
  ): Promise<UpdatedWorkspacePropertiesDto> {

    if (requestingUserProjectAssignment === undefined) {
      throw new InternalServerErrorException('Could not update workspace properties: user project assignment was not injected');
    }

    if (updateWorkspacePropertiesDto.positionX === undefined &&
      updateWorkspacePropertiesDto.positionY === undefined &&
      updateWorkspacePropertiesDto.scale === undefined) {
      return DesignerWorkspaceUtils.buildUpdatedWorkspacePropertiesDto(requestingUserProjectAssignment);
    }

    if (updateWorkspacePropertiesDto.positionX !== undefined) {
      requestingUserProjectAssignment.designerPositionX = updateWorkspacePropertiesDto.positionX;
    }

    if (updateWorkspacePropertiesDto.positionY !== undefined) {
      requestingUserProjectAssignment.designerPositionY = updateWorkspacePropertiesDto.positionY;
    }

    if (updateWorkspacePropertiesDto.scale !== undefined) {
      requestingUserProjectAssignment.designerScale = updateWorkspacePropertiesDto.scale;
    }

    return DesignerWorkspaceUtils.buildUpdatedWorkspacePropertiesDto(
      await this.userProjectAssignmentDataService.save(requestingUserProjectAssignment)
    );

  }

  async updatePosition(
    updateWorkspacePositionDto: UpdateWorkspacePositionDto,
    requestingUserProjectAssignment: UserProjectAssignment
  ): Promise<UpdatedWorkspacePositionDto> {

    if (requestingUserProjectAssignment === undefined) {
      throw new InternalServerErrorException('Could not update workspace position: user project assignment was not injected');
    }

    requestingUserProjectAssignment.designerPositionX = updateWorkspacePositionDto.positionX;
    requestingUserProjectAssignment.designerPositionY = updateWorkspacePositionDto.positionY;

    return DesignerWorkspaceUtils.buildUpdatedWorkspacePositionDto(
      await this.userProjectAssignmentDataService.save(requestingUserProjectAssignment)
    );

  }

  async updateScale(
    updateWorkspaceScaleDto: UpdateWorkspaceScaleDto,
    requestingUserProjectAssignment: UserProjectAssignment
  ): Promise<UpdatedWorkspaceScaleDto> {

    if (requestingUserProjectAssignment === undefined) {
      throw new InternalServerErrorException('Could not update workspace scale: user project assignment was not injected');
    }

    requestingUserProjectAssignment.designerScale = updateWorkspaceScaleDto.scale;

    return DesignerWorkspaceUtils.buildUpdatedWorkspaceScaleDto(
      await this.userProjectAssignmentDataService.save(requestingUserProjectAssignment)
    );

  }

}
