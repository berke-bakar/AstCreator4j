package com.berkebakar.AstCreator;

import guru.nidi.graphviz.engine.*;
import guru.nidi.graphviz.model.MutableGraph;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class AstCreator {
    public static void createAst(Path inputPath, Path outputPath) {
        try {
            //TODO: Generalize the method to handle directories
            String sourceCode = Files.readString(inputPath);
            ASTParser parser = ASTParser.newParser(AST.JLS19);
            parser.setSource(sourceCode.toCharArray());

            Map<String, String> options = JavaCore.getOptions();
            options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_17);
            options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_17);
            options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_17);
            parser.setCompilerOptions(options);
            parser.setResolveBindings(true);
            parser.setBindingsRecovery(true);
            parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);

            TypeDeclaration typeDeclaration = (TypeDeclaration) parser.createAST(null);
            GraphicalAstVisitor visitor = new GraphicalAstVisitor();
            typeDeclaration.accept(visitor);

            //TODO: Make output type a command line argument with default value of PNG
            Path outputFilePath = outputPath.resolve(inputPath.getFileName().toString().replace(".java", ".png"));
            File outputFile = null;
            if (Files.notExists(outputFilePath)){
                outputFile = Files.createFile(outputFilePath).toFile();
            }
            else {
                outputFile = outputFilePath.toFile();
            }

            MutableGraph astGraph = visitor.getGraph();
            //TODO: Make width user defined from command line
            //TODO: Make node type coloring configurable from a file
            Graphviz.fromGraph(astGraph).width(1920).height(1080).render(Format.PNG).toFile(outputFile);

        } catch (IOException e) {
            System.err.println("An error occurred while writing to file: " + e.getMessage());
        }
    }
}
