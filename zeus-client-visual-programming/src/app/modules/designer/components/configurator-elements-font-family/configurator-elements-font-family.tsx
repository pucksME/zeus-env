import React, { useEffect, useState } from 'react';

import './configurator-elements-font-family.module.scss';
import { Autocomplete } from '@material-ui/lab';
import { makeStyles, TextField } from '@material-ui/core';
import { FontFamily, ShapeDto } from '../../../../../gen/api-client';
import { useStore } from '../../../../store';
import colors from '../../../../../assets/styling/colors.json';
import { ShapeUtils } from '../../shape.utils';

export interface ConfiguratorElementsFontFamilyProps {
  elementUuids: string[];
  shapes: ShapeDto[];
  workspaceUuid: string;
}

const fontFamilies = [
  {id: FontFamily.Arial, name: 'Arial'}
];

const useAutocompleteStyles = makeStyles({

});

const useTextFieldStyles = makeStyles({
  root: {
    backgroundColor: colors.border_light,
    fontSize: '0.875rem',
    height: 26,
    padding: '0!important',
    paddingLeft: '5px!important'
  },
  notchedOutline: {
    border: 'none'
  }
});

export function ConfiguratorElementsFontFamily(
  props: ConfiguratorElementsFontFamilyProps
) {

  const autocompleteClasses = useAutocompleteStyles();
  const textFieldClasses = useTextFieldStyles();
  const [fontFamily, setFontFamily] = useState<FontFamily | null>(null);
  const [compatible, setCompatible] = useState(false);
  const setTextStyleEditorState = useStore(state => state.setTextStyleEditorState);

  useEffect(() => {
    if (props.elementUuids.length === 0) {
      return;
    }

    const sharedProperties = ShapeUtils.getSharedProperties(
      props.shapes, ['fontFamily']
    );

    if (!sharedProperties.compatible) {
      return setCompatible(false);
    }

    setFontFamily(sharedProperties.properties.fontFamily);
    setCompatible(true);
  }, [props.elementUuids]);

  const selectedFontFamily = fontFamilies.find(currentFontFamily => currentFontFamily.id === fontFamily);

  const handleChange = (event, value: {id: FontFamily, name: string}) => {
    const fontFamily = value.id;
    setTextStyleEditorState({
      shapes: props.shapes,
      textPropertiesToUpdate: {fontFamily}
    });
    setFontFamily(fontFamily);
  }

  return (!compatible) ? null : (
    <Autocomplete
      disableClearable={true}
      style={{width: 120}}
      size={'small'}
      value={selectedFontFamily}
      classes={autocompleteClasses}
      onChange={handleChange}
      renderInput={
        (params) =>
          <TextField
            {...params}
            InputProps={{
              ...params.InputProps,
              classes: textFieldClasses
            }}
            variant={'outlined'}
          />
      }
      options={fontFamilies}
      getOptionLabel={(option) => option.name}
    />
  );
}

export default ConfiguratorElementsFontFamily;
