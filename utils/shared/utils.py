import argparse
import pathlib
from shared.argument import Argument


def parse_arguments(arguments: set[Argument]) -> dict[Argument, str]: #tuple[str, str, str, str | None, str | None]:
    argument_parser = argparse.ArgumentParser()

    if Argument.ADDRESS in arguments:
        argument_parser.add_argument(
            '--address',
            help='compiler address (e.g. localhost:8080)',
            required=True
        )

    if Argument.INPUT_FILE in arguments:
        argument_parser.add_argument(
            '--input-file',
            help='input file path (rain)',
            required=True
        )

    if Argument.EXPORT_TARGET in arguments:
        argument_parser.add_argument(
            '--export-target',
            help='translation target (supported: REACT_TYPESCRIPT)',
            required=True
        )

    if Argument.OUTPUT_FILE in arguments:
        argument_parser.add_argument(
            '--output-file',
            help='output file path (output is written to stdout if not provided)'
        )

    if Argument.EXTRACTION_DIRECTORY in arguments:
        argument_parser.add_argument(
            '--extraction-directory',
            help='if provided, target directory path for extracted output payload'
        )

    if Argument.OUTPUT_DIRECTORY in arguments:
        argument_parser.add_argument(
            '--output-directory',
            help='output directory path',
            required=True
        )

    if Argument.CODE_MODULE_NAME in arguments:
        argument_parser.add_argument(
            '--code-module-name',
            help='code module name',
            required=True
        )

    parsed_arguments = argument_parser.parse_args()
    return {argument : getattr(parsed_arguments, argument.value) for argument in arguments}


def build_payload(rain_code: str, export_target: str) -> str:
    rain_code = rain_code.replace('\n', '\\n')
    rain_code = rain_code.replace('"', '\\"')
    return '{{"code": "{rain_file}", "exportTarget": "{export_target}"}}'.format(
        rain_file=rain_code,
        export_target=export_target
    )


def read_rain_file(file_path: str) -> str | None:
    path = pathlib.Path(file_path).resolve()
    if not path.exists():
        print('file does not exist')
        return None

    with open(file_path, 'r') as file:
        return file.read()