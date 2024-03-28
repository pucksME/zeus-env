import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from '../entities/user.entity';
import { Repository } from 'typeorm';
import { DatabaseUtils, errorCodeTypes } from '../../../database.utils';

@Injectable()
export class UserDataService {

  constructor(
    @InjectRepository(User)
    private readonly userRepository: Repository<User>
  ) {
  }

  async save(userEntity: User): Promise<User> {
    try {
      return await this.userRepository.save(userEntity);
    } catch (error) {
      throw DatabaseUtils.mapErrorCodeToHttpException(
        error.code,
        { [errorCodeTypes.UNIQUE_VIOLATION]: 'The e-mail address is already taken' }
      );
    }
  }

  findOneByUuid(uuid: string): Promise<User | undefined> {
    return this.userRepository.findOne({ uuid });
  }

  findOneByEmail(email: string): Promise<User | undefined> {
    return this.userRepository.findOne({ email });
  }

}
