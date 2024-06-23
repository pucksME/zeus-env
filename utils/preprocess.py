from shared import utils
from shared.argument import Argument
import pathlib


def print_errors(errors: list[str]):
    if len(errors) == 0:
        return

    for error in errors:
        print(error)


def build_error(message: str, line: int = None):
    if line is None:
        return 'Error: ' + message

    return 'Error at line {line}: {message}'.format(line=line, message=message)


def preprocess_include(statement: str, line: int, errors: list[str]) -> str:
    prefix = '~include'
    indent = len(statement) - len(statement.lstrip())
    statement_parts = statement.strip().split(' ')

    if statement_parts[0] != prefix or len(statement_parts) != 2:
        return statement

    path = pathlib.Path(statement_parts[1]).resolve()

    if not path.exists():
        errors.append(build_error('file does not exist', line))
        return statement

    if not path.is_file():
        errors.append(build_error('input file is not a file', line))
        return statement

    with open(path, 'r') as file:
        return '\n'.join([' ' * indent + line for line in file.read().split('\n')])


def preprocess_statements(rain_file: str, errors: list[str]) -> str:
    rain_file_lines = []

    for index, line in enumerate(rain_file.split('\n')):
        rain_file_lines.append(preprocess_include(line, index + 1, errors))

    return '\n'.join(rain_file_lines)


def preprocess(file_path: str, output_path: str | None, errors: list[str]):
    with open(pathlib.Path(file_path).resolve(), 'r') as file:
        rain_file = file.read()

    if output_path is None:
        print(rain_file)
        return

    output_path = pathlib.Path(output_path).resolve()

    if not output_path.exists():
        output_path.parent.mkdir(parents=True, exist_ok=True)

    if output_path.is_dir():
        print(build_error('output file is a directory'))
        exit(1)

    with open(pathlib.Path(output_path).resolve(), 'w') as file:
        file.write(preprocess_statements(rain_file, errors))


def main():
    arguments = utils.parse_arguments({Argument.INPUT_FILE, Argument.OUTPUT_FILE})
    errors = []
    preprocess(arguments[Argument.INPUT_FILE], arguments[Argument.OUTPUT_FILE], errors)
    print_errors(errors)


if __name__ == '__main__':
    main()