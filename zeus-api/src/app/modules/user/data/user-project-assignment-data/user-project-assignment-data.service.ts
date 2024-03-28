import { Injectable } from '@nestjs/common';
import { Repository } from 'typeorm';
import { UserProjectAssignment } from '../../entities/user-project-assignment.entity';
import { InjectRepository } from '@nestjs/typeorm';

@Injectable()
export class UserProjectAssignmentDataService {

  constructor(
    @InjectRepository(UserProjectAssignment)
    private readonly userProjectAssignmentRepository: Repository<UserProjectAssignment>
  ) {
  }

  save(userProjectAssignmentEntity: UserProjectAssignment): Promise<UserProjectAssignment> {
    return this.userProjectAssignmentRepository.save(userProjectAssignmentEntity);
  }

  find(userUuid: string, relations: string[] = []): Promise<UserProjectAssignment[]> {
    return this.userProjectAssignmentRepository.find({ where: { user: { uuid: userUuid } }, relations });
  }

}
