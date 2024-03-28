import {
  AuthenticateUserDto,
  AuthenticationApi,
  CreateUserDto,
  JwtTokenDto,
  UserApi,
  UserDto
} from '../../../../gen/api-client';
import { AppUtils } from '../../../app.utils';

export abstract class UserService {

  private static userApi = new UserApi(AppUtils.getApiConfiguration());
  private static authenticationApi = new AuthenticationApi(AppUtils.getApiConfiguration());

  static async save(createUserDto: CreateUserDto): Promise<UserDto> {
    return (await UserService.userApi.userControllerSave(createUserDto)).data;
  }

  static async authenticate(authenticateUserDto: AuthenticateUserDto): Promise<JwtTokenDto | null> {
    try {
      return (await UserService.authenticationApi.authenticationControllerAuthenticate(authenticateUserDto)).data;
    } catch (error) {
      return null;
    }
  }

}
