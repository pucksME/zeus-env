import { Column, Entity, JoinColumn, OneToMany, OneToOne, PrimaryGeneratedColumn } from 'typeorm';
import { DesignerWorkspace } from '../../designer/entities/designer-workspace.entity';
import { UserProjectAssignment } from '../../user/entities/user-project-assignment.entity';
import { CodeModule } from '../../visualizer/entities/code-module.entity';
import { ExportedProject } from './exported-project.entity';

@Entity()
export class Project {
  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column()
  name: string;

  @Column({ nullable: true })
  description: string;

  @OneToMany(
    type => UserProjectAssignment,
    userProjectAssignmentEntity => userProjectAssignmentEntity.project,
    { cascade: true }
  )
  userAssignments: UserProjectAssignment[];

  @OneToOne(
    type => DesignerWorkspace,
    workspaceEntity => workspaceEntity.project,
    { nullable: true, cascade: true, onDelete: 'CASCADE' }
  )
  @JoinColumn()
  designerWorkspace: DesignerWorkspace | null;

  @OneToMany(
    type => CodeModule,
    moduleEntity => moduleEntity.project,
    {nullable: true, cascade: true}
  )
  codeModules: CodeModule[];

  @OneToOne(
    type => ExportedProject,
    exportedProjectEntity => exportedProjectEntity.project,
    {cascade: true, onDelete: 'SET NULL'}
  )
  @JoinColumn()
  exportedProject: ExportedProject;
}
