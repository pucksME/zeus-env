import { PassportStrategy } from '@nestjs/passport';
import { Strategy } from 'passport-local';
import { Injectable, UnauthorizedException } from '@nestjs/common';
import { AuthenticationService } from '../services/authentication.service';

// https://docs.nestjs.com/security/authentication#implementing-passport-local
@Injectable()
export class LocalStrategy extends PassportStrategy(Strategy) {

  constructor(private readonly authenticationService: AuthenticationService) {
    // https://docs.nestjs.com/security/authentication#customize-passport
    super({
      usernameField: 'email',
      passwordField: 'password'
    });
  }

  // If successful, the user will be added to the request object
  async validate(username: string, password: string): Promise<any> {
    const user = await this.authenticationService.validateCredentials(username, password);

    if (!user) {
      throw new UnauthorizedException();
    }

    return user;
  }

}
