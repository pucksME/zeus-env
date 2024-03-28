import { forwardRef, Module } from '@nestjs/common';
import { VisualizerWorkspaceController } from './controllers/visualizer-workspace/visualizer-workspace.controller';
import { DesignerModule } from '../designer/designer.module';
import { VisualizerWorkspaceService } from './services/visualizer-workspace/visualizer-workspace.service';
import { VisualizerWorkspaceDataService } from './data/visualizer-workspace-data/visualizer-workspace-data.service';
import { UserModule } from '../user/user.module';
import { TypeOrmModule } from '@nestjs/typeorm';
import { VisualizerWorkspace } from './entities/visualizer-workspace.entity';
import { Component } from '../designer/entities/component.entity';
import { ProjectModule } from '../project/project.module';
import { CodeModuleController } from './controllers/code-module/code-module.controller';
import { CodeModule } from './entities/code-module.entity';
import { CodeModuleService } from './services/code-module/code-module.service';
import { CodeModuleDataService } from './data/code-module-data/code-module-data.service';
import { CodeModuleInstanceController } from './controllers/code-module-instance/code-module-instance.controller';
import { CodeModuleInstance } from './entities/code-module-instance.entity';
import { CodeModuleInstanceService } from './services/code-module-instance/code-module-instance.service';
import { CodeModuleInstanceDataService } from './data/code-module-instance-data/code-module-instance-data.service';
import {
  CodeModuleInstancesConnectionDataService
} from './data/code-module-instances-connection-data/code-module-instances-connection-data.service';
import { CodeModuleInstancesConnection } from './entities/code-module-instances-connection.entity';

@Module({
  controllers: [
    VisualizerWorkspaceController,
    CodeModuleController,
    CodeModuleInstanceController
  ],
  imports: [
    TypeOrmModule.forFeature([
      VisualizerWorkspace,
      Component,
      CodeModule,
      CodeModuleInstance,
      CodeModuleInstancesConnection
    ]),
    forwardRef(() => ProjectModule),
    UserModule,
    forwardRef(() => DesignerModule)
  ],
  providers: [
    VisualizerWorkspaceService,
    VisualizerWorkspaceDataService,
    CodeModuleService,
    CodeModuleDataService,
    CodeModuleInstanceService,
    CodeModuleInstanceDataService,
    CodeModuleInstancesConnectionDataService
  ],
  exports: [
    CodeModuleInstanceDataService
  ]
})
export class VisualizerModule {}
