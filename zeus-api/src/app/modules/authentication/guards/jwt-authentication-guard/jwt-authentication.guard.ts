import { ExecutionContext, Injectable } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { Reflector } from '@nestjs/core';
import { MetadataKeys } from '../../../../metadata-keys.enum';

// https://docs.nestjs.com/security/authentication#implementing-passport-jwt
@Injectable()
export class JwtAuthenticationGuard extends AuthGuard('jwt') {
  // https://docs.nestjs.com/recipes/passport#enable-authentication-globally
  constructor(private reflector: Reflector) {
    super();
  }

  canActivate(context: ExecutionContext) {
    if (this.reflector.getAllAndOverride(
      MetadataKeys.PUBLIC,
      [context.getHandler(), context.getClass()]
    )) {
      return true;
    }

    return super.canActivate(context);
  }
}
