import {
  Column,
  Entity,
  JoinColumn,
  ManyToOne,
  OneToMany,
  OneToOne,
  PrimaryGeneratedColumn,
  Tree,
  TreeChildren, TreeParent
} from 'typeorm';
import { Shape } from './shape.entity';
import { View } from './view.entity';
import { BlueprintComponent } from './blueprint-component.entity';
import { ShapeMutation } from './shape-mutation.entity';
import { VisualizerWorkspace } from '../../visualizer/entities/visualizer-workspace.entity';
import { ComponentMutation } from './component-mutation.entity';

@Entity()
@Tree('closure-table')
export class Component {

  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column({ nullable: true })
  name: string;

  @Column({ type: 'float' })
  positionX: number;

  @Column({ type: 'float' })
  positionY: number;

  @Column()
  sorting: number;

  @ManyToOne(
    type => View,
    viewEntity => viewEntity.components,
    { onDelete: 'CASCADE' }
  )
  view: View;

  @TreeParent({onDelete: 'CASCADE'})
  parent: Component;

  @TreeChildren({cascade: true})
  children: Component[];

  @OneToMany(
    type => Shape,
    shapeEntity => shapeEntity.component,
    { cascade: true }
  )
  shapes: Shape[];

  @OneToMany(
    type => ComponentMutation,
    componentMutationEntity => componentMutationEntity.component,
    {cascade: true}
  )
  componentMutations: ComponentMutation[];

  @OneToMany(
    type => ShapeMutation,
    shapeMutationsEntity => shapeMutationsEntity.component,
    {cascade: true}
  )
  shapeMutations: ShapeMutation[];

  @ManyToOne(
    type => BlueprintComponent,
    blueprintComponentEntity => blueprintComponentEntity.referencingComponents,
    {onDelete: 'CASCADE'}
  )
  blueprintComponent: BlueprintComponent;

  @OneToOne(
    type => VisualizerWorkspace,
    workspaceEntity => workspaceEntity.component,
    {nullable: true, cascade: true, onDelete: 'SET NULL'}
  )
  @JoinColumn()
  workspace: VisualizerWorkspace;

}
