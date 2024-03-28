import { forwardRef, Module } from '@nestjs/common';
import { ComponentController } from './controllers/component/component.controller';
import { ComponentService } from './services/component/component.service';
import { ComponentDataService } from './data/component-data/component-data.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Component } from './entities/component.entity';
import { DesignerWorkspace } from './entities/designer-workspace.entity';
import { DesignerWorkspaceService } from './services/designer-workspace/designer-workspace.service';
import { DesignerWorkspaceDataService } from './data/designer-workspace-data/designer-workspace-data.service';
import { DesignerWorkspaceController } from './controllers/designer-workspace/designer-workspace.controller';
import { View } from './entities/view.entity';
import { ViewService } from './services/view/view.service';
import { ViewDataService } from './data/view-data/view-data.service';
import { ProjectModule } from '../project/project.module';
import { ViewController } from './controllers/view/view.controller';
import { UserModule } from '../user/user.module';
import { ShapeController } from './controllers/shape/shape.controller';
import { Shape } from './entities/shape.entity';
import { ShapeService } from './services/shape/shape.service';
import { ShapeDataService } from './data/shape-data/shape-data.service';
import { BlueprintComponentController } from './controllers/blueprint-component/blueprint-component.controller';
import { BlueprintComponentService } from './services/blueprint-component/blueprint-component.service';
import { BlueprintComponentDataService } from './data/blueprint-component-data/blueprint-component-data.service';
import { BlueprintComponent } from './entities/blueprint-component.entity';
import { ShapeMutation } from './entities/shape-mutation.entity';
import { ShapeMutationDataService } from './data/shape-mutation-data/shape-mutation-data.service';
import { VisualizerModule } from '../visualizer/visualizer.module';
import { ComponentMutationDataService } from './data/component-mutation-data/component-mutation-data.service';
import { ComponentMutation } from './entities/component-mutation.entity';

@Module({
  controllers: [
    DesignerWorkspaceController,
    ViewController,
    ComponentController,
    ShapeController,
    BlueprintComponentController
  ],
  imports: [
    TypeOrmModule.forFeature([
      DesignerWorkspace,
      View,
      Component,
      Shape,
      ShapeMutation,
      ComponentMutation,
      BlueprintComponent
    ]),
    forwardRef(() => ProjectModule),
    UserModule,
    VisualizerModule
  ],
  exports: [
    DesignerWorkspaceDataService,
    ViewDataService,
    ComponentDataService,
    ShapeDataService,
    ShapeMutationDataService,
    ComponentMutationDataService,
    BlueprintComponentDataService
  ],
  providers: [
    DesignerWorkspaceService,
    DesignerWorkspaceDataService,
    ViewService,
    ViewDataService,
    ComponentService,
    ComponentDataService,
    ShapeService,
    ShapeDataService,
    ShapeMutationDataService,
    ComponentMutationDataService,
    BlueprintComponentService,
    BlueprintComponentDataService
  ]
})
export class DesignerModule {
}
