import { User } from './entities/user.entity';
import { UserDto } from './dtos/user.dto';

export abstract class UserUtils {

  static buildUserDto(userEntity: User): UserDto {
    return {
      uuid: userEntity.uuid,
      email: userEntity.email,
      firstName: userEntity.firstName,
      lastName: userEntity.lastName
    }
  }

}
