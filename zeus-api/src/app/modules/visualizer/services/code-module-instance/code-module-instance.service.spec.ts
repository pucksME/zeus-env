import { Test, TestingModule } from '@nestjs/testing';
import { CodeModuleInstanceService } from './code-module-instance.service';

describe('CodeModuleInstanceService', () => {
  let service: CodeModuleInstanceService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CodeModuleInstanceService],
    }).compile();

    service = module.get<CodeModuleInstanceService>(CodeModuleInstanceService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
