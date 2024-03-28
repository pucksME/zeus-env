import { Test, TestingModule } from '@nestjs/testing';
import { DesignerWorkspaceController } from './designer-workspace.controller';

describe('DesignerWorkspaceController', () => {
  let controller: DesignerWorkspaceController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [DesignerWorkspaceController],
    }).compile();

    controller = module.get<DesignerWorkspaceController>(DesignerWorkspaceController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
