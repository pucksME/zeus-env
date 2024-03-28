import { ApiProperty, PickType } from '@nestjs/swagger';
import { ComponentDto } from './component.dto';
import { IsNumber, IsUUID } from 'class-validator';
import { BlueprintElementDto } from './blueprint-element.dto';

export class BlueprintComponentDto extends PickType(ComponentDto, ['name'] as const) {

  @ApiProperty()
  @IsUUID()
  uuid: string;

  @ApiProperty()
  positionX: number;

  @ApiProperty()
  positionY: number;

  @ApiProperty()
  @IsNumber()
  sorting: number;

  @ApiProperty()
  referencingComponentUuids: string[];

  @ApiProperty({ type: [BlueprintElementDto] })
  elements: BlueprintElementDto[];

}
