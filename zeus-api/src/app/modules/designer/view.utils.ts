import { View } from './entities/view.entity';
import { ViewDto } from './dtos/view.dto';
import { ComponentUtils } from './component.utils';
import { WorkspaceType } from './enums/workspace-type.enum';
import { ViewType } from './enums/view-type.enum';
import { InternalServerErrorException } from '@nestjs/common';
import { ViewDimensions } from './interfaces/view-dimensions.interface';
import { ViewPosition } from './interfaces/view-position.interface';
import { AppUtils } from '../../app.utils';
import { Component } from './entities/component.entity';

export abstract class ViewUtils {

  static buildViewDto(viewEntity: View, defaultName: string = null): ViewDto {
    return {
      uuid: viewEntity.uuid,
      name: viewEntity.name === null && defaultName !== null ? defaultName : viewEntity.name,
      type: viewEntity.type,
      height: viewEntity.height,
      width: viewEntity.width,
      positionX: viewEntity.positionX,
      positionY: viewEntity.positionY,
      isRoot: viewEntity.isRoot,
      components: viewEntity.components
        ? AppUtils.sort<Component>(viewEntity.components)
          .map(componentEntity => ComponentUtils.buildComponentDto(
            componentEntity, ComponentUtils.buildComponentName(componentEntity)
          ))
        : null
    };
  }

  static getDefaultViewDimensions(workspaceType: WorkspaceType, viewType: ViewType): ViewDimensions {
    switch (workspaceType) {
      case WorkspaceType.DESKTOP:
        return ViewUtils.getDefaultDesktopViewDimensions(viewType);
      default:
        throw new InternalServerErrorException(`Workspace type ${workspaceType} has no default dimensions`);
    }
  }

  private static getDefaultDesktopViewDimensions(viewType: ViewType): ViewDimensions {
    switch (viewType) {
      case ViewType.PAGE:
        return { height: 1080, width: 1920 };
      default:
        throw new InternalServerErrorException(`View type ${viewType} has no default dimensions`);
    }
  }

  // TODO: Also the client has to know the initial view's default position
  static getDefaultViewPosition(workspaceType: WorkspaceType): ViewPosition {
    switch (workspaceType) {
      case WorkspaceType.DESKTOP:
        return { positionX: 100, positionY: 100 };
      default:
        throw new InternalServerErrorException(`Workspace type ${workspaceType} has no default position`);
    }
  }

}
