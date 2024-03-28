import { BeforeInsert, Column, Entity, OneToMany, PrimaryGeneratedColumn } from 'typeorm';
import { hashSync, genSaltSync, compareSync } from 'bcrypt';
import { UserProjectAssignment } from './user-project-assignment.entity';
import { CodeModule } from '../../visualizer/entities/code-module.entity';

@Entity()
export class User {

  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column({ unique: true })
  email: string;

  @Column()
  password: string;

  @Column()
  firstName: string;

  @Column()
  lastName: string;

  @OneToMany(
    type => UserProjectAssignment,
    userProjectAssignmentEntity => userProjectAssignmentEntity.user,
    { cascade: true, onDelete: 'CASCADE' }
  )
  projectAssignments: UserProjectAssignment[];

  @OneToMany(
    type => CodeModule,
    codeModuleEntity => codeModuleEntity.user,
    {nullable: true, cascade: true, onDelete: 'CASCADE'}
  )
  codeModules: CodeModule[];

  // https://github.com/kelektiv/node.bcrypt.js
  @BeforeInsert()
  private hashPassword() {
    this.password = hashSync(this.password, genSaltSync(10));
  }

  verifyPassword(password: string): boolean {
    return compareSync(password, this.password);
  }

}
