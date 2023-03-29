package com.berkebakar.AstCreator;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        CommandLineParser cliParser = new DefaultParser();
        Options cliOptions = createCliOptions();

        try {
            CommandLine commandLine = cliParser.parse(cliOptions, args);
            // Validate the options first
            validateOptions(commandLine);
            // initialize read/write paths based on command line parameters
            Path inputPath = null;
            Path outputPath;

            if (commandLine.hasOption("f")) {
                inputPath = Paths.get(commandLine.getOptionValue("f"));
            } else if (commandLine.hasOption("d")) {
                inputPath = Paths.get(commandLine.getOptionValue("d"));
            }

            if (commandLine.hasOption("o")) {
                outputPath = Paths.get(commandLine.getOptionValue("o"));
            } else { // if there is no --outputDir option defined then set the output directory as the input file(s)'s directory
                outputPath = getParentPath(inputPath);
            }

            if (inputPath != null){
                if (Files.isDirectory(inputPath)){ // walk through the directory
                    try (Stream<Path> paths = Files.walk(inputPath)) {
                        paths.filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(".java"))
                                .forEach(path -> {
                                    System.out.println("Creating AST for " + path.getFileName().toString());
                                    AstCreator.createAst(path, outputPath);
                                    System.out.println("Generated AST for " + path.getFileName().toString());
                                }); // Replace with your own logic
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        System.exit(6);
                    }
                }
                else { // single file
                    System.out.println("Creating AST for " + inputPath.getFileName().toString());
                    AstCreator.createAst(inputPath, outputPath);
                    System.out.println("Generated AST for " + inputPath.getFileName().toString());
                }
            }
            else { // should not be possible we validate inputs before
                System.err.println("Input path is null, exiting...");
                System.exit(5);
            }

            AstCreator.createAst(inputPath, outputPath);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(4);
        }
    }

    private static Options createCliOptions() {
        Options paramOptions = new Options();
        paramOptions.addOption(new Option("f", "file", true, "Java file that contains the function to be converted into AST."));
        paramOptions.addOption(new Option("d", "directory", true, "directory that contains .java files to be converted into AST."));
        paramOptions.addOption(new Option("o", "outputDir", true, "Output directory to save generated ASTs. If option is not given, then Java files directory will be used."));
        return paramOptions;
    }

    private static void validateOptions(CommandLine line) {
        if (!line.hasOption("f") && !line.hasOption("d")) {
            System.err.println("Need to provide at least one of -f/--file or -d/--directory options. Giving both will result in error.");
            System.exit(1);
        }

        if (line.hasOption("f") && line.hasOption("d")) {
            System.err.println("Cannot provide both -f/--files and -d/--directory options at the same time.");
            System.exit(2);
        }

        // Validate the values
        if (line.hasOption("f")) {
            try {
                Path path = Paths.get(line.getOptionValue("f"));
                if (!(Files.exists(path) && Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"))) {
                    System.err.println("A valid Java file path must be given to -f/--file option argument.");
                    System.exit(3);
                }
            } catch (InvalidPathException e) {
                System.err.println("Given path to -f/--file option is invalid.");
                System.exit(3);
            }
        } else if (line.hasOption("d")) {
            try {
                Path path = Paths.get(line.getOptionValue("d"));
                if (!(Files.exists(path) && Files.isDirectory(path))) {
                    System.err.println("A valid directory path must be given to -d/--directory option argument.");
                    System.exit(3);
                }
            } catch (InvalidPathException e) {
                System.err.println("Given path to -d/--directory option is invalid.");
                System.exit(3);
            }
        }

        if (line.hasOption("o")) {
            try {
                Path path = Paths.get(line.getOptionValue("o"));
                if (!(Files.exists(path) && Files.isWritable(path))) {
                    System.err.println("Invalid or non-writable path is given for -o/--outputDir option. Cannot continue.");
                    System.exit(3);
                }
            } catch (InvalidPathException e) {
                System.err.println("Given path to -o/--outputDir option is invalid. Cannot save files to invalid location.");
                System.exit(3);
            }
        }
    }

    private static Path getParentPath(Path path) {
        if (Files.isRegularFile(path)) {
            return path.getParent();
        } else {
            return path;
        }
    }

}
