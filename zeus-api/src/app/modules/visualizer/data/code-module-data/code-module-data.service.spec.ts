import { Test, TestingModule } from '@nestjs/testing';
import { CodeModuleDataService } from './code-module-data.service';

describe('CodeModuleDataService', () => {
  let service: CodeModuleDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CodeModuleDataService],
    }).compile();

    service = module.get<CodeModuleDataService>(CodeModuleDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
