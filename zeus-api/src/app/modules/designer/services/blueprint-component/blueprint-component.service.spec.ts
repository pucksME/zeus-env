import { Test, TestingModule } from '@nestjs/testing';
import { BlueprintComponentService } from './blueprint-component.service';

describe('BlueprintComponentService', () => {
  let service: BlueprintComponentService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [BlueprintComponentService],
    }).compile();

    service = module.get<BlueprintComponentService>(BlueprintComponentService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
