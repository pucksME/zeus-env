import argparse
import subprocess
import pathlib


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


def parse_arguments() -> tuple[str, str, str, str | None]:
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

    arguments = argument_parser.parse_args()
    return arguments.address, arguments.input_file, arguments.export_target, arguments.output_file


def main():
    address, input_file, export_target, output_file = parse_arguments()
    code = translate(address, input_file, export_target)
    write_output(code, output_file)


if __name__ == '__main__':
    main()