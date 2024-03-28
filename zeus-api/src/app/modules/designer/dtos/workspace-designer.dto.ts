import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsUUID } from 'class-validator';
import { WorkspaceType } from '../enums/workspace-type.enum';
import { ViewDto } from './view.dto';

export class WorkspaceDesignerDto {

  @ApiProperty()
  @IsUUID()
  uuid: string;

  @ApiProperty()
  @IsString()
  name: string;

  @ApiProperty()
  positionX: number;

  @ApiProperty()
  positionY: number;

  @ApiProperty()
  scale: number;

  @ApiProperty({
    enum: WorkspaceType,
    enumName: 'WorkspaceType'
  })
  type: WorkspaceType;

  @ApiProperty({ type: [ViewDto] })
  views: ViewDto[];

}
