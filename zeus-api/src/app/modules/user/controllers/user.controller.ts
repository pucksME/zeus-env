import { Body, Controller, Post, ValidationPipe } from '@nestjs/common';
import { CreateUserDto } from '../dtos/create-user.dto';
import { UserDto } from '../dtos/user.dto';
import { UserService } from '../services/user.service';
import { ApiBadRequestResponse, ApiConflictResponse, ApiOkResponse, ApiOperation, ApiTags } from '@nestjs/swagger';

@ApiTags('User')
// https://docs.nestjs.com/openapi/security#bearer-authentication
// @ApiBearerAuth()
// @UseGuards(JwtAuthenticationGuard)
@Controller('user')
export class UserController {

  constructor(private readonly userService: UserService) {
  }

  @ApiOperation({
    summary: 'Creates a new user',
    description: 'Creates a new user'
  })
  @ApiOkResponse({
    type: UserDto,
    description: 'The user was registered successfully'
  })
  @ApiConflictResponse({ description: 'Another user was already registered with that email address' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Post()
  save(@Body(ValidationPipe) createUserDto: CreateUserDto): Promise<UserDto> {
    return this.userService.save(createUserDto);
  }

}
