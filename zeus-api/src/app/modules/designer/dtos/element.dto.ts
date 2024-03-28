import { ComponentDto } from './component.dto';
import { ShapeDto } from './shape.dto';
import { ElementType } from '../enums/element-type.enum';
import { ApiProperty, getSchemaPath } from '@nestjs/swagger';

export class ElementDto {

  @ApiProperty({
    oneOf: [
      { $ref: getSchemaPath('ComponentDto') },
      { $ref: getSchemaPath('ShapeDto') }
    ]
  })
  element: ComponentDto | ShapeDto;

  @ApiProperty({
    enum: ElementType,
    enumName: 'ElementType'
  })
  type: ElementType;

}
