import { Injectable } from '@nestjs/common';
import { DeleteResult, In, Repository } from 'typeorm';
import { CodeModuleInstance } from '../../entities/code-module-instance.entity';
import { InjectRepository } from '@nestjs/typeorm';

@Injectable()
export class CodeModuleInstanceDataService {

  constructor(
    @InjectRepository(CodeModuleInstance)
    private readonly codeModuleInstanceRepository: Repository<CodeModuleInstance>
  ) {
  }

  save(codeModuleInstanceEntity: CodeModuleInstance): Promise<CodeModuleInstance> {
    return this.codeModuleInstanceRepository.save(codeModuleInstanceEntity);
  }

  saveMany(codeModuleInstanceEntities: CodeModuleInstance[]): Promise<CodeModuleInstance[]> {
    return this.codeModuleInstanceRepository.save(codeModuleInstanceEntities);
  }

  find(codeModuleInstanceUuid: string, relations: string[] = []): Promise<CodeModuleInstance | undefined> {
    return this.codeModuleInstanceRepository.findOne({uuid: codeModuleInstanceUuid}, {relations});
  }

  findMany(codeModuleInstanceUuids: string[], relations: string[] = []): Promise<CodeModuleInstance[]> {
    return this.codeModuleInstanceRepository.find({where: {uuid: In(codeModuleInstanceUuids)}, relations});
  }

  deleteMany(codeModuleInstanceUuids: string[]): Promise<DeleteResult> {
    return this.codeModuleInstanceRepository.delete(codeModuleInstanceUuids);
  }

}
