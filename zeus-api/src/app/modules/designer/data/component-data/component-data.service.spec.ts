import { Test, TestingModule } from '@nestjs/testing';
import { ComponentDataService } from './component-data.service';

describe('ComponentDataService', () => {
  let service: ComponentDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ComponentDataService],
    }).compile();

    service = module.get<ComponentDataService>(ComponentDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
