import { ApiProperty } from '@nestjs/swagger';
import { IsBoolean, IsNumber, IsString, IsUUID } from 'class-validator';
import { ElementDto } from './element.dto';

export class ComponentDto {

  @ApiProperty()
  @IsUUID()
  uuid: string;

  @ApiProperty()
  @IsString()
  name: string;

  @ApiProperty()
  @IsNumber()
  positionX: number;

  @ApiProperty()
  @IsNumber()
  positionY: number;

  @ApiProperty()
  @IsNumber()
  sorting: number;

  @ApiProperty()
  @IsBoolean()
  isBlueprintComponentInstance: boolean;

  @ApiProperty({ type: [ElementDto] })
  elements: ElementDto[];

}
