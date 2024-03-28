import { Test, TestingModule } from '@nestjs/testing';
import { CodeModuleInstanceController } from './code-module-instance.controller';

describe('CodeModuleInstanceController', () => {
  let controller: CodeModuleInstanceController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [CodeModuleInstanceController],
    }).compile();

    controller = module.get<CodeModuleInstanceController>(CodeModuleInstanceController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
