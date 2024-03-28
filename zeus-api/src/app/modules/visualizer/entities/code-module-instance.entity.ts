import { Column, Entity, ManyToOne, PrimaryGeneratedColumn } from 'typeorm';
import { CodeModule } from './code-module.entity';
import { VisualizerWorkspace } from './visualizer-workspace.entity';

@Entity()
export class CodeModuleInstance {
  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column({nullable: true})
  flowDescription: string | null;

  @Column({type: 'float'})
  positionX: number;

  @Column({type: 'float'})
  positionY: number;

  @ManyToOne(
    type => CodeModule,
    codeModuleEntity => codeModuleEntity.instances,
    {onDelete: 'CASCADE'}
  )
  module: CodeModule;

  @ManyToOne(
    type => VisualizerWorkspace,
    visualizerWorkspaceEntity => visualizerWorkspaceEntity.codeModuleInstances
  )
  workspace: VisualizerWorkspace;
}
