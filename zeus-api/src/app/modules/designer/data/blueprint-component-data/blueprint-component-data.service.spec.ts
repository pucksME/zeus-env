import { Test, TestingModule } from '@nestjs/testing';
import { BlueprintComponentDataService } from './blueprint-component-data.service';

describe('BlueprintComponentDataService', () => {
  let service: BlueprintComponentDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [BlueprintComponentDataService],
    }).compile();

    service = module.get<BlueprintComponentDataService>(BlueprintComponentDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
