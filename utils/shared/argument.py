import enum


class Argument(enum.Enum):
    ADDRESS = 'address'
    INPUT_FILE = 'input_file'
    EXPORT_TARGET = 'export_target'
    OUTPUT_FILE = 'output_file'
    EXTRACTION_DIRECTORY = 'extraction_directory'
    OUTPUT_DIRECTORY = 'output_directory'
    CODE_MODULE_NAME = 'code_module_name'
