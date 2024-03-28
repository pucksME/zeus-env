import { ApiProperty } from '@nestjs/swagger';
import { IsString, Min } from 'class-validator';
import { ViewType } from '../enums/view-type.enum';
import { ComponentDto } from './component.dto';

export class ViewDto {

  @ApiProperty()
  @IsString()
  uuid: string;

  @ApiProperty()
  @IsString()
  name: string;

  @ApiProperty({
    enum: ViewType,
    enumName: 'ViewType'
  })
  type: ViewType;

  @ApiProperty()
  @Min(50)
  height: number;

  @ApiProperty()
  @Min(50)
  width: number;

  @ApiProperty()
  positionX: number;

  @ApiProperty()
  positionY: number;

  @ApiProperty()
  isRoot: boolean;

  @ApiProperty({ type: [ComponentDto] })
  components: ComponentDto[];

}
