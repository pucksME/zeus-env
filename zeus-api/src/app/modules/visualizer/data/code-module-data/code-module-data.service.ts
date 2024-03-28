import { Injectable } from '@nestjs/common';
import { CodeModule } from '../../entities/code-module.entity';
import { InjectRepository } from '@nestjs/typeorm';
import { DeleteResult, Repository } from 'typeorm';

@Injectable()
export class CodeModuleDataService {

  constructor(
    @InjectRepository(CodeModule)
    private readonly codeModuleRepository: Repository<CodeModule>
  ) {
  }

  save(codeModuleEntity: CodeModule): Promise<CodeModule> {
    return this.codeModuleRepository.save(codeModuleEntity);
  }

  saveMany(codeModuleEntities: CodeModule[]): Promise<CodeModule[]> {
    return this.codeModuleRepository.save(codeModuleEntities);
  }

  findManySystem(): Promise<CodeModule[]> {
    return this.codeModuleRepository.find({where: {user: null}});
  }

  findManyProject(projectUuid: string): Promise<CodeModule[]> {
    return this.codeModuleRepository.find({where: {project: {uuid: projectUuid}}});
  }

  find(codeModuleUuid: string, relations: string[] = []): Promise<CodeModule | undefined> {
    return this.codeModuleRepository.findOne({uuid: codeModuleUuid}, {relations});
  }

  delete(codeModuleUuid: string): Promise<DeleteResult> {
    return this.codeModuleRepository.delete(codeModuleUuid);
  }

}
