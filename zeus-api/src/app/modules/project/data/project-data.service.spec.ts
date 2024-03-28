import { Test, TestingModule } from '@nestjs/testing';
import { ProjectDataService } from './project-data.service';

describe('ProjectDataService', () => {
  let service: ProjectDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ProjectDataService],
    }).compile();

    service = module.get<ProjectDataService>(ProjectDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
