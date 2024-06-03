import argparse
import subprocess
import pathlib
import json


def extract_output(code: str, directory_path: str | None):
    if directory_path is None:
        return

    code_json = json.loads(code)
    if code_json['exportedFileDtos'] is None or code_json['errors'] is None:
        print('invalid output: could not perform extraction')
        return

    path = pathlib.Path(directory_path).resolve()

    if path.exists() and not path.is_dir():
        print('extraction directory is not a directory: could not perform extraction')
        return

    if not path.exists():
        path.mkdir(parents=True, exist_ok=True)

    for file in code_json['exportedFileDtos']:
        with open(path.joinpath(file['filename']).resolve(), 'w') as code_file:
            code_file.write(file['code'])

    errors = []
    for error in code_json['errors']:
        errors.append('{line}:{position} - {message}'.format(
            line=error['line'],
            position=error['linePosition'],
            message=error['message']
        ))

    with open(path.joinpath('errors.txt'), 'w') as file:
        file.write('\n'.join(errors))


def write_output(code: str, file_path: str | None):
    if file_path is None:
        print(code)
        return

    with open(file_path, 'w') as file:
        file.write(code)


def translate(address: str, file_path: str, export_target: str) -> str:
    path = pathlib.Path(file_path).resolve()
    if not path.exists():
        print('file does not exist')
        exit(1)

    with open(file_path, 'r') as file:
        rain_file = file.read()

    rain_file = rain_file.replace('\n', '\\n')
    rain_file = '{{"code": "{rain_file}", "exportTarget": "{export_target}"}}'.format(
        rain_file=rain_file,
        export_target=export_target
    )

    output = subprocess.run(
        ['curl', '-X', 'POST', '-H', 'Content-Type: application/json', '--data', rain_file, 'http://{address}/translateProject'.format(address=address)],
        capture_output=True
    )

    return output.stdout.decode('utf-8')


def parse_arguments() -> tuple[str, str, str, str | None, str | None]:
    argument_parser = argparse.ArgumentParser()
    argument_parser.add_argument(
        '--address',
        help='compiler address (e.g. localhost:8080)',
        required=True
    )
    argument_parser.add_argument(
        '--input-file',
        help='input file path (rain)',
        required=True
    )

    argument_parser.add_argument(
        '--export-target',
        help='translation target (supported: REACT_TYPESCRIPT)',
        required=True
    )

    argument_parser.add_argument(
        '--output-file',
        help='output file path (output is written to stdout if not provided)'
    )

    argument_parser.add_argument(
        '--extraction-directory',
        help='if provided, target directory path for extracted output payload'
    )

    arguments = argument_parser.parse_args()
    return arguments.address, arguments.input_file, arguments.export_target, arguments.output_file, arguments.extraction_directory


def main():
    address, input_file, export_target, output_file, extraction_directory = parse_arguments()
    code = translate(address, input_file, export_target)
    write_output(code, output_file)
    extract_output(code, extraction_directory)


if __name__ == '__main__':
    main()