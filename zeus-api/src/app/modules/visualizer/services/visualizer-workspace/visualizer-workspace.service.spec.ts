import { Test, TestingModule } from '@nestjs/testing';
import { VisualizerWorkspaceService } from './visualizer-workspace.service';

describe('DesignerWorkspaceService', () => {
  let service: VisualizerWorkspaceService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [VisualizerWorkspaceService],
    }).compile();

    service = module.get<VisualizerWorkspaceService>(VisualizerWorkspaceService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
