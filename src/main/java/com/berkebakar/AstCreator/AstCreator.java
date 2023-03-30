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
import java.util.Properties;

public class AstCreator {
    public static void createAst(Path inputPath, Path outputPath, Properties properties) {
        try {
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
            GraphicalAstVisitor visitor = new GraphicalAstVisitor(properties);
            typeDeclaration.accept(visitor);

            Path outputFilePath = outputPath.resolve(inputPath.getFileName().toString().replace(".java", ".png"));
            File outputFile;
            if (Files.notExists(outputFilePath)) {
                outputFile = Files.createFile(outputFilePath).toFile();
            } else {
                outputFile = outputFilePath.toFile();
            }

            MutableGraph astGraph = visitor.getGraph();
            Graphviz.fromGraph(astGraph)
                    .width(Integer.parseInt(properties.getProperty("output.width", "224")))
                    .height(Integer.parseInt(properties.getProperty("output.height", "224")))
                    .render(Format.PNG).toFile(outputFile);

        } catch (IOException e) {
            System.err.println("An error occurred while writing to file: " + e.getMessage());
        } catch (GraphvizException e) {
            System.err.println("An error occurred while creating AST for " + inputPath.getFileName().toString() + " Message: " + e.getMessage());
        }
    }
}
