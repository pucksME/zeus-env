import { Test, TestingModule } from '@nestjs/testing';
import { VisualizerWorkspaceDataService } from './visualizer-workspace-data.service';

describe('DesignerWorkspaceDataService', () => {
  let service: VisualizerWorkspaceDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [VisualizerWorkspaceDataService],
    }).compile();

    service = module.get<VisualizerWorkspaceDataService>(VisualizerWorkspaceDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
