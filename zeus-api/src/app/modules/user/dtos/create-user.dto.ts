import { IsString, MinLength } from 'class-validator';
import { ApiProperty, OmitType, PickType } from '@nestjs/swagger';
import { UserDto } from './user.dto';

export class CreateUserDto extends PickType(UserDto, ['email', 'firstName', 'lastName' as const]) {

  @ApiProperty()
  @IsString()
  @MinLength(4)
  password: string;

}
