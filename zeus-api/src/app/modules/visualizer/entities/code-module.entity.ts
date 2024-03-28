import { Column, Entity, ManyToOne, OneToMany, PrimaryGeneratedColumn } from 'typeorm';
import { Project } from '../../project/entities/project.entity';
import { CodeModuleInstance } from './code-module-instance.entity';
import { User } from '../../user/entities/user.entity';
import { CodeModuleInstancesConnection } from './code-module-instances-connection.entity';

@Entity()
export class CodeModule {

  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column()
  code: string;

  @Column()
  global: boolean;

  @Column()
  sorting: number;

  @ManyToOne(
    type => User,
    userEntity => userEntity.codeModules
  )
  user: User;

  @ManyToOne(
    type => Project,
    projectEntity => projectEntity.codeModules,
    {onDelete: "CASCADE"}
  )
  project: Project;

  @OneToMany(
    type => CodeModuleInstance,
    codeModuleInstanceEntity => codeModuleInstanceEntity.module,
    {cascade: true}
  )
  instances: CodeModuleInstance[];



}
