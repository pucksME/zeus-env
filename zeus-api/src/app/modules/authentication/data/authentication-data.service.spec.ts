import { Test, TestingModule } from '@nestjs/testing';
import { AuthenticationDataService } from './authentication-data.service';

describe('AuthenticationDataService', () => {
  let service: AuthenticationDataService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [AuthenticationDataService],
    }).compile();

    service = module.get<AuthenticationDataService>(AuthenticationDataService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
