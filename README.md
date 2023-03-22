# AstCreator4j
This tool is created for research about bug detection using machine learning. Tool generates Abstract Syntax Tree (AST) from given `*.java` file(s).
For currently planned version the given `*.java` file **must** only include function implementation. Example can be found under `src/java/resources`. 

# Requirements
This code is only tested with Java 17. You also need Gradle on your system.

# Installation via Gradle
After downloading/cloning the code, go to terminal and open the root directory of the project:

`.\gradlew installDist` if on Windows, `./gradlew installDist` if on Linux/MacOS. This will compile the project and generate executable scripts for you.

Then go to `build/install/AstCreator4j/bin` directory in your terminal. You will see `AstCreator4j` executable file.

# Command line (CLI) Usage
On Windows:
`.\AstCreator4j [-f/--file] "path_to_java_file" or [-d/--directory] "directory_path_including_java_files" [-o/--outputDir] "path_to_output_directory"`

On Linux/MacOS:
`.\AstCreator4j [-f/--file] "path_to_java_file" or [-d/--directory] "directory_path_including_java_files" [-o/--outputDir] "path_to_output_directory"`

Options:

`-f/--file` Path to a .java file. Cannot be used together with -d/--directory option.

`-d/--directory` Path to a directory that includes .java file(s). Cannot be used together with -f/--file option.

`-o/--outputDir` (Optional) Path to write generated AST JSON files. If not given, directory given by -f or -d will be used.

