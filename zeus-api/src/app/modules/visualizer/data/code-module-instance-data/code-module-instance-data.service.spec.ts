import { Test, TestingModule } from '@nestjs/testing';
import { CodeModuleInstanceDataService } from './code-module-instance-data.service';

describe('CodeModuleInstanceDataService', () => {
  let service: CodeModuleInstanceDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CodeModuleInstanceDataService],
    }).compile();

    service = module.get<CodeModuleInstanceDataService>(CodeModuleInstanceDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
