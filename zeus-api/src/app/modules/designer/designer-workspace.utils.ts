import { DesignerWorkspace } from './entities/designer-workspace.entity';
import { WorkspaceDesignerDto } from './dtos/workspace-designer.dto';
import { ViewUtils } from './view.utils';
import { UserProjectAssignment } from '../user/entities/user-project-assignment.entity';
import { UpdatedWorkspacePositionDto } from './dtos/update-workspace/updated-workspace-position.dto';
import { UpdatedWorkspaceScaleDto } from './dtos/update-workspace/updated-workspace-scale.dto';
import { UpdatedWorkspacePropertiesDto } from './dtos/update-workspace/updated-workspace-properties.dto';
import { AppUtils } from '../../app.utils';
import { View } from './entities/view.entity';

export interface WorkspaceState {
  positionX: number;
  positionY: number;
  scale: number;
}

export abstract class DesignerWorkspaceUtils {

  static buildWorkspaceDto(
    workspaceEntity: DesignerWorkspace,
    workspaceState: WorkspaceState
  ): WorkspaceDesignerDto {
    return {
      uuid: workspaceEntity.uuid,
      name: workspaceEntity.name,
      type: workspaceEntity.type,
      positionX: workspaceState ? workspaceState.positionX : null,
      positionY: workspaceState ? workspaceState.positionY : null,
      scale: workspaceState ? workspaceState.scale : null,
      views: workspaceEntity.views
        ? AppUtils.sort<View>(workspaceEntity.views)
          .map((viewEntity, index) => ViewUtils.buildViewDto(viewEntity, `View ${index + 1}`))
        : null
    };
  }

  static buildUpdatedWorkspacePropertiesDto(
    userProjectAssignmentEntity: UserProjectAssignment
  ): UpdatedWorkspacePropertiesDto {
    return {
      positionX: userProjectAssignmentEntity.designerPositionX,
      positionY: userProjectAssignmentEntity.designerPositionY,
      scale: userProjectAssignmentEntity.designerScale
    };
  }

  static buildUpdatedWorkspacePositionDto(
    userProjectAssignmentEntity: UserProjectAssignment
  ): UpdatedWorkspacePositionDto {
    return {
      positionX: userProjectAssignmentEntity.designerPositionX,
      positionY: userProjectAssignmentEntity.designerPositionY
    };
  }

  static buildUpdatedWorkspaceScaleDto(
    userProjectAssignmentEntity: UserProjectAssignment
  ): UpdatedWorkspaceScaleDto {
    return {
      scale: userProjectAssignmentEntity.designerScale
    };
  }

}
