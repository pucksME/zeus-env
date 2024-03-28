import {
  BadRequestException,
  CanActivate,
  ExecutionContext,
  Injectable,
  InternalServerErrorException,
  NotFoundException
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { MetadataKeys } from '../metadata-keys.enum';
import { ProjectDataService } from '../modules/project/data/project-data.service';
import {
  DesignerWorkspaceDataService
} from '../modules/designer/data/designer-workspace-data/designer-workspace-data.service';
import { ComponentDataService } from '../modules/designer/data/component-data/component-data.service';
import { UserProjectAssignment } from '../modules/user/entities/user-project-assignment.entity';
import { ViewDataService } from '../modules/designer/data/view-data/view-data.service';
import { isUUID } from 'class-validator';
import { DesignerPermissionToken } from '../modules/designer/enums/designer-permission-token.enum';
import { Permission } from '../interfaces/permission.interface';
import { IdentifierLocation } from '../modules/designer/enums/identifier-location.enum';
import { RequestKeys } from '../enums/request-keys.enum';
import { PermissionGuardMetadata } from '../interfaces/permission-guard-metadata.interface';
import { PermissionGuardMode } from '../enums/permission-guard-mode.enum';
import { PermissionToken } from '../types/permission-token.type';
import { ProjectPermissionToken } from '../modules/project/enums/project-permission-token.enum';
import { ShapeDataService } from '../modules/designer/data/shape-data/shape-data.service';
import {
  BlueprintComponentDataService
} from '../modules/designer/data/blueprint-component-data/blueprint-component-data.service';
import { VisualizerPermissionToken } from '../modules/visualizer/enums/visualizer-permission-token.enum';
import {
  CodeModuleInstanceDataService
} from '../modules/visualizer/data/code-module-instance-data/code-module-instance-data.service';
import { View } from '../modules/designer/entities/view.entity';
import { Component } from '../modules/designer/entities/component.entity';

@Injectable()
export class PermissionGuard implements CanActivate {

  private request;

  constructor(
    private readonly reflector: Reflector,
    private readonly projectDataService: ProjectDataService,
    private readonly workspaceDataService: DesignerWorkspaceDataService,
    private readonly viewDataService: ViewDataService,
    private readonly componentDataService: ComponentDataService,
    private readonly shapeDataService: ShapeDataService,
    private readonly blueprintComponentDataService: BlueprintComponentDataService,
    private readonly codeModuleInstanceDataService: CodeModuleInstanceDataService
  ) {
  }

  async canActivate(
    context: ExecutionContext
  ): Promise<boolean> {
    const permissionGuardMetadata: PermissionGuardMetadata = this.reflector.getAllAndOverride(
      MetadataKeys.PERMISSION_GUARD, [context.getHandler(), context.getClass()]
    );

    // no decorator was used
    if (permissionGuardMetadata === undefined) {
      return true;
    }

    const { permissions, mode } = permissionGuardMetadata;

    if (permissions.length === 0) {
      throw new InternalServerErrorException('Cannot perform a permission check without permissions');
    }

    this.request = context.switchToHttp().getRequest();

    for (const permission of permissions) {
      if (!(await this.verifyPermission(
        permission,
        mode,
        (permission.relations === undefined) ? [] : permission.relations))) {
        return false;
      }
    }

    return true;

  }

  private async verifyPermission(permission: Permission, mode: PermissionGuardMode, relations: string[]): Promise<boolean> {

    const { user } = this.request;

    if (!user) {
      throw new InternalServerErrorException('Could not check permissions: user was not injected');
    }

    const identifier = this.extractIdentifier(permission.keyName, permission.extractFrom);

    if (identifier === undefined) {
      throw new BadRequestException('Request did not contain a valid identifier for extraction');
    }

    if (!isUUID(identifier)) {
      throw new BadRequestException(`The extracted identifier ${identifier} for the key ${permission.keyName} is not a valid uuid`);
    }

    const userProjectAssignment = await this.findUserProjectAssignment(permission.keyName, identifier, user.sub, relations);

    if (userProjectAssignment === undefined) {
      return false;
    }

    return this.hasPermission(userProjectAssignment, permission.token, mode);

  }

  private hasPermission(
    userProjectAssignment: UserProjectAssignment | null,
    permissionToken: PermissionToken,
    mode: PermissionGuardMode
  ): boolean {
    if (userProjectAssignment === null) {
      return true;
    }

    switch (mode) {
      case PermissionGuardMode.PROJECT:
        return this.hasProjectPermission(userProjectAssignment, permissionToken as ProjectPermissionToken);
      case PermissionGuardMode.DESIGNER:
        return this.hasDesignerPermission(userProjectAssignment, permissionToken as DesignerPermissionToken);
      case PermissionGuardMode.VISUALIZER:
        return this.hasVisualizerPermission(userProjectAssignment, permissionToken as VisualizerPermissionToken);
      default:
        throw new InternalServerErrorException(`Permission guard does not support mode ${mode} operation`);
    }
  }

  private hasProjectPermission(
    userProjectAssignmentEntity: UserProjectAssignment, permissionToken: ProjectPermissionToken
  ): boolean {
    switch (permissionToken) {
      case ProjectPermissionToken.READ:
        return userProjectAssignmentEntity.projectPermission === ProjectPermissionToken.READ ||
          userProjectAssignmentEntity.projectPermission === ProjectPermissionToken.WRITE ||
          userProjectAssignmentEntity.projectPermission === ProjectPermissionToken.OWN;
      case ProjectPermissionToken.WRITE:
        return userProjectAssignmentEntity.projectPermission === ProjectPermissionToken.WRITE ||
          userProjectAssignmentEntity.projectPermission === ProjectPermissionToken.OWN;
      case ProjectPermissionToken.OWN:
        return userProjectAssignmentEntity.projectPermission === ProjectPermissionToken.OWN;
    }
  }

  private hasDesignerPermission(
    userProjectAssignmentEntity: UserProjectAssignment, permissionToken: DesignerPermissionToken
  ): boolean {
    switch (permissionToken) {
      case DesignerPermissionToken.READ:
        return userProjectAssignmentEntity.designerPermission === DesignerPermissionToken.READ ||
          userProjectAssignmentEntity.designerPermission === DesignerPermissionToken.WRITE ||
          userProjectAssignmentEntity.designerPermission === DesignerPermissionToken.OWN;
      case DesignerPermissionToken.WRITE:
        return userProjectAssignmentEntity.designerPermission === DesignerPermissionToken.WRITE ||
          userProjectAssignmentEntity.designerPermission === DesignerPermissionToken.OWN;
      case DesignerPermissionToken.OWN:
        return userProjectAssignmentEntity.designerPermission === DesignerPermissionToken.OWN;
    }
  }

  private hasVisualizerPermission(
    userProjectAssignmentEntity: UserProjectAssignment, permissionToken: VisualizerPermissionToken
  ): boolean {
    switch (permissionToken) {
      case VisualizerPermissionToken.READ:
        return userProjectAssignmentEntity.visualizerPermission === VisualizerPermissionToken.READ ||
          userProjectAssignmentEntity.visualizerPermission === VisualizerPermissionToken.WRITE ||
          userProjectAssignmentEntity.visualizerPermission === VisualizerPermissionToken.OWN;
      case VisualizerPermissionToken.WRITE:
        return userProjectAssignmentEntity.visualizerPermission === VisualizerPermissionToken.WRITE ||
          userProjectAssignmentEntity.visualizerPermission === VisualizerPermissionToken.OWN;
      case VisualizerPermissionToken.OWN:
        return userProjectAssignmentEntity.visualizerPermission === VisualizerPermissionToken.OWN;
    }
  }

  private extractIdentifier(keyName: string, identifierLocation: IdentifierLocation): string | undefined {
    switch (identifierLocation) {
      case IdentifierLocation.PARAMS:
        return this.extractIdentifierFromParams(keyName);
      case IdentifierLocation.BODY:
        return this.extractIdentifierFromBody(keyName);
      default:
        throw new InternalServerErrorException('Unsupported location when trying to extract identifier from request');
    }
  }

  private extractIdentifierFromParams(keyName: string): string | undefined {
    return this.request.params[keyName] !== undefined ? this.request.params[keyName] : undefined;
  }

  private extractIdentifierFromBody(keyName: string): string | undefined {
    return this.request.body[keyName] !== undefined ? this.request.body[keyName] : undefined;
  }

  private findUserProjectAssignment(
    keyName: string, identifier: string, requestingUserUuid: string, relations: string[]
  ): Promise<UserProjectAssignment | null | undefined> {
    switch (keyName) {
      // TODO: create an enum for these key names
      case 'workspaceUuid':
        return this.findUserProjectAssignmentWithWorkspaceUuid(identifier, requestingUserUuid, relations);
      case 'projectUuid':
        return this.findUserProjectAssignmentWithProjectUuid(identifier, requestingUserUuid, relations);
      case 'viewUuid':
        return this.findUserProjectAssignmentWithViewUuid(identifier, requestingUserUuid, relations);
      case 'componentUuid':
        return this.findUserProjectAssignmentWithComponentUuid(identifier, requestingUserUuid, relations);
      case 'shapeUuid':
        return this.findUserProjectAssignmentWithShapeUuid(identifier, requestingUserUuid, relations);
      case 'blueprintComponentUuid':
        return this.findUserProjectAssignmentWithBlueprintComponentUuid(identifier, requestingUserUuid, relations);
      case 'codeModuleInstanceUuid':
        return this.findUserProjectAssignmentWithCodeModuleInstanceUuid(identifier, requestingUserUuid, relations);
      default:
        throw new InternalServerErrorException(`Unsupported key name ${keyName}: could not map identifier to key name`);
    }
  }

  private findProjectAssignmentOfRequestingUser(
    userProjectAssignmentEntities: UserProjectAssignment[], requestingUserUuid: string
  ): UserProjectAssignment | undefined {
    return userProjectAssignmentEntities.find(
      assignment => assignment.user !== null && assignment.user.uuid === requestingUserUuid
    );
  }

  private async findUserProjectAssignmentWithWorkspaceUuid(
    workspaceUuid: string, requestingUserUuid, relations: string[]
  ): Promise<UserProjectAssignment | undefined> {

    const workspace = await this.workspaceDataService.find(
      workspaceUuid, ['project', 'project.userAssignments', 'project.userAssignments.user', ...relations]
    );

    if (workspace === undefined) {
      throw new NotFoundException(`There was no workspace with uuid ${workspaceUuid}`);
    }

    const userProjectAssignment = this.findProjectAssignmentOfRequestingUser(
      workspace.project.userAssignments, requestingUserUuid
    );

    if (userProjectAssignment !== undefined) {
      this.request[RequestKeys.USER_PROJECT_ASSIGNMENT] = userProjectAssignment;
      this.request[RequestKeys.DESIGNER_WORKSPACE] = workspace;
    }

    return userProjectAssignment;

  }

  private async findUserProjectAssignmentWithProjectUuid(
    projectUuid: string, requestingUserUuid, relations: string[]
  ): Promise<UserProjectAssignment | undefined> {

    const project = await this.projectDataService.findOne(
      projectUuid, ['userAssignments', 'userAssignments.user', ...relations]
    );

    if (project === undefined) {
      throw new NotFoundException(`There was no project with uuid ${projectUuid}`);
    }

    const userProjectAssignment = this.findProjectAssignmentOfRequestingUser(
      project.userAssignments, requestingUserUuid
    );

    if (userProjectAssignment !== undefined) {
      this.request[RequestKeys.USER_PROJECT_ASSIGNMENT] = userProjectAssignment;
      this.request[RequestKeys.PROJECT] = project;
    }

    return userProjectAssignment;

  }

  private async findUserProjectAssignmentWithViewUuid(
    viewUuid: string, requestingUserUuid: string, relations: string[]
  ): Promise<UserProjectAssignment | undefined> {

    const view = await this.viewDataService.find(
      viewUuid,
      [
        'workspace',
        'workspace.project',
        'workspace.project.userAssignments',
        'workspace.project.userAssignments.user',
        ...relations
      ]
    );

    if (view === undefined) {
      throw new NotFoundException(`There was no view with uuid ${viewUuid}`);
    }

    const userProjectAssignment = this.findProjectAssignmentOfRequestingUser(
      view.workspace.project.userAssignments, requestingUserUuid
    );

    if (userProjectAssignment !== undefined) {
      this.request[RequestKeys.USER_PROJECT_ASSIGNMENT] = userProjectAssignment;
      this.request[RequestKeys.VIEW] = view;
    }

    return userProjectAssignment;

  }

  private async getViewFromRootComponent(component: Component): Promise<View> {
    const rootComponent = await this.componentDataService.findRoot(
      component,
      [
        'view',
        'view.workspace',
        'view.workspace.project',
        'view.workspace.project.userAssignments',
        'view.workspace.project.userAssignments.user'
      ]
    );

    if (rootComponent === undefined) {
      throw new InternalServerErrorException(`Could not find root component of component with uuid ${component.uuid}`);
    }

    return rootComponent.view;
  }
  private async findUserProjectAssignmentWithComponentUuid(
    componentUuid: string, requestingUserUuid: string, relations: string[]
  ): Promise<UserProjectAssignment | undefined> {

    const component = await this.componentDataService.find(
      componentUuid,
      [
        'view',
        'view.workspace',
        'view.workspace.project',
        'view.workspace.project.userAssignments',
        'view.workspace.project.userAssignments.user',
        ...relations
      ]
    );

    if (component === undefined) {
      throw new NotFoundException(`There was no component with uuid ${componentUuid}`);
    }

    const view: View = (component.view !== null) ? component.view : await this.getViewFromRootComponent(component);

    const userProjectAssignment = this.findProjectAssignmentOfRequestingUser(
      view.workspace.project.userAssignments, requestingUserUuid
    );

    if (userProjectAssignment !== undefined) {
      this.request[RequestKeys.USER_PROJECT_ASSIGNMENT] = userProjectAssignment;
      this.request[RequestKeys.COMPONENT] = component;
    }

    return userProjectAssignment;

  }

  private async findUserProjectAssignmentWithShapeUuid(
    shapeUuid: string, requestingUserUuid: string, relations: string[]
  ): Promise<UserProjectAssignment | undefined> {
    const shape = await this.shapeDataService.find(
      shapeUuid,
      [
        'component',
        'component.view',
        'component.view.workspace',
        'component.view.workspace.project',
        'component.view.workspace.project.userAssignments',
        'component.view.workspace.project.userAssignments.user',
        'blueprintComponent',
        ...relations
      ]
    );

    if (shape === undefined) {
      throw new NotFoundException(`There was no shape with uuid ${shapeUuid}`);
    }

    if (shape.blueprintComponent !== null) {
      this.request[RequestKeys.SHAPE] = shape;
      return null;
    }

    const view: View = (shape.component.view !== null)
      ? shape.component.view
      : await this.getViewFromRootComponent(shape.component);

    const userProjectAssignment = this.findProjectAssignmentOfRequestingUser(
      view.workspace.project.userAssignments, requestingUserUuid
    );

    if (userProjectAssignment !== undefined) {
      this.request[RequestKeys.USER_PROJECT_ASSIGNMENT] = userProjectAssignment;
      this.request[RequestKeys.SHAPE] = shape;
    }

    return userProjectAssignment;
  }

  private async findUserProjectAssignmentWithBlueprintComponentUuid(
    blueprintComponentUuid: string, requestingUserUuid: string, relations: string[]
  ): Promise<UserProjectAssignment | undefined> {
    const blueprintComponent = await this.blueprintComponentDataService.find(
      blueprintComponentUuid,
      [
        'workspace',
        'workspace.project',
        'workspace.project.userAssignments',
        'workspace.project.userAssignments.user',
        ...relations
      ]
    );

    if (blueprintComponent === undefined) {
      throw new NotFoundException(`There was no blueprint component with uuid ${blueprintComponentUuid}`);
    }

    const userProjectAssignment = this.findProjectAssignmentOfRequestingUser(
      blueprintComponent.workspace.project.userAssignments, requestingUserUuid
    );

    if (userProjectAssignment !== undefined) {
      this.request[RequestKeys.USER_PROJECT_ASSIGNMENT] = userProjectAssignment;
      this.request[RequestKeys.BLUEPRINT_COMPONENT] = blueprintComponent;
    }

    return userProjectAssignment;
  }

  private async findUserProjectAssignmentWithCodeModuleInstanceUuid(
    codeModuleInstanceUuid: string, requestingUserUuid: string, relations: string[]
  ): Promise<UserProjectAssignment | undefined> {
    const codeModuleInstance = await this.codeModuleInstanceDataService.find(
      codeModuleInstanceUuid,
      [
        'workspace',
        'workspace.component',
        'workspace.component.view',
        'workspace.component.view.workspace',
        'workspace.component.view.workspace.project',
        'workspace.component.view.workspace.project.userAssignments',
        'workspace.component.view.workspace.project.userAssignments.user',
        ...relations
      ]
    );

    if (codeModuleInstance === undefined) {
      throw new NotFoundException(`There was no code module instance with uuid ${codeModuleInstanceUuid}`);
    }

    const userProjectAssignment = this.findProjectAssignmentOfRequestingUser(
      codeModuleInstance.workspace.component.view.workspace.project.userAssignments, requestingUserUuid
    );

    if (userProjectAssignment !== undefined) {
      this.request[RequestKeys.USER_PROJECT_ASSIGNMENT] = userProjectAssignment;
      this.request[RequestKeys.CODE_MODULE_INSTANCE] = codeModuleInstance;
    }

    return userProjectAssignment;
  }

}
