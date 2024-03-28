import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Project } from '../entities/project.entity';
import { DeleteResult, Repository } from 'typeorm';
import { ExportedProject } from '../entities/exported-project.entity';

@Injectable()
export class ProjectDataService {

  constructor(
    @InjectRepository(Project)
    private readonly projectRepository: Repository<Project>,
    @InjectRepository(ExportedProject)
    private readonly exportedProjectRepository: Repository<ExportedProject>
  ) {
  }

  save(projectEntity: Project): Promise<Project> {
    return this.projectRepository.save(projectEntity);
  }

  findOne(projectUuid: string, relations: string[] = []): Promise<Project | undefined> {
    return this.projectRepository.findOne({ uuid: projectUuid }, { relations });
  }

  delete(projectUuid: string): Promise<DeleteResult> {
    return this.projectRepository.delete(projectUuid);
  }

  saveExportedProject(exportedProjectEntity: ExportedProject): Promise<ExportedProject> {
    return this.exportedProjectRepository.save(exportedProjectEntity);
  }

  deleteExportedProject(exportedProjectUuid: string): Promise<DeleteResult> {
    return this.exportedProjectRepository.delete(exportedProjectUuid);
  }

  findOneExportedProject(exportedProjectUuid: string, relations: string[] = []): Promise<ExportedProject> {
    return this.exportedProjectRepository.findOne({uuid: exportedProjectUuid}, {relations});
  }

}
