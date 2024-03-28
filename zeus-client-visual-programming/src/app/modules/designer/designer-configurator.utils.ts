import { CSSProperties } from 'react';
import colors from '../../../assets/styling/colors.json';

export abstract class DesignerConfiguratorUtils {

  static getColorPickerButtonStyle(): CSSProperties {
    return {
      borderStyle: 'solid',
      borderColor: colors.border_light,
      borderWidth: 1,
      height: 20,
      minWidth: 35,
      width: 35
    };
  }

}
