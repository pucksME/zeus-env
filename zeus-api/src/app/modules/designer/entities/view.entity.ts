import { Column, Entity, ManyToOne, OneToMany, PrimaryGeneratedColumn } from 'typeorm';
import { DesignerWorkspace } from './designer-workspace.entity';
import { Component } from './component.entity';
import { ViewType } from '../enums/view-type.enum';

@Entity()
export class View {

  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column({ nullable: true })
  name: string;

  @Column({
    type: 'enum',
    enum: ViewType
  })
  type: ViewType;

  @Column({type: 'float'})
  height: number;

  @Column({type: 'float'})
  width: number;

  @Column({type: 'float'})
  positionX: number;

  @Column({type: 'float'})
  positionY: number;

  @Column({default: false})
  isRoot: boolean;

  @Column()
  sorting: number;

  @OneToMany(
    type => Component,
    componentEntity => componentEntity.view,
    { cascade: true }
  )
  components: Component[];

  @ManyToOne(
    type => DesignerWorkspace,
    workspaceEntity => workspaceEntity.views,
    { onDelete: 'CASCADE' }
  )
  workspace: DesignerWorkspace;

}
