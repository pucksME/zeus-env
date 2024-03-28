import {
  BadRequestException,
  Inject,
  Injectable,
  InternalServerErrorException,
  NotFoundException
} from '@nestjs/common';
import { ViewDataService } from '../../data/view-data/view-data.service';
import { CreateViewDto } from '../../dtos/create-view.dto';
import { ViewDto } from '../../dtos/view.dto';
import { DesignerWorkspaceDataService } from '../../data/designer-workspace-data/designer-workspace-data.service';
import { View } from '../../entities/view.entity';
import { ViewDimensions } from '../../interfaces/view-dimensions.interface';
import { ViewUtils } from '../../view.utils';
import { UpdateViewNameDto } from '../../dtos/update-view-name.dto';
import { REQUEST } from '@nestjs/core';
import { RequestKeys } from '../../../../enums/request-keys.enum';
import { ScaleViewDto } from '../../dtos/scale-view.dto';
import { ScaleOrigin } from '../../enums/scale-origin.enum';
import { TranslateViewDto } from '../../dtos/translate-view.dto';
import { ReshapeViewDto } from '../../dtos/reshape-view.dto';
import { PositionViewDto } from '../../dtos/position-view.dto';

@Injectable()
export class ViewService {

  constructor(
    @Inject(REQUEST)
    private readonly req,
    private readonly viewDataService: ViewDataService,
    private readonly workspaceDataService: DesignerWorkspaceDataService
  ) {
  }

  async save(workspaceUuid: string, createViewDto: CreateViewDto): Promise<ViewDto> {

    const workspace = await this.workspaceDataService.find(workspaceUuid, ['views']);

    if (workspace === undefined) {
      throw new NotFoundException(`There was no workspace with uuid ${workspaceUuid}`);
    }

    const view = new View();
    view.isRoot = workspace.views.length === 0;
    view.sorting = workspace.views.length;
    view.type = createViewDto.type;
    view.workspace = workspace;

    let defaultDimensions: ViewDimensions = { height: undefined, width: undefined };
    if (createViewDto.height === undefined || createViewDto.width === undefined) {
      defaultDimensions = ViewUtils.getDefaultViewDimensions(workspace.type, createViewDto.type);
    }

    view.height = createViewDto.height === undefined ? defaultDimensions.height : createViewDto.height;
    view.width = createViewDto.width === undefined ? defaultDimensions.width : createViewDto.width;

    const defaultPosition = ViewUtils.getDefaultViewPosition(workspace.type);

    // workspace has no views
    if (workspace.views.length === 0) {
      view.positionX = defaultPosition.positionX;
      view.positionY = defaultPosition.positionY;
      return ViewUtils.buildViewDto(await this.viewDataService.save(view));
    }

    // TODO: make the new view's position dependent of the active view's y-position instead
    //  keep the current algorithm for the case if there currently is no active view
    // find view with highest y-position value
    let yPositionMax = -1;
    for (const view of workspace.views) {
      if (view.positionY > yPositionMax) {
        yPositionMax = view.positionY;
      }
    }

    // for all views with yPositionMax, find that with the greatest x-position
    let lastView: View = workspace.views[0];
    for (const view of workspace.views) {
      if (view.positionY === yPositionMax && view.positionX >= lastView.positionX) {
        lastView = view;
      }
    }

    // compute new view's position
    view.positionX = lastView.positionX + lastView.width + defaultPosition.positionX;
    view.positionY = lastView.positionY;

    return ViewUtils.buildViewDto(await this.viewDataService.save(view));

  }

  async updateName(viewUuid: string, updateViewNameDto: UpdateViewNameDto): Promise<ViewDto> {
    const view: View | undefined = this.req[RequestKeys.VIEW];

    if (view === undefined) {
      throw new InternalServerErrorException('Could not update view name: view was not injected');
    }

    view.name = (updateViewNameDto.name === undefined) ? null : updateViewNameDto.name;
    return ViewUtils.buildViewDto(await this.viewDataService.save(view));
  }

  async setRootView(viewUuid: string): Promise<ViewDto[]> {
    const view: View | undefined = this.req[RequestKeys.VIEW];

    if (view === undefined) {
      throw new InternalServerErrorException('Could not set root view: view was not injected');
    }

    const views: View[] = [];

    for (const currentView of view.workspace.views) {
      if (!currentView.isRoot || currentView.uuid === view.uuid) {
        continue;
      }

      currentView.isRoot = false;
      views.push(currentView);
    }

    view.isRoot = true;
    views.push(view);

    return (await this.viewDataService.saveMany(views)).map(view => ViewUtils.buildViewDto(view));
  }

