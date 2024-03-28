import { Test, TestingModule } from '@nestjs/testing';
import { CodeModuleInstancesConnectionDataService } from './code-module-instances-connection-data.service';

describe('CodeModuleInstancesConnectionDataService', () => {
  let service: CodeModuleInstancesConnectionDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CodeModuleInstancesConnectionDataService],
    }).compile();

    service = module.get<CodeModuleInstancesConnectionDataService>(CodeModuleInstancesConnectionDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
