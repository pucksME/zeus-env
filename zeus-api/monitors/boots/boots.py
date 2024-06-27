import argparse


def parse_arguments() -> tuple[str, str, str]:
    argument_parser = argparse.ArgumentParser()

    argument_parser.add_argument(
        '--route',
        help='The route (for context)'
    )
    argument_parser.add_argument(
        '--request-parameters',
        help="The request's url parameters",
        required=True
    )

    argument_parser.add_argument(
        '--request-payload',
        help="The request's payload",
        required=True
    )

    arguments = argument_parser.parse_args()
    return arguments.route, arguments.request_parameters, arguments.request_payload


def main():
    route, request_parameters, request_payload = parse_arguments()
    print('route', route, sep='\n')
    print('request parameters', request_parameters, sep='\n')
    print('request payload', request_payload, sep='\n')


if __name__ == '__main__':
    main()