  async scaleView(viewUuid: string, scaleViewDto: ScaleViewDto): Promise<ViewDto> {
    const view: View | undefined = this.req[RequestKeys.VIEW];

    if (view === undefined) {
      throw new InternalServerErrorException('Could not scale view: view was not injected');
    }

    const diagonalScaling = scaleViewDto.scaleOrigin === ScaleOrigin.TOP_LEFT || scaleViewDto.scaleOrigin === ScaleOrigin.TOP_RIGHT || scaleViewDto.scaleOrigin === ScaleOrigin.BOTTOM_LEFT || scaleViewDto.scaleOrigin === ScaleOrigin.BOTTOM_RIGHT;

    const height = view.height;
    const width = view.width;

    if (diagonalScaling || scaleViewDto.scaleOrigin === ScaleOrigin.TOP || scaleViewDto.scaleOrigin === ScaleOrigin.BOTTOM) {
      view.height *= scaleViewDto.scaleY;
    }

    if (diagonalScaling || scaleViewDto.scaleOrigin === ScaleOrigin.LEFT || scaleViewDto.scaleOrigin === ScaleOrigin.RIGHT) {
      view.width *= scaleViewDto.scaleX;
    }

    if (scaleViewDto.scaleOrigin === ScaleOrigin.LEFT || scaleViewDto.scaleOrigin === ScaleOrigin.TOP_LEFT || scaleViewDto.scaleOrigin === ScaleOrigin.BOTTOM_LEFT) {
      view.positionX += width - view.width;
    }

    if (scaleViewDto.scaleOrigin === ScaleOrigin.TOP || scaleViewDto.scaleOrigin === ScaleOrigin.TOP_LEFT || scaleViewDto.scaleOrigin === ScaleOrigin.TOP_RIGHT) {
      view.positionY += height - view.height;
    }

    return ViewUtils.buildViewDto(await this.viewDataService.save(view));
  }

  async translateView(viewUuid: string, translateViewDto: TranslateViewDto): Promise<ViewDto> {

    const view: View | undefined = this.req[RequestKeys.VIEW];

    if (view === undefined) {
      throw new InternalServerErrorException('Could not translate view: view was not injected');
    }

    view.positionX += translateViewDto.translateX;
    view.positionY += translateViewDto.translateY;

    return ViewUtils.buildViewDto(await this.viewDataService.save(view));

  }

  async reshapeView(viewUuid: string, reshapeViewDto: ReshapeViewDto): Promise<ViewDto> {

    const view: View | undefined = this.req[RequestKeys.VIEW];

    if (view === undefined) {
      throw new InternalServerErrorException('Could not reshape view: view was not injected');
    }

    if (reshapeViewDto.height === undefined && reshapeViewDto.width === undefined) {
      throw new BadRequestException('Could not reshape view: both height and width were not set');
    }

    if (reshapeViewDto.height !== undefined) {
      view.height = reshapeViewDto.height;
    }

    if (reshapeViewDto.width !== undefined) {
      view.width = reshapeViewDto.width;
    }

    return ViewUtils.buildViewDto(await this.viewDataService.save(view));

  }

  async positionView(viewUuid: string, positionViewDto: PositionViewDto): Promise<ViewDto> {

    const view: View | undefined = this.req[RequestKeys.VIEW];

    if (view === undefined) {
      throw new InternalServerErrorException('Could not position view: view was not injected');
    }

    if (positionViewDto.positionX === undefined && positionViewDto.positionY === undefined) {
      throw new BadRequestException('Could not position view: both positionX and positionY were not set');
    }

    if (positionViewDto.positionX !== undefined) {
      view.positionX = positionViewDto.positionX;
    }

    if (positionViewDto.positionY !== undefined) {
      view.positionY = positionViewDto.positionY;
    }

    return ViewUtils.buildViewDto(await this.viewDataService.save(view));

  }

  async deleteView(viewUuid: string): Promise<void> {

    const view: View | undefined = this.req[RequestKeys.VIEW];

    if (view === undefined) {
      throw new InternalServerErrorException('Could not delete view: view was not injected');
    }

    await this.viewDataService.delete(view.uuid);

  }

}
