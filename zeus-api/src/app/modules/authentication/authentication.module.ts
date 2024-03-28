import { Module } from '@nestjs/common';
import { AuthenticationController } from './controllers/authentication.controller';
import { AuthenticationService } from './services/authentication.service';
import { AuthenticationDataService } from './data/authentication-data.service';
import { UserModule } from '../user/user.module';
import { LocalStrategy } from './strategies/local.strategy';
import { JwtModule } from '@nestjs/jwt';
import { jwtConstants } from '../../../constants';
import { JwtStrategy } from './strategies/jwt.strategy';

@Module({
  imports: [
    UserModule,
    // https://docs.nestjs.com/security/authentication#jwt-functionality
    JwtModule.register({
      secret: jwtConstants.secret,
      signOptions: {
        expiresIn: jwtConstants.lifespan
      }
    })
  ],
  controllers: [AuthenticationController],
  providers: [
    AuthenticationService,
    AuthenticationDataService,
    LocalStrategy,
    JwtStrategy
  ]
})
export class AuthenticationModule {
}
