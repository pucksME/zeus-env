import { Test, TestingModule } from '@nestjs/testing';
import { ShapeMutationDataService } from './shape-mutation-data.service';

describe('ShapeMutationDataService', () => {
  let service: ShapeMutationDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ShapeMutationDataService],
    }).compile();

    service = module.get<ShapeMutationDataService>(ShapeMutationDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
