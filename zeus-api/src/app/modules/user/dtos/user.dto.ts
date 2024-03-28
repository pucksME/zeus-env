import { ApiProperty } from '@nestjs/swagger';
import { IsEmail, IsString, IsUUID, MaxLength, MinLength } from 'class-validator';

export class UserDto {

  @IsUUID()
  uuid: string;

  @ApiProperty()
  @IsEmail()
  email: string;

  @ApiProperty()
  @IsString()
  // TODO: store such values in a json file shared between API and client
  @MinLength(3)
  @MaxLength(20)
  firstName: string;

  @ApiProperty()
  @IsString()
  @MinLength(3)
  @MaxLength(20)
  lastName: string;

}
