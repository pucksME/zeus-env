import { Module } from '@nestjs/common';

import { AppController } from './controllers/app.controller';
import { AppService } from './services/app.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from './modules/user/entities/user.entity';
import { UserModule } from './modules/user/user.module';
import { AuthenticationModule } from './modules/authentication/authentication.module';
import { DesignerModule } from './modules/designer/designer.module';
import { Component } from './modules/designer/entities/component.entity';
import { Shape } from './modules/designer/entities/shape.entity';
import { ShapeMutation } from './modules/designer/entities/shape-mutation.entity';
import { DesignerWorkspace } from './modules/designer/entities/designer-workspace.entity';
import { View } from './modules/designer/entities/view.entity';
import { ProjectModule } from './modules/project/project.module';
import { Project } from './modules/project/entities/project.entity';
import { UserProjectAssignment } from './modules/user/entities/user-project-assignment.entity';
import { BlueprintComponent } from './modules/designer/entities/blueprint-component.entity';
import { VisualizerWorkspace } from './modules/visualizer/entities/visualizer-workspace.entity';
import { CodeModule } from './modules/visualizer/entities/code-module.entity';
import { CodeModuleInstance } from './modules/visualizer/entities/code-module-instance.entity';
import { VisualizerModule } from './modules/visualizer/visualizer.module';
import { ComponentMutation } from './modules/designer/entities/component-mutation.entity';
import { CodeModuleInstancesConnection } from './modules/visualizer/entities/code-module-instances-connection.entity';
import { ExportedProject } from './modules/project/entities/exported-project.entity';

// typeorm integration: https://docs.nestjs.com/techniques/database
// typeorm connection options: https://typeorm.io/#/connection-options
@Module({
  imports: [
    TypeOrmModule.forRoot({
      type: 'postgres',
      host: 'localhost',
      port: 5432,
      username: 'zeus',
      password: 'zeus',
      database: 'zeus',
      entities: [
        User,
        Project,
        ExportedProject,
        UserProjectAssignment,
        DesignerWorkspace,
        View,
        Component,
        BlueprintComponent,
        Shape,
        ComponentMutation,
        ShapeMutation,
        VisualizerWorkspace,
        CodeModule,
        CodeModuleInstance,
        CodeModuleInstancesConnection
      ],
      synchronize: true,
      logging: ['query']
    }),
    AuthenticationModule,
    UserModule,
    ProjectModule,
    DesignerModule,
    VisualizerModule
  ],
  controllers: [AppController],
  providers: [AppService]
})
export class AppModule {
}
