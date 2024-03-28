import { Test, TestingModule } from '@nestjs/testing';
import { ShapeDataService } from './shape-data.service';

describe('ShapeDataService', () => {
  let service: ShapeDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ShapeDataService],
    }).compile();

    service = module.get<ShapeDataService>(ShapeDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
