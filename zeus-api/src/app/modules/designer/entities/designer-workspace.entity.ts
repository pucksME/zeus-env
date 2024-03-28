import { Column, Entity, OneToMany, OneToOne, PrimaryGeneratedColumn } from 'typeorm';
import { View } from './view.entity';
import { Project } from '../../project/entities/project.entity';
import { WorkspaceType } from '../enums/workspace-type.enum';
import { BlueprintComponent } from './blueprint-component.entity';

@Entity()
export class DesignerWorkspace {

  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column({ nullable: true })
  name: string;

  @Column({
    type: 'enum',
    enum: WorkspaceType
  })
  type: WorkspaceType;

  @OneToOne(
    type => Project,
    projectEntity => projectEntity.designerWorkspace
  )
  project: Project;

  @OneToMany(
    type => View,
    viewEntity => viewEntity.workspace,
    { cascade: true }
  )
  views: View[];

  @OneToMany(
    type => BlueprintComponent,
    blueprintComponentEntity => blueprintComponentEntity.workspace,
    {cascade: true}
  )
  blueprintComponents: BlueprintComponent[]

}
