import { Injectable } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';

// https://docs.nestjs.com/security/authentication#login-route
@Injectable()
export class LocalAuthenticationGuard extends AuthGuard('local') {
}
