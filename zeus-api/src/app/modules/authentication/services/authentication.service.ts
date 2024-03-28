import { Injectable, InternalServerErrorException } from '@nestjs/common';
import { UserDataService } from '../../user/data/user-data.service';
import { TokenUserDto } from '../dtos/token-user.dto';
import { AuthenticationUtils } from '../authentication.utils';
import { JwtService } from '@nestjs/jwt';
import { JwtTokenDto } from '../dtos/jwt-token.dto';

@Injectable()
export class AuthenticationService {

  constructor(
    private readonly userDataService: UserDataService,
    private readonly jwtService: JwtService
  ) {
  }

  // https://docs.nestjs.com/security/authentication#authentication
  async validateCredentials(email: string, password: string): Promise<TokenUserDto | null> {
    const user = await this.userDataService.findOneByEmail(email);
    return (!user || !user.verifyPassword(password)) ? null : AuthenticationUtils.buildTokenUserDto(user);
  }

  // https://docs.nestjs.com/security/authentication#jwt-functionality
  async generateJwtToken(user: TokenUserDto): Promise<JwtTokenDto> {

    if (!user) {
      throw new InternalServerErrorException('User object was not present when generating a JWT token');
    }

    return {
      token: this.jwtService.sign(user)
    };
  }

}
