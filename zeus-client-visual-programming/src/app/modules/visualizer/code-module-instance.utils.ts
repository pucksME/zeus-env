import { CodeModuleInstanceDto } from '../../../gen/api-client';
import { CodeModuleInstancePort } from '../../interfaces/code-module-instance-port.interface';
import spacing from '../../../assets/styling/spacing.json';

export abstract class CodeModuleInstanceUtils {
  static getPortPosition(
    codeModuleInstanceDto: CodeModuleInstanceDto,
    codeModuleInstancePort: CodeModuleInstancePort,
    isOutput: boolean
  ) {
    const x = codeModuleInstanceDto.positionX + ((isOutput) ? spacing.codeModuleInstance.width : 0);
    const portIndex = ((isOutput)
      ? codeModuleInstanceDto.codeModule.outputEndpoints
      : codeModuleInstanceDto.codeModule.inputEndpoints).findIndex(
        port => port.name === codeModuleInstancePort.portName
    );

    if (portIndex === -1) {
      return {x, y: 0};
    }

    const portHeight = spacing.codeModuleInstance.port.fontSize + spacing.codeModuleInstance.port.margin;
    const y = codeModuleInstanceDto.positionY +
      CodeModuleInstanceUtils.calculateMainDetailsHeight() +
      CodeModuleInstanceUtils.calculateDelimiterHeight() +
      ((portIndex + 1) * portHeight) - portHeight / 2;

    return {x, y};
  }

  static calculateCurvePoints(start: [number, number], end: [number, number]): [number, number, number, number] {
    const x = (start[0] + end[0]) / 2;
    return [x, start[1], x, end[1]];
  }

  static calculateMainDetailsHeight() {
    return spacing.codeModuleInstance.paddingVertical +
      spacing.codeModuleInstance.nameFontSize +
      spacing.codeModuleInstance.descriptionMargin +
      spacing.codeModuleInstance.descriptionFontSize;
  }

  static calculateDelimiterHeight() {
    return (spacing.codeModuleInstance.paddingVertical * 2) +
      spacing.codeModuleInstance.delimiterHeight;
  }

  static calculatePortHeight() {
    return spacing.codeModuleInstance.port.fontSize + spacing.codeModuleInstance.port.margin;
  }

  static calculatePortsHeight(codeModuleInstanceDto: CodeModuleInstanceDto) {
    return Math.max(
      codeModuleInstanceDto.codeModule.inputEndpoints.length,
      codeModuleInstanceDto.codeModule.outputEndpoints.length
    ) * CodeModuleInstanceUtils.calculatePortHeight();
  }
}
