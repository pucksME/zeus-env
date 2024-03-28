import { Body, Controller, Post, Req, UseGuards, ValidationPipe } from '@nestjs/common';
import { ApiOkResponse, ApiOperation, ApiTags, ApiUnauthorizedResponse } from '@nestjs/swagger';
import { LocalAuthenticationGuard } from '../guards/local-authentication-guard/local-authentication.guard';
import { AuthenticateUserDto } from '../dtos/authenticate-user.dto';
import { AuthenticationService } from '../services/authentication.service';
import { JwtTokenDto } from '../dtos/jwt-token.dto';

@ApiTags('Authentication')
@Controller('authentication')
export class AuthenticationController {

  constructor(private readonly authenticationService: AuthenticationService) {
  }

  @ApiOperation({
    summary: 'Authenticates an user',
    description: 'Authenticates an user and returns a JWT token'
  })
  @ApiOkResponse({
    type: JwtTokenDto,
    description: 'The user was authenticated successfully'
  })
  @ApiUnauthorizedResponse({ description: 'The user did not exist, the password was wrong or the sent payload was invalid' })
  // First, the local strategy will ensure that the sent credentials are valid.
  // (if not, the request will fail). Furthermore, user data is injected
  // into the request object (req.user). Then, this function focuses
  // on generating a JWT token including that information.
  @UseGuards(LocalAuthenticationGuard)
  @Post()
  authenticate(
    // Used to enforce that credentials are sent to this route
    @Body(ValidationPipe) authenticateUserDto: AuthenticateUserDto,
    @Req() req
  ): Promise<JwtTokenDto> {
    return this.authenticationService.generateJwtToken(req.user);
  }

}
