import { Test, TestingModule } from '@nestjs/testing';
import { ComponentMutationDataService } from './component-mutation-data.service';

describe('ComponentMutationDataService', () => {
  let service: ComponentMutationDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ComponentMutationDataService],
    }).compile();

    service = module.get<ComponentMutationDataService>(ComponentMutationDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
