package com.berkebakar.AstCreator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AstCreator {
    public static void createAst(Path inputPath, Path outputPath) {
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
            parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);

            TypeDeclaration typeDeclaration = (TypeDeclaration) parser.createAST(null);

            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            JsonObject json = new JsonObject();

            // Add information about the method declaration
            MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
            json.addProperty("name", methodDeclaration.getName().getIdentifier());
            json.addProperty("returnType", methodDeclaration.getReturnType2().toString());
            json.addProperty("constructor", methodDeclaration.isConstructor());
            json.addProperty("varargs", methodDeclaration.isVarargs());

            List<String> modifiers = new ArrayList<>();
            for (Object extendedModifier : methodDeclaration.modifiers()) {
                IExtendedModifier modifier = (IExtendedModifier) extendedModifier;
                if (modifier.isModifier()) {
                    modifiers.add(modifier.toString());
                }
            }
            json.add("modifiers", gson.toJsonTree(modifiers));

            // Add information about the method parameters
            List<JsonObject> parameters = new ArrayList<>();
            for (SingleVariableDeclaration parameter : (List<SingleVariableDeclaration>) methodDeclaration.parameters()) {
                JsonObject parameterJson = new JsonObject();
                parameterJson.addProperty("name", parameter.getName().getIdentifier());
                parameterJson.addProperty("type", parameter.getType().toString());
                parameters.add(parameterJson);
            }
            json.add("parameters", gson.toJsonTree(parameters));

            // Add information about the method body statements
            Block body = methodDeclaration.getBody();
            if (body != null) {
                JsonArray statements = new JsonArray();
                for (Object statement : body.statements()) {
                    JsonObject statementJson = new JsonObject();
                    statementJson.addProperty("type", statement.getClass().getSimpleName());
                    statementJson.addProperty("text", StringEscapeUtils.unescapeJava(statement.toString().replaceAll("[\\t\\n\\r]+", "").translateEscapes()));
                    statements.add(statementJson);
                }
                json.add("statements", statements);
            }
            String contentsJson = gson.toJson(json);
            Path outputFilePath = outputPath.resolve(inputPath.getFileName().toString().replace(".java", ".json"));

            if (!Files.exists(outputFilePath)){
                Files.createFile(outputFilePath);
            }
            Files.write(outputFilePath, contentsJson.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

            System.out.printf("AST exported successfully as %s to %s%n", outputFilePath.getFileName().toString(), outputPath.toRealPath());
        } catch (IOException e) {
            System.err.println("An error occurred while writing to file: " + e.getMessage());
        }
    }
}
