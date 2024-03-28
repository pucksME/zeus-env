import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { CodeModuleInstancesConnection } from '../../entities/code-module-instances-connection.entity';
import { DeleteResult, Repository } from 'typeorm';

@Injectable()
export class CodeModuleInstancesConnectionDataService {
  constructor(
    @InjectRepository(CodeModuleInstancesConnection)
    private readonly codeModulesConnectionsRepository: Repository<CodeModuleInstancesConnection>
  ) {
  }

  save(codeModulesConnectionsEntity: CodeModuleInstancesConnection): Promise<CodeModuleInstancesConnection> {
    return this.codeModulesConnectionsRepository.save(codeModulesConnectionsEntity);
  }

  delete(codeModuleInstanceConnectionUuid: string): Promise<DeleteResult> {
    return this.codeModulesConnectionsRepository.delete(codeModuleInstanceConnectionUuid);
  }
}
