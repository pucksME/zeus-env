import { Test, TestingModule } from '@nestjs/testing';
import { DesignerWorkspaceService } from './designer-workspace.service';

describe('DesignerWorkspaceService', () => {
  let service: DesignerWorkspaceService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [DesignerWorkspaceService],
    }).compile();

    service = module.get<DesignerWorkspaceService>(DesignerWorkspaceService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
