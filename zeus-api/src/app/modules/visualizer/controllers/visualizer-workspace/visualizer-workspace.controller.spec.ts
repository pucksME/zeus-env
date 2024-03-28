import { Test, TestingModule } from '@nestjs/testing';
import { VisualizerWorkspaceController } from './visualizer-workspace.controller';

describe('DesignerWorkspaceController', () => {
  let controller: VisualizerWorkspaceController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [VisualizerWorkspaceController],
    }).compile();

    controller = module.get<VisualizerWorkspaceController>(VisualizerWorkspaceController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
