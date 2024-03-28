import { ApiProperty } from '@nestjs/swagger';
import { IsString } from 'class-validator';
import { ConfigType } from '../enums/config-type.enum';
import { CodeModuleConfigTypeDto } from './code-module-config-type.dto';

export class CodeModuleConfigDto {
  @ApiProperty()
  @IsString()
  name: string;

  @ApiProperty({type: CodeModuleConfigTypeDto})
  type: CodeModuleConfigTypeDto;

  @ApiProperty({
    enum: ConfigType,
    enumName: 'configType'
  })
  configType: ConfigType
}
