import { ApiProperty } from '@nestjs/swagger';

export class AuthenticateUserDto {

  @ApiProperty()
  email: string;

  @ApiProperty()
  password: string;

}
