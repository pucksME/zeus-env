# Zeus Environment
Zeus-Env is a programming environment for developing general-purpose applications. It includes visual programming capabilities, a core API as well as a compiler for transforming the textual programming languages rain and thunder into a specific target.

# Subsystems
To serve full functionality, `zeus-env` encompasses several subsystems that work together. In this section there is a brief introduction of these systems.

## Zeus API
The `zeus-api` component is the core interface providing access to `zeus-compiler` and the database where project data is organized. Further, the API also implements features such as packaging compiled programs.

## Visual Programming Client
Visual programming features are accessible through a web client implemented in the `zeus-client-visual-programming` subsystem.

## Zeus Compiler
The `zeus-compiler` subsystem provides API endpoints to serve its functionalities and is responsible for compiling the `rain` and `thunder` languages as well as `boots` and `umbrella` specifications to the respective targets. More specifically, to achieve these translations, the zeus compiler involves multiple compilers that work together.

## Utils
In general, the `utils` project includes tools to utilize the other subsystems.