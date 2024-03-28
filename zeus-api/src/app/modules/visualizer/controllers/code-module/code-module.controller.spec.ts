import { Test, TestingModule } from '@nestjs/testing';
import { CodeModuleController } from './code-module.controller';

describe('CodeModuleController', () => {
  let controller: CodeModuleController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [CodeModuleController],
    }).compile();

    controller = module.get<CodeModuleController>(CodeModuleController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
