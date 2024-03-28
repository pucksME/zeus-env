import { Test, TestingModule } from '@nestjs/testing';
import { BlueprintComponentController } from './blueprint-component.controller';

describe('BlueprintComponentController', () => {
  let controller: BlueprintComponentController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [BlueprintComponentController],
    }).compile();

    controller = module.get<BlueprintComponentController>(BlueprintComponentController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
