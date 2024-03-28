import { ShapePropertiesDto } from './shape-properties.dto';
import {
  FontFamily,
  FontStyle, TextAlign,
  TextDecoration,
  TextProperties,
  TextTransform
} from '../../interfaces/shape-properties/text-properties.interface';
import { ApiProperty } from '@nestjs/swagger';
import { IsNumber, IsString, Min } from 'class-validator';

export class TextPropertiesDto extends ShapePropertiesDto implements TextProperties {

  @ApiProperty({
    type: 'enum',
    enum: FontFamily,
    enumName: 'FontFamily'
  })
  fontFamily: FontFamily;

  @ApiProperty()
  @IsNumber()
  @Min(0)
  fontSize: number;

  @ApiProperty({
    type: 'enum',
    enum: FontStyle,
    enumName: 'FontStyle'
  })
  fontStyle: FontStyle;

  @ApiProperty()
  @IsString()
  text: string;

  @ApiProperty({
    type: 'enum',
    enum: TextDecoration,
    enumName: 'TextDecoration'
  })
  textDecoration: TextDecoration;

  @ApiProperty({
    type: 'enum',
    enum: TextTransform,
    enumName: 'TextTransform'
  })
  textTransform: TextTransform;

  @ApiProperty({
    type: 'enum',
    enum: TextAlign,
    enumName: 'TextAlign'
  })
  textAlign: TextAlign;

}
