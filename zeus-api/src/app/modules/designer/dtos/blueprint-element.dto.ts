import { ApiProperty, getSchemaPath, PickType } from '@nestjs/swagger';
import { ElementDto } from './element.dto';
import { ShapeDto } from './shape.dto';
import { BlueprintComponentDto } from './blueprint-component.dto';

export class BlueprintElementDto extends PickType(ElementDto, ['type'] as const) {

  @ApiProperty({
    oneOf: [
      { $ref: getSchemaPath('BlueprintComponentDto') },
      { $ref: getSchemaPath('ShapeDto') }
    ]
  })
  element: BlueprintComponentDto | ShapeDto;

}
