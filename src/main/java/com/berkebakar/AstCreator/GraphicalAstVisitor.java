package com.berkebakar.AstCreator;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class GraphicalAstVisitor extends ASTVisitor {
    private final MutableGraph graph;

    public GraphicalAstVisitor() {
        super(false);
        graph = mutGraph("AST").setDirected(true);
    }

    public MutableGraph getGraph() {
        return graph;
    }

    private String getNodeLabel(ASTNode node) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(node.getClass().getSimpleName()).append("\n");
        switch (node.getNodeType()) {
            case ASTNode.TYPE_DECLARATION -> {
                TypeDeclaration typeDeclaration = (TypeDeclaration) node;
                stringBuilder.append("Name: ").append(typeDeclaration.getName().getIdentifier());
                stringBuilder.append("Kind: ").append(typeDeclaration.isInterface() ? "Interface" : "Class");
            }
            case ASTNode.METHOD_DECLARATION -> {
                MethodDeclaration methodDeclaration = (MethodDeclaration) node;
                stringBuilder.append("Name: ").append(methodDeclaration.getName().getIdentifier()).append("\n");
                stringBuilder.append(formatListProperties("Parameters", methodDeclaration.parameters()));
                stringBuilder.append("Return Type: ").append(methodDeclaration.getReturnType2().toString());
            }
            case ASTNode.SINGLE_VARIABLE_DECLARATION -> {
                SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) node;
                stringBuilder.append(formatListProperties("Modifier", variableDeclaration.modifiers()));
                stringBuilder.append("Name: ").append(variableDeclaration.getName().getIdentifier()).append("\n");
                stringBuilder.append("Type: ").append(variableDeclaration.getType().toString());
            }
            case ASTNode.ASSERT_STATEMENT -> {
                AssertStatement assertStatement = (AssertStatement) node;
                stringBuilder.append("Expression: ").append(assertStatement.getExpression().toString()).append("\n");
                stringBuilder.append("Message: ").append(assertStatement.getMessage());
            }
            case ASTNode.DO_STATEMENT -> {
                DoStatement doStatement = (DoStatement) node;
                stringBuilder.append("Condition: ").append(doStatement.getExpression().toString());
            }
            case ASTNode.ENHANCED_FOR_STATEMENT -> {
                EnhancedForStatement enhancedForStatement = (EnhancedForStatement) node;
                stringBuilder.append("Expression: ").append(enhancedForStatement.getExpression().toString());
            }
            case ASTNode.EXPRESSION_STATEMENT -> {
                ExpressionStatement expressionStatement = (ExpressionStatement) node;
                stringBuilder.append("Expression: ").append(expressionStatement.getExpression().toString());
            }
            case ASTNode.FOR_STATEMENT -> {
                ForStatement forStatement = (ForStatement) node;
                stringBuilder.append("Condition: ").append(forStatement.getExpression().toString()).append("\n");
                stringBuilder.append(formatListProperties("Update", forStatement.updaters()));
            }
            case ASTNode.IF_STATEMENT -> {
                IfStatement ifStatement = (IfStatement) node;
                stringBuilder.append("Condition: ").append(ifStatement.getExpression().toString());
            }
            case ASTNode.LABELED_STATEMENT -> {
                LabeledStatement labeledStatement = (LabeledStatement) node;
                stringBuilder.append("Label: ").append(labeledStatement.getLabel().getIdentifier());
            }
            case ASTNode.RETURN_STATEMENT -> {
                ReturnStatement returnStatement = (ReturnStatement) node;
                stringBuilder.append("Returns: ").append(returnStatement.getExpression().toString());
            }
            case ASTNode.SWITCH_CASE -> {
                SwitchCase switchCase = (SwitchCase) node;
                stringBuilder.append("isDefaultCase: ").append(switchCase.isDefault());
                stringBuilder.append(formatListProperties("Condition", switchCase.expressions()));
            }
            case ASTNode.SWITCH_STATEMENT -> {
                SwitchStatement switchStatement = (SwitchStatement) node;
                stringBuilder.append("Condition: ").append(switchStatement.getExpression().toString());
            }
            case ASTNode.SYNCHRONIZED_STATEMENT -> {
                SynchronizedStatement synchronizedStatement = (SynchronizedStatement) node;
                stringBuilder.append("Lock: ").append(synchronizedStatement.getExpression().toString());
            }
            case ASTNode.THROW_STATEMENT -> {
                ThrowStatement throwStatement = (ThrowStatement) node;
                stringBuilder.append("Throws:").append(throwStatement.getExpression().toString());
            }
            case ASTNode.VARIABLE_DECLARATION_STATEMENT -> {
                VariableDeclarationStatement variableDeclaration = (VariableDeclarationStatement) node;
                stringBuilder.append(formatListProperties("Modifier", variableDeclaration.modifiers()));
                stringBuilder.append("Type: ").append(variableDeclaration.getType().toString()).append("\n");
                stringBuilder.append("Name: ");
                List fragments = variableDeclaration.fragments();
                for (int i = 0; i < fragments.size(); i++){
                    stringBuilder.append(((VariableDeclarationFragment) fragments.get(i)).getName()).append(i == fragments.size() - 1 ? "" : ", ");
                }
            }
            case ASTNode.WHILE_STATEMENT -> {
                WhileStatement whileStatement = (WhileStatement) node;
                stringBuilder.append("Condition: ").append(whileStatement.getExpression().toString());
            }
        }

        return stringBuilder.toString();
    }

    private void addNode(ASTNode node) {
        graph.add(mutNode(Integer.toString(node.hashCode())).add(Label.of(getNodeLabel(node))));
    }

    private void addEdge(ASTNode source, ASTNode target) {
        if (source != null) // no need to add edge if there is no parent
            graph.add(mutNode(Integer.toString(source.hashCode())).addLink(mutNode(Integer.toString(target.hashCode()))));
    }

    private ASTNode getParentInGraph(ASTNode node) {
        ASTNode currentParentNode = node.getParent();

        while (currentParentNode != null) {

            for (MutableNode currentNode : graph.nodes()) {
                if (currentNode.name().equals(Label.of(Integer.toString(currentParentNode.hashCode())))) {
                    return currentParentNode;
                }
            }
            currentParentNode = currentParentNode.getParent();
        }

        return null;
    }

    private String formatListProperties(String propertyName, List propertyValueList){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(propertyName).append(": ");
        for (int i=0; i < propertyValueList.size(); i++){
            stringBuilder.append(propertyValueList.get(i).toString()).append(i == propertyValueList.size() - 1 ? "" : ", ");
        }
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        if (node.getParent() != node.getRoot()) { // Do not add the root type declaration
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(SingleVariableDeclaration node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(AssertStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(Block node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(BreakStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(ContinueStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(DoStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(ExpressionStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(ForStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(IfStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(LabeledStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(ReturnStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(SwitchStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(SynchronizedStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(ThrowStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(TryStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(CatchClause node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(TypeDeclarationStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(WhileStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(YieldStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(EmptyStatement node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }
}
