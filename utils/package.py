from shared import utils
import subprocess
import zipfile
import io
from shared.argument import Argument
import pathlib


def package(address: str, file_path: str, export_target: str, directory_path: str):
    rain_code = utils.read_rain_file(file_path)

    if rain_code is None:
        exit(1)

    output = subprocess.run([
        'curl', '-X', 'POST',
        '-H', 'Content-Type: application/json',
        '--data', utils.build_payload(rain_code, export_target),
        'http://{address}/api/project/packageRain'.format(address=address)
    ], capture_output=True)

    directory_path = pathlib.Path(directory_path).resolve()

    if directory_path.is_file():
        print('output directory is a file: could not package')
        exit(1)

    if not directory_path.exists():
        directory_path.mkdir(parents=True, exist_ok=True)

    package_file = zipfile.ZipFile(io.BytesIO(output.stdout))
    package_file.extractall(directory_path)


def main():
    arguments = utils.parse_arguments({
        Argument.ADDRESS,
        Argument.INPUT_FILE,
        Argument.EXPORT_TARGET,
        Argument.OUTPUT_DIRECTORY
    })

    package(
        arguments[Argument.ADDRESS],
        arguments[Argument.INPUT_FILE],
        arguments[Argument.EXPORT_TARGET],
        arguments[Argument.OUTPUT_DIRECTORY]
    )


if __name__ == '__main__':
    main()