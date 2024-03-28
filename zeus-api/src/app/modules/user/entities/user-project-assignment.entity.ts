import { Column, Entity, ManyToOne, PrimaryGeneratedColumn } from 'typeorm';
import { User } from './user.entity';
import { Project } from '../../project/entities/project.entity';
import { DesignerPermissionToken } from '../../designer/enums/designer-permission-token.enum';
import { ProjectPermissionToken } from '../../project/enums/project-permission-token.enum';
import { VisualizerPermissionToken } from '../../visualizer/enums/visualizer-permission-token.enum';

@Entity()
export class UserProjectAssignment {

  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column({
    type: 'enum',
    enum: ProjectPermissionToken
  })
  projectPermission: ProjectPermissionToken;

  @Column({
    type: 'enum',
    enum: DesignerPermissionToken
  })
  designerPermission: DesignerPermissionToken;

  @Column({
    type: 'enum',
    enum: VisualizerPermissionToken
  })
  visualizerPermission: VisualizerPermissionToken;

  @Column({ type: 'float' })
  designerPositionX: number;

  @Column({ type: 'float' })
  designerPositionY: number;

  @Column({ type: 'float' })
  designerScale: number;

  @ManyToOne(
    type => User,
    user => user.projectAssignments
  )
  user: User;

  @ManyToOne(
    type => Project,
    project => project.userAssignments,
    { onDelete: 'CASCADE' }
  )
  project: Project;

}
