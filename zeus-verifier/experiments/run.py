import pathlib
import subprocess
from time import sleep


def get_project_directory() -> pathlib.Path:
    return pathlib.Path(__file__).parent.parent.parent.resolve()


def start_compiler(print_stdout: bool) -> subprocess.Popen:
    print('Starting compiler...')
    return subprocess.Popen(
        ['java', '-jar', get_project_directory() / 'zeus-compiler' / 'build' / 'libs' / 'zeus-compiler-0.0.1-SNAPSHOT.jar'],
        stdout=None if print_stdout else subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1
    )


def start_verifier(
        root_config: str,
        model_checking_gateway_config: str,
        abstraction_gateway_config: str,
        counterexample_analysis_gateway_config: str,
        storage_gateway_config: str,
        model_checking_worker_config: str,
        abstraction_worker_config: str,
        counterexample_analysis_worker_config: str,
        storage_worker_config: str,
) -> tuple[subprocess.Popen, list[subprocess.Popen]]:
    def start_node(config: str, print_stdout: bool) -> subprocess.Popen:
        print(f'Starting node with config "{config}"')
        return subprocess.Popen(
            [
                'java', '-jar',
                pathlib.Path(
                    __file__).parent.parent.resolve() / 'build' / 'libs' / 'zeus-verifier-1.0-SNAPSHOT-all.jar',
                pathlib.Path(__file__).parent.parent.resolve() / 'src' / 'main' / 'resources' / 'configs' / config
            ],
            stdout=None if print_stdout else subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1
        )
    print('Starting verifier...')
    print('Starting root node...')
    root_process = start_node(root_config, False)
    sleep(0.25)

    print('Starting gateway nodes...')
    gateway_processes = []

    for gateway_config in [
        model_checking_gateway_config,
        abstraction_gateway_config,
        counterexample_analysis_gateway_config,
        storage_gateway_config
    ]:
        gateway_processes.append(start_node(gateway_config, False))

    sleep(0.25)
    print('Starting worker nodes...')
    worker_processes = []

    for worker_config in [
        model_checking_worker_config,
        abstraction_worker_config,
        counterexample_analysis_worker_config,
        storage_worker_config
    ]:
        worker_processes.append(start_node(worker_config, False))

    return root_process, gateway_processes + worker_processes


def setup_executable(directory: str, task_name: str):
    subprocess.run([get_project_directory() / directory / 'gradlew', '-p', get_project_directory() / directory, 'clean'])
    subprocess.run([get_project_directory() / directory / 'gradlew', '-p', get_project_directory() / directory, task_name])

def run():
    setup_executable('zeus-compiler', 'bootJar')
    setup_executable('zeus-verifier', 'shadowJar')
    compiler_process = start_compiler(True)
    verifier_process, other_processes = start_verifier(
        'config-node-root.json',
        'config-node-model-checking-gateway.json',
        'config-node-abstraction-gateway.json',
        'config-node-counterexample-analysis-gateway.json',
        'config-node-storage-gateway.json',
        'config-node-model-checking.json',
        'config-node-abstraction.json',
        'config-node-counterexample-analysis.json',
        'config-node-storage.json'
    )

    try:
        while True:
            output_line = verifier_process.stdout.readline()
            print(output_line)
            if not output_line:
                break
    except KeyboardInterrupt:
        print('Shutting down...')
    finally:
        compiler_process.terminate()
        verifier_process.terminate()
        for gateway_process in other_processes:
            gateway_process.terminate()


if __name__ == '__main__':
    run()