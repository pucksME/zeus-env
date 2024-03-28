import { Injectable, Logger } from '@nestjs/common';
import { CreateUserDto } from '../dtos/create-user.dto';
import { UserDto } from '../dtos/user.dto';
import { UserDataService } from '../data/user-data.service';
import { User } from '../entities/user.entity';
import { UserUtils } from '../user.utils';

export enum findModes {
  UUID,
  E_MAIL
}

@Injectable()
export class UserService {

  constructor(private readonly userDataService: UserDataService) {
  }

  async save(createUserDto: CreateUserDto): Promise<UserDto> {
    // Decided against using the entity's .create function since
    // doing it manually instead is more stable to
    // potential dto changes
    const userEntity: User = new User();
    userEntity.email = createUserDto.email;
    userEntity.firstName = createUserDto.firstName;
    userEntity.lastName = createUserDto.lastName;
    userEntity.password = createUserDto.password;

    return UserUtils.buildUserDto(await this.userDataService.save(userEntity));
  }

  async find(identifier: string, findMode: findModes): Promise<UserDto | undefined> {
    switch (findMode) {
      case findModes.UUID:
        return this.userDataService.findOneByUuid(identifier);
      case findModes.E_MAIL:
        return this.userDataService.findOneByEmail(identifier);
      default:
        Logger.warn(`Can't find user with invalid find mode "${findMode}"`);
        return undefined;
    }
  }

}
