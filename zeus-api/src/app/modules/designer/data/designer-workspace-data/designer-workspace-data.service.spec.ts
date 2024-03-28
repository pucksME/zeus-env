import { Test, TestingModule } from '@nestjs/testing';
import { DesignerWorkspaceDataService } from './designer-workspace-data.service';

describe('DesignerWorkspaceDataService', () => {
  let service: DesignerWorkspaceDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [DesignerWorkspaceDataService],
    }).compile();

    service = module.get<DesignerWorkspaceDataService>(DesignerWorkspaceDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
