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
            props.setProperty("output.includeExpressions", "true");
            props.setProperty("output.nodeWidth", "2");
            props.setProperty("output.nodeHeight", "2");
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
            props.setProperty("ArrayAccess.shape", "cylinder");
            props.setProperty("ArrayCreation.shape", "Mcircle");
            props.setProperty("ArrayInitializer.shape", "Mdiamond");
            props.setProperty("Assignment.shape", "Msquare");
            props.setProperty("VariableDeclarationExpression.shape", "tab");
            props.setProperty("TypeMethodReference.shape", "folder");
            props.setProperty("TypeLiteral.shape", "rpromoter");
            props.setProperty("ThisExpression.shape", "rarrow");
            props.setProperty("SuperMethodInvocation.shape", "larrow");
            props.setProperty("SuperMethodReference.shape", "lpromoter");
            props.setProperty("SuperFieldAccess.shape", "box3d");
            props.setProperty("StringLiteral.shape", "rect");
            props.setProperty("PrefixExpression.shape", "invtriangle");
            props.setProperty("PostfixExpression.shape", "invtrapezium");
            props.setProperty("ParenthesizedExpression.shape", "invhouse");
            props.setProperty("NumberLiteral.shape", "square");
            props.setProperty("NullLiteral.shape", "star");
            props.setProperty("MethodRef.shape", "hexagon");
            props.setProperty("MethodInvocation.shape", "pentagon");
            props.setProperty("LambdaExpression.shape", "septagon");
            props.setProperty("InstanceofExpression.shape", "egg");
            props.setProperty("InfixExpression.shape", "triangle");
            props.setProperty("FieldAccess.shape", "diamond");
            props.setProperty("ExpressionMethodReference.shape", "parallelogram");
            props.setProperty("CreationReference.shape", "house");
            props.setProperty("ConditionalExpression.shape", "cds");
            props.setProperty("ClassInstanceCreation.shape", "ellipse");
            props.setProperty("CharacterLiteral.shape", "circle");
            props.setProperty("CastExpression.shape", "trapezium");
            props.setProperty("BooleanLiteral.shape", "component");
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
            props.setProperty("ArrayAccess.color", "antiquewhite");
            props.setProperty("ArrayCreation.color", "aquamarine");
            props.setProperty("ArrayInitializer.color", "aquamarine4");
            props.setProperty("Assignment.color", "blueviolet");
            props.setProperty("VariableDeclarationExpression.color", "cornflowerblue");
            props.setProperty("TypeMethodReference.color", "crimson");
            props.setProperty("TypeLiteral.color", "gold");
            props.setProperty("ThisExpression.color", "gray60");
            props.setProperty("SuperMethodInvocation.color", "lightgoldenrod2");
            props.setProperty("SuperMethodReference.color", "lavender");
            props.setProperty("SuperFieldAccess.color", "magenta");
            props.setProperty("StringLiteral.color", "olive");
            props.setProperty("PrefixExpression.color", "springgreen");
            props.setProperty("PostfixExpression.color", "violetred");
            props.setProperty("ParenthesizedExpression.color", "webpurple");
            props.setProperty("NumberLiteral.color", "tomato");
            props.setProperty("NullLiteral.color", "violet");
            props.setProperty("MethodRef.color", "lightpink3");
            props.setProperty("MethodInvocation.color", "darkorchid2");
            props.setProperty("LambdaExpression.color", "darkkhaki");
            props.setProperty("InstanceofExpression.color", "darkcyan");
            props.setProperty("InfixExpression.color", "chocolate");
            props.setProperty("FieldAccess.color", "darkorange3");
            props.setProperty("ExpressionMethodReference.color", "darkslateblue");
            props.setProperty("CreationReference.color", "dodgerblue");
            props.setProperty("ConditionalExpression.color", "dimgray");
            props.setProperty("ClassInstanceCreation.color", "forestgreen");
            props.setProperty("CharacterLiteral.color", "darksalmon");
            props.setProperty("CastExpression.color", "darkred");
            props.setProperty("BooleanLiteral.color", "maroon3");
            // Default visit values
            props.setProperty("TypeDeclaration.visit", "true");
            props.setProperty("MethodDeclaration.visit", "true");
            props.setProperty("AnonymousClassDeclaration.visit", "false");
            props.setProperty("SingleVariableDeclaration.visit", "true");
            props.setProperty("AssertStatement.visit", "true");
            props.setProperty("Block.visit", "false");
            props.setProperty("BreakStatement.visit", "true");
            props.setProperty("ContinueStatement.visit", "true");
            props.setProperty("DoStatement.visit", "true");
            props.setProperty("EnhancedForStatement.visit", "true");
            props.setProperty("ExpressionStatement.visit", "true");
            props.setProperty("ForStatement.visit", "true");
            props.setProperty("IfStatement.visit", "true");
            props.setProperty("LabeledStatement.visit", "true");
            props.setProperty("ReturnStatement.visit", "true");
            props.setProperty("SwitchStatement.visit", "true");
            props.setProperty("SynchronizedStatement.visit", "false");
            props.setProperty("ThrowStatement.visit", "true");
            props.setProperty("TryStatement.visit", "true");
            props.setProperty("CatchClause.visit", "true");
            props.setProperty("TypeDeclarationStatement.visit", "false");
            props.setProperty("VariableDeclarationStatement.visit", "true");
            props.setProperty("WhileStatement.visit", "true");
            props.setProperty("YieldStatement.visit", "false");
            props.setProperty("ArrayAccess.visit", "false");
            props.setProperty("ArrayCreation.visit", "false");
            props.setProperty("ArrayInitializer.visit", "false");
            props.setProperty("Assignment.visit", "true");
            props.setProperty("VariableDeclarationExpression.visit", "true");
            props.setProperty("TypeMethodReference.visit", "false");
            props.setProperty("TypeLiteral.visit", "false");
            props.setProperty("ThisExpression.visit", "false");
            props.setProperty("SuperMethodInvocation.visit", "true");
            props.setProperty("SuperMethodReference.visit", "false");
            props.setProperty("SuperFieldAccess.visit", "true");
            props.setProperty("StringLiteral.visit", "false");
            props.setProperty("PrefixExpression.visit", "false");
            props.setProperty("PostfixExpression.visit", "false");
            props.setProperty("ParenthesizedExpression.visit", "false");
            props.setProperty("NumberLiteral.visit", "false");
            props.setProperty("NullLiteral.visit", "false");
            props.setProperty("MethodRef.visit", "false");
            props.setProperty("MethodInvocation.visit", "true");
            props.setProperty("LambdaExpression.visit", "false");
            props.setProperty("InstanceofExpression.visit", "false");
            props.setProperty("InfixExpression.visit", "false");
            props.setProperty("FieldAccess.visit", "true");
            props.setProperty("ExpressionMethodReference.visit", "false");
            props.setProperty("CreationReference.visit", "false");
            props.setProperty("ConditionalExpression.visit", "false");
            props.setProperty("ClassInstanceCreation.visit", "true");
            props.setProperty("CharacterLiteral.visit", "false");
            props.setProperty("CastExpression.visit", "false");
            props.setProperty("BooleanLiteral.visit", "false");
            props.setProperty("ConstructorInvocation.visit", "true");
            props.setProperty("SuperConstructorInvocation.visit", "true");
            props.setProperty("SwitchCase.visit", "true");
            props.setProperty("VariableDeclarationFragment.visit", "true");
            props.setProperty("FieldDeclaration.visit", "true");
            props.setProperty("EnumDeclaration.visit", "true");
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
