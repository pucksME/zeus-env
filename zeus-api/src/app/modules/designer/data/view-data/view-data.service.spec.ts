import { Test, TestingModule } from '@nestjs/testing';
import { ViewDataService } from './view-data.service';

describe('ViewDataService', () => {
  let service: ViewDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ViewDataService],
    }).compile();

    service = module.get<ViewDataService>(ViewDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
