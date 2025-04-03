import subprocess

from shared import utils
from shared.argument import Argument
import json


def verify_code_module(address: str, file_path: str, code_module_name: str) -> None:
    rain_code = utils.read_rain_file(file_path)

    if rain_code is None:
        exit(1)

    output = subprocess.run([
        'curl', '-X', 'POST',
        '-H', 'Content-Type: application/json',
        '--data', json.dumps({
            'code': rain_code,
            'codeModuleName': code_module_name
        }),
        f'http://{address}/api/project/verifyCodeModule'
    ], capture_output=True)

    print(output.stdout)

def main():
    arguments = utils.parse_arguments({
        Argument.ADDRESS,
        Argument.INPUT_FILE,
        Argument.CODE_MODULE_NAME
    })

    verify_code_module(
        arguments[Argument.ADDRESS],
        arguments[Argument.INPUT_FILE],
        arguments[Argument.CODE_MODULE_NAME]
    )

if __name__ == '__main__':
    main()
