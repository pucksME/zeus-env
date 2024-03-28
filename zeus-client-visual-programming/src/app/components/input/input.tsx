import React, { CSSProperties, useEffect, useRef, useState } from 'react';

import './input.module.scss';
import { Button, InputBase, makeStyles } from '@material-ui/core';
import DownControlIcon from '@material-ui/icons/Remove';
import UpControlIcon from '@material-ui/icons/Add';
import { AppUtils } from '../../app.utils';

export enum InputType {
  TEXT,
  PASSWORD,
  NUMERIC,
  NUMERIC_CONTROLS
}

export enum InputSize {
  SMALL,
  DEFAULT
}

enum ControlType {
  DOWN,
  UP
}

export interface InputChangeEvent {
  event: React.ChangeEvent<HTMLInputElement> | null;
  value: unknown;
}

export interface InputSubmitEvent {
  event: React.KeyboardEvent<HTMLInputElement> | null;
  value: unknown;
}

export interface InputProps {
  placeholder?: string;
  type?: InputType;
  size?: InputSize;
  value: unknown;
  onChange?: (event: InputChangeEvent) => void;
  onSubmit?: (event: InputSubmitEvent) => void;
  debounceTimeoutOnControlClick?: number;
  style?: CSSProperties;
}

const useInputStyles = (fontSize: string) => makeStyles({
  root: {
    paddingLeft: 5,
    paddingRight: 5
  },
  input: { fontSize }
});

const useControlsStyles = (dimensions: number) => makeStyles({
  root: {
    backgroundColor: '#ffffff',
    borderRadius: 5,
    height: dimensions,
    minWidth: dimensions,
    width: dimensions,
    padding: 0
  }
});

export function Input(props: InputProps) {

  const size = (props.size === undefined) ? InputSize.DEFAULT : props.size;
  const controlDimensions = (size === InputSize.DEFAULT) ? 25 : 20;
  const inputPadding = (size === InputSize.DEFAULT) ? 5 : 3;
  const inputClasses = useInputStyles((size === InputSize.DEFAULT) ? '1rem' : '0.875rem')();
  const controlClasses = useControlsStyles(controlDimensions)();

  const validateNumericValue = (value: unknown) => {
    const valueNumber = Number(value);

    if (isNaN(valueNumber)) {
      return null;
    }

    return Math.round(valueNumber);
  };

  const [value, setValue] = useState<unknown>('');

  useEffect(() => {
    const initialValue = props.type === InputType.NUMERIC || props.type === InputType.NUMERIC_CONTROLS
      ? validateNumericValue(props.value)
      : props.value;

    setValue((initialValue === null) ? '' : initialValue);
  }, [props.value]);

  const debouncedOnSubmit = useRef(AppUtils.debounce(
    props.onSubmit, (props.debounceTimeoutOnControlClick === undefined) ? 0 : props.debounceTimeoutOnControlClick
  ));

  const getType = () => {
    switch (props.type) {
      case InputType.PASSWORD:
        return 'password';
      case InputType.TEXT:
      case InputType.NUMERIC:
      case InputType.NUMERIC_CONTROLS:
      default:
        return 'text';
    }
  };

  const controlIconStyle: CSSProperties = {
    fontSize: 15
  };

  const handleControlClick = (type: ControlType) => {
    const handlerValue = Number(value) + ((type === ControlType.UP) ? 1 : -1);
    setValue(handlerValue);
    props.onChange({event: null, value: handlerValue});

    if (props.onSubmit === undefined) {
      return;
    }

    if (props.debounceTimeoutOnControlClick !== undefined) {
      debouncedOnSubmit.current({event: null, value: handlerValue});
      return;
    }

    props.onSubmit({event: null, value: handlerValue});
  };

  const buildControl = (type: ControlType) => <Button
    onClick={() => handleControlClick(type)}
    classes={{ root: controlClasses.root }}
    disableRipple={true}
  >
    {type === ControlType.DOWN
      ? <DownControlIcon style={controlIconStyle} />
      : <UpControlIcon style={controlIconStyle} />}
  </Button>;

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {

    const eventValue = event.target.value;
    let eventValueNumber = null;

    if (props.type === InputType.NUMERIC || props.type === InputType.NUMERIC_CONTROLS) {
      eventValueNumber = validateNumericValue(eventValue);
      if (eventValueNumber === null) {
        return;
      }
    }

    const handlerValue = eventValueNumber !== null ? eventValueNumber : eventValue;

    if (props.onChange !== undefined) {
      props.onChange({ event, value: handlerValue });
    }

    setValue(handlerValue);
  };

  const handleKeyPress = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.code === 'Enter' && props.onSubmit !== undefined) {
      props.onSubmit({ event, value });
    }
  };

  return (
    <div style={{
      backgroundColor: '#eeeeee',
      borderRadius: 5,
      boxSizing: 'border-box',
      display: 'inline-flex',
      alignItems: 'center',
      height: controlDimensions + 2 * (inputPadding),
      paddingLeft: 5,
      paddingRight: 5,
      ...props.style
    }}>
      {(props.type !== InputType.NUMERIC_CONTROLS) ? null : buildControl(ControlType.DOWN)}
      <InputBase
        classes={{ root: inputClasses.root, input: inputClasses.input }}
        fullWidth={true}
        type={getType()}
        placeholder={props.placeholder}
        value={value}
        onChange={handleChange}
        onKeyPress={handleKeyPress}
      />
      {(props.type !== InputType.NUMERIC_CONTROLS) ? null : buildControl(ControlType.UP)}
    </div>
  );
}

export default Input;
