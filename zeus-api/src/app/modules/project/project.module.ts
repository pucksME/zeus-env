import { forwardRef, Module } from '@nestjs/common';
import { ProjectController } from './controllers/project.controller';
import { ProjectService } from './services/project.service';
import { ProjectDataService } from './data/project-data.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Project } from './entities/project.entity';
import { DesignerModule } from '../designer/designer.module';
import { UserModule } from '../user/user.module';
import { VisualizerModule } from '../visualizer/visualizer.module';
import { ExportedProject } from './entities/exported-project.entity';

@Module({
  controllers: [ProjectController],
  imports: [
    TypeOrmModule.forFeature([Project, ExportedProject]),
    forwardRef(() => DesignerModule),
    UserModule,
    forwardRef(() => VisualizerModule)
  ],
  exports: [ProjectDataService],
  providers: [ProjectService, ProjectDataService]
})
export class ProjectModule {
}
