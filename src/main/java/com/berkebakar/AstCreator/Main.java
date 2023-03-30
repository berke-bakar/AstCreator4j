package com.berkebakar.AstCreator;

import org.apache.commons.cli.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        CommandLineParser cliParser = new DefaultParser();
        Options cliOptions = createCliOptions();
        // Read or create properties file
        System.out.println("Reading config.properties...");
        Properties properties = getOrCreateProperties();

        if (properties == null){
            System.err.println("config.properties file cannot be created, move the executable to a different location, exiting...");
            System.exit(7);
        }

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
                                    AstCreator.createAst(path, outputPath, properties);
                                    System.out.println("Generated AST for " + path.getFileName().toString());
                                }); // Replace with your own logic
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        System.exit(6);
                    }
                }
                else { // single file
                    System.out.println("Creating AST for " + inputPath.getFileName().toString());
                    AstCreator.createAst(inputPath, outputPath, properties);
                    System.out.println("Generated AST for " + inputPath.getFileName().toString());
                }
            }
            else { // should not be possible we validate inputs before
                System.err.println("Input path is null, exiting...");
                System.exit(5);
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(4);
        }
    }

    private static Options createCliOptions() {
        Options paramOptions = new Options();
        paramOptions.addOption(new Option("f", "file", true, "Java file that contains the function to be converted into AST."));
        paramOptions.addOption(new Option("d", "directory", true, "Directory that contains .java files to be converted into AST."));
        paramOptions.addOption(new Option("o", "outputDir", true, "Output directory to save generated ASTs. If option is not given, then Java files directory will be used."));
        paramOptions.addOption(new Option("help", "help", false, "Prints this help text."));
        return paramOptions;
    }

    private static void validateOptions(CommandLine line) {
        if (line.hasOption("help")){
            printHelpMenu();
            System.exit(0);
        }

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

    private static void printHelpMenu(){
        System.out.println("Following are the commandline parameters of this tool. Additional settings");
        System.out.println("can be done in config.properties file.");
        for (Option option : createCliOptions().getOptions()){
            System.out.printf("-%-5s --%-10s      %-50s\n", option.getOpt(), option.getLongOpt(), option.getDescription());
        }
    }

    private static Properties getOrCreateProperties(){
        Properties props = new Properties();
        try (InputStream inputStream = new FileInputStream("config.properties")) {
            props.load(inputStream);
        } catch (FileNotFoundException e) {
            System.out.println("Could not find config.properties, creating a new one...");
            // The file doesn't exist, so create it with default values
            props.setProperty("output.height", "224");
            props.setProperty("output.width", "224");
            props.setProperty("output.detailed", "false");
            props.setProperty("output.fillNodes", "true");
            // Default shape values
            props.setProperty("TypeDeclaration.shape", "true");
            props.setProperty("MethodDeclaration.shape", "box");
            props.setProperty("AnonymousClassDeclaration.shape", "house");
            props.setProperty("SingleVariableDeclaration.shape", "circle");
            props.setProperty("AssertStatement.shape", "star");
            props.setProperty("Block.shape", "square");
            props.setProperty("BreakStatement.shape", "trapezium");
            props.setProperty("ContinueStatement.shape", "invtriangle");
            props.setProperty("DoStatement.shape", "hexagon");
            props.setProperty("EnhancedForStatement.shape", "parallelogram");
            props.setProperty("ExpressionStatement.shape", "triangle");
            props.setProperty("ForStatement.shape", "parallelogram");
            props.setProperty("IfStatement.shape", "diamond");
            props.setProperty("LabeledStatement.shape", "pentagon");
            props.setProperty("ReturnStatement.shape", "cds");
            props.setProperty("SwitchStatement.shape", "diamond");
            props.setProperty("SynchronizedStatement.shape", "octagon");
            props.setProperty("ThrowStatement.shape", "invtrapezium");
            props.setProperty("TryStatement.shape", "invhouse");
            props.setProperty("CatchClause.shape", "septagon");
            props.setProperty("TypeDeclarationStatement.shape", "doubleoctagon");
            props.setProperty("VariableDeclarationStatement.shape", "component");
            props.setProperty("WhileStatement.shape", "hexagon");
            props.setProperty("YieldStatement.shape", "rarrow");
            // Default color values
            props.setProperty("TypeDeclaration.color", "green");
            props.setProperty("MethodDeclaration.color", "aqua");
            props.setProperty("AnonymousClassDeclaration.color", "darkgoldenrod1");
            props.setProperty("SingleVariableDeclaration.color", "darkgreen");
            props.setProperty("AssertStatement.color", "teal");
            props.setProperty("Block.color", "coral");
            props.setProperty("BreakStatement.color", "orange");
            props.setProperty("ContinueStatement.color", "sienna");
            props.setProperty("DoStatement.color", "yellow");
            props.setProperty("EnhancedForStatement.color", "red");
            props.setProperty("ExpressionStatement.color", "fuchsia");
            props.setProperty("ForStatement.color", "red");
            props.setProperty("IfStatement.color", "purple");
            props.setProperty("LabeledStatement.color", "lightpink");
            props.setProperty("ReturnStatement.color", "hotpink");
            props.setProperty("SwitchStatement.color", "purple");
            props.setProperty("SynchronizedStatement.color", "palegreen");
            props.setProperty("ThrowStatement.color", "tan");
            props.setProperty("TryStatement.color", "honeydew4");
            props.setProperty("CatchClause.color", "firebrick");
            props.setProperty("TypeDeclarationStatement.color", "royalblue");
            props.setProperty("VariableDeclarationStatement.color", "brown");
            props.setProperty("WhileStatement.color", "yellow");
            props.setProperty("YieldStatement.color", "blue");
            // Write to file
            try (OutputStream outputStream = new FileOutputStream("config.properties")) {
                props.store(outputStream, "Default properties file created by AstCreator4j");
            } catch (IOException ex) {
                // Handle the exception
                return null;
            }
        } catch (IOException e) {
            // Could not create or write to file
            return null;
        }

        return props;
    }

}
