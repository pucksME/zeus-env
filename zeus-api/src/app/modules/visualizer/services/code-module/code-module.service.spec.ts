import { Test, TestingModule } from '@nestjs/testing';
import { CodeModuleService } from './code-module.service';

describe('CodeModuleService', () => {
  let service: CodeModuleService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CodeModuleService],
    }).compile();

    service = module.get<CodeModuleService>(CodeModuleService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
