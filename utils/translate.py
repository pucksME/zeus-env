from shared import utils
import subprocess
import pathlib
import json
from shared.argument import Argument


def extract_output(code: str, directory_path: str | None):
    if directory_path is None:
        return

    code_json = json.loads(code)
    if 'exportedClientDtos' not in code_json or 'exportedServerDtos' not in code_json or 'errors' not in code_json:
        print('invalid output: could not perform extraction')
        return

    path = pathlib.Path(directory_path).resolve()

    if path.exists() and not path.is_dir():
        print('extraction directory is not a directory: could not perform extraction')
        return

    if not path.exists():
        path.mkdir(parents=True, exist_ok=True)

    for server in code_json['exportedServerDtos']:
        server_path = path.joinpath('server-' + server['name']).resolve()

        if not server_path.exists():
            server_path.mkdir(parents=True, exist_ok=True)

        for file in server['exportedFileDtos']:
            with open(server_path.joinpath(file['filename']).resolve(), 'w') as code_file:
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
    rain_code = utils.read_rain_file(file_path)

    if rain_code is None:
        exit(1)

    output = subprocess.run([
        'curl', '-X', 'POST',
        '-H', 'Content-Type: application/json',
        '--data', utils.build_payload(rain_code, export_target),
        'http://{address}/translateProject'.format(address=address)
    ], capture_output=True)

    return output.stdout.decode('utf-8')


def main():
    arguments = utils.parse_arguments({
        Argument.ADDRESS,
        Argument.INPUT_FILE,
        Argument.EXPORT_TARGET,
        Argument.OUTPUT_FILE,
        Argument.EXTRACTION_DIRECTORY
    })

    code = translate(arguments[Argument.ADDRESS], arguments[Argument.INPUT_FILE], arguments[Argument.EXPORT_TARGET])
    write_output(code, arguments[Argument.OUTPUT_FILE])
    extract_output(code, arguments[Argument.EXTRACTION_DIRECTORY])


if __name__ == '__main__':
    main()