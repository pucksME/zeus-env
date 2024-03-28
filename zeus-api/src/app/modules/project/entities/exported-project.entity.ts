import { Column, Entity, OneToOne, PrimaryGeneratedColumn } from 'typeorm';
import { Project } from './project.entity';
import { ExportedFile } from '../interfaces/exported-file.interface';
import { ExportTarget } from '../enums/export-target.enum';
import { Error } from '../interfaces/error.interface';

@Entity()
export class ExportedProject {
  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column('json')
  exportedFiles: ExportedFile[];

  @Column('json')
  exportedErrors: Error[];

  @Column({
    type: 'enum',
    enum: ExportTarget
  })
  exportTarget: ExportTarget;

  @OneToOne(
    type => Project,
    projectEntity => projectEntity.exportedProject
  )
  project: Project;
}
