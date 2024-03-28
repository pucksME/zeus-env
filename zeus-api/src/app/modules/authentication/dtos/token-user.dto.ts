import { ApiProperty, PickType } from '@nestjs/swagger';
import { UserDto } from '../../user/dtos/user.dto';
import { IsUUID } from 'class-validator';

export class TokenUserDto extends PickType(UserDto, ['email', 'firstName', 'lastName'] as const) {

  @ApiProperty()
  @IsUUID()
  sub: string;

}
