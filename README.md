# AstCreator4j

This tool is created for research about bug detection using machine learning. Tool generates Abstract Syntax Tree (AST)
from given `*.java` file(s) and save them as PNG file.
For currently planned version the given `*.java` file **must** only include function implementation. Example can be
found under `src/main/java/resources/1.java`.

# Requirements

This code is only tested with Java 17. You also need Gradle on your system.

# Installation via Gradle

After downloading/cloning the code, go to terminal and open the root directory of the project:

`.\gradlew installDist` if on Windows, `./gradlew installDist` if on Linux/macOS. This will compile the project and
generate executable scripts for you.

Then go to `build/install/AstCreator4j/bin` directory in your terminal. You will see `AstCreator4j` executable file.
Also `config.properties` file will be here. Usage of `config.properties` is mentioned
in [Output Modification](#output-modification) section.

# Command line (CLI) Usage

On Windows:
`.\AstCreator4j [-f/--file] "path_to_java_file" or [-d/--directory] "directory_path_including_java_files" [-o/--outputDir] "path_to_output_directory"`

On Linux/MacOS:
`./AstCreator4j [-f/--file] "path_to_java_file" or [-d/--directory] "directory_path_including_java_files" [-o/--outputDir] "path_to_output_directory"`

Options:

`-f/--file` Path to a .java file. Cannot be used together with -d/--directory option.

`-d/--directory` Path to a directory that includes .java file(s). Cannot be used together with -f/--file option.

`-o/--outputDir` (Optional) Path to write generated AST JSON files. If not given, directory given by -f or -d will be
used.

`-help/--help` Prints the help text. Explanation of the arguments can be found here.

# Output Modification

Before compilation, you can customize your output graph by modifying `config.properties` file
under `src/main/java/resources`.

After compilation, you can customize your output graph by modifying `config.properties` file under `build/install`.

This file is read when generating the ASTs. You can change the shape and color of each visited node.
Height and width of the output and nodes can also be changed from here. If you do not want to fill nodes you can turn it
off from
here as well (simply change `output.fillNodes` to `false`). If you want more details about the node you can also
configure that by changing `output.detailed` to `true`. If you only want statement level depth in your ASTs,
set `output.includeExpressions` to `false`.

By default, output PNG is 224x224, because ResNet50 accepts this image size.

*IMPORTANT:* Do not forget to recompile your project after changing the properties file, or you can simply change the
properties file in `build/install/AstCreator4j/bin`.

### Deleted config.properties?

Do not worry, if AstCreator4j cannot find the `config.properties` file, it will generate a new one with default values.
