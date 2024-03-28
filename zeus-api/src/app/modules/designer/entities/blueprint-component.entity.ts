import { Column, Entity, ManyToOne, OneToMany, PrimaryGeneratedColumn, Tree, TreeChildren, TreeParent } from 'typeorm';
import { Shape } from './shape.entity';
import { Component } from './component.entity';
import { DesignerWorkspace } from './designer-workspace.entity';
import { ComponentMutation } from './component-mutation.entity';

@Entity()
@Tree('closure-table')
export class BlueprintComponent {

  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column({nullable: true})
  name: string;

  @Column({type: 'float'})
  positionX: number;

  @Column({type: 'float'})
  positionY: number;

  @Column()
  sorting: number;

  @TreeParent({onDelete: 'CASCADE'})
  parent: BlueprintComponent;

  @TreeChildren({cascade: true})
  children: BlueprintComponent[];

  @OneToMany(
    type => Shape,
    shapeEntity => shapeEntity.blueprintComponent,
    {cascade: true, onDelete: 'CASCADE'}
  )
  shapes: Shape[];

  @OneToMany(
    type => ComponentMutation,
    componentMutationEntity => componentMutationEntity.blueprintComponent,
    {cascade: true}
  )
  componentMutations: ComponentMutation[];

  @OneToMany(
    type => Component,
    componentEntity => componentEntity.blueprintComponent,
    {cascade: true, onDelete: 'CASCADE'}
  )
  referencingComponents: Component[];

  @ManyToOne(
    type => DesignerWorkspace,
    workspaceEntity => workspaceEntity.blueprintComponents,
    { onDelete: 'CASCADE' }
  )
  workspace: DesignerWorkspace;

}
