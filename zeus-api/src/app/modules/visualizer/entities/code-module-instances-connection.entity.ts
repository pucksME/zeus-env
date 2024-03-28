import { Column, Entity, ManyToOne, PrimaryGeneratedColumn } from 'typeorm';
import { VisualizerWorkspace } from './visualizer-workspace.entity';

@Entity()
export class CodeModuleInstancesConnection {
  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column()
  inputCodeModuleInstanceName: string;

  @Column()
  inputCodeModuleInstancePortName: string;

  @Column()
  outputCodeModuleInstanceName: string;

  @Column()
  outputCodeModuleInstancePortName: string;

  @ManyToOne(
    type => VisualizerWorkspace,
    visualizerWorkspaceEntity => visualizerWorkspaceEntity.codeModuleInstancesConnections,
    {onDelete: 'CASCADE'}
  )
  visualizerWorkspace: VisualizerWorkspace;
}
