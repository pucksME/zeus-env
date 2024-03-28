import { User } from '../user/entities/user.entity';
import { TokenUserDto } from './dtos/token-user.dto';

export abstract class AuthenticationUtils {

  static buildTokenUserDto(userEntity: User): TokenUserDto {
    return {
      sub: userEntity.uuid,
      email: userEntity.email,
      firstName: userEntity.firstName,
      lastName: userEntity.lastName
    };
  }

}
