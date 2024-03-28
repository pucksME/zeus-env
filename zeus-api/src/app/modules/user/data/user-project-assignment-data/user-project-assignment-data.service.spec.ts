import { Test, TestingModule } from '@nestjs/testing';
import { UserProjectAssignmentDataService } from './user-project-assignment-data.service';

describe('UserProjectAssignmentDataService', () => {
  let service: UserProjectAssignmentDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [UserProjectAssignmentDataService],
    }).compile();

    service = module.get<UserProjectAssignmentDataService>(UserProjectAssignmentDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
