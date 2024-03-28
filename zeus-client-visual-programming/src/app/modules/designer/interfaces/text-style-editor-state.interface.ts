import { ShapeDto, TextPropertiesDto } from '../../../../gen/api-client';

export interface TextStyleEditorState {
  shapes: ShapeDto[];
  textPropertiesToUpdate: Partial<TextPropertiesDto>;
}
