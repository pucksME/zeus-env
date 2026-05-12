import pathlib
import subprocess
from time import sleep


def start_node(config: str, print_stdout: bool) -> subprocess.Popen:
    print(f'Starting node with config "{config}"')
    return subprocess.Popen(
        [
            'java', '-jar',
            pathlib.Path(__file__).parent.parent.resolve() / 'build' / 'libs' / 'zeus-verifier-1.0-SNAPSHOT-all.jar',
            pathlib.Path(__file__).parent.parent.resolve() / 'src' / 'main' / 'resources' / 'configs' / config
        ],
        stdout=None if print_stdout else subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1
    )

def start_nodes(
        root_config: str,
        model_checking_gateway_config: str,
        abstraction_gateway_config: str,
        counterexample_analysis_gateway_config: str,
        storage_gateway_config: str,
        model_checking_worker_config: str,
        abstraction_worker_config: str,
        counterexample_analysis_worker_config: str,
        storage_worker_config: str,
):
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


    try:
        while True:
            output_line = root_process.stdout.readline()
            # print(output_line)
            if not output_line:
                break
    except KeyboardInterrupt:
        print('Shutting down...')
    finally:
        root_process.terminate()
        for gateway_process in gateway_processes + worker_processes:
            gateway_process.terminate()


def run():
    start_nodes(
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


if __name__ == '__main__':
    run()