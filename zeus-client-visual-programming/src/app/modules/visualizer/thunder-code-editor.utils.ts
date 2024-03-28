export abstract class ThunderCodeEditorUtils {
  static getLinesOfCode(code: string): number {
    return code.split('\n').length;
  }

  static getLineNumber(code: string, position: number): number {
    const lines = code.split('\n').map(line => line + '\n');
    for (let i = 0; i < lines.length; i++) {
      position -= lines[i].length;
      if (position < 0) {
        return i + 1;
      }
    }
    return lines.length;
  }

  static getNumberOfCharactersInLine(code: string, lineNumber: number): number {
    if (lineNumber < 1) {
      return 0;
    }

    const lines = code.split('\n');

    if (lineNumber > lines.length) {
      return 0;
    }

    return lines[lineNumber - 1].length;
  }
}
