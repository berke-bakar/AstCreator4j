package com.berkebakar.AstCreator;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.eclipse.jdt.core.dom.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class GraphicalAstVisitor extends ASTVisitor {
    private final MutableGraph graph;
    private final Properties properties;

    private final boolean isExpressionsIncluded;

    public GraphicalAstVisitor(Properties properties) {
        super(false);
        this.graph = mutGraph("AST").setDirected(true);
        this.properties = properties;
        this.isExpressionsIncluded = properties.getProperty("output.includeExpressions", "true").equals("true");
    }

    public MutableGraph getGraph() {
        return graph;
    }

    private String getNodeLabel(ASTNode node) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(node.getClass().getSimpleName());

        // print details if user declared output.detailed in properties file
        if (Boolean.parseBoolean(properties.getProperty("output.detailed", "false"))) {
            stringBuilder.append("\n");
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
                    stringBuilder.append("Return Type: ").append(methodDeclaration.getReturnType2());
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
                    stringBuilder.append("Condition: ").append(forStatement.getExpression()).append("\n");
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
                    stringBuilder.append("Returns: ").append(returnStatement.getExpression());
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
                    for (int i = 0; i < fragments.size(); i++) {
                        stringBuilder.append(((VariableDeclarationFragment) fragments.get(i)).getName()).append(i == fragments.size() - 1 ? "" : ", ");
                    }
                }
                case ASTNode.WHILE_STATEMENT -> {
                    WhileStatement whileStatement = (WhileStatement) node;
                    stringBuilder.append("Condition: ").append(whileStatement.getExpression().toString());
                }
            }
        }

        return stringBuilder.toString();
    }

    private void addNode(ASTNode node) {
        if (properties.getProperty(node.getClass().getSimpleName() + ".visit", "false").equals("false")){
            return;
        }

        Shape nodeShape = ShapeMap.getShape(properties.getProperty(node.getClass().getSimpleName() + ".shape", "box"));
        Color nodeColor = Color.named(properties.getProperty(node.getClass().getSimpleName() + ".color", "white"));
        int width = Integer.parseInt(properties.getProperty("output.nodeWidth", "2"));
        int height = Integer.parseInt(properties.getProperty("output.nodeHeight", "2"));
        boolean fill = false;
        if (properties.getProperty("output.fillNodes", "true").equals("true")) {
            nodeColor = nodeColor.fill();
            fill = true;
        }

        graph.add(mutNode(Integer.toString(node.hashCode()))
                .add(Label.of(getNodeLabel(node)))
                .add(nodeColor)
                .add(nodeShape)
                .add(fill ? Style.FILLED : Style.SOLID)
                .add(Size.mode(Size.Mode.FIXED).size(width, height))
        );
    }

    private void addEdge(ASTNode source, ASTNode target) {
        if (source != null) // no need to add edge if there is no parent
            graph.add(mutNode(Integer.toString(source.hashCode())).addLink(mutNode(Integer.toString(target.hashCode()))));
    }

    private ASTNode getParentInGraph(ASTNode node) {
        if (properties.getProperty(node.getClass().getSimpleName() + ".visit", "false").equals("false")){
            return null;
        }

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

    private String formatListProperties(String propertyName, List propertyValueList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(propertyName).append(": ");
        for (int i = 0; i < propertyValueList.size(); i++) {
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
    public boolean visit(ArrayAccess node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(ArrayCreation node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(ArrayInitializer node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(Assignment node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(VariableDeclarationExpression node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(TypeMethodReference node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(TypeLiteral node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(ThisExpression node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(SuperMethodReference node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(SuperFieldAccess node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(StringLiteral node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(PrefixExpression node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(PostfixExpression node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(ParenthesizedExpression node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(NumberLiteral node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(NullLiteral node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(MethodRef node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(LambdaExpression node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(InstanceofExpression node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(InfixExpression node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(FieldAccess node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(ExpressionMethodReference node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(CreationReference node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(ConditionalExpression node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(CharacterLiteral node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(CastExpression node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(BooleanLiteral node) {
        if (isExpressionsIncluded){
            addNode(node);
            addEdge(getParentInGraph(node), node);
        }
        return true;
    }

    @Override
    public boolean visit(ConstructorInvocation node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(SuperConstructorInvocation node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(SwitchCase node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(VariableDeclarationFragment node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        addNode(node);
        addEdge(getParentInGraph(node), node);
        return true;
    }

    private static class ShapeMap {
        private static final Map<String, Shape> shapeMap = new HashMap<>();

        static {
            shapeMap.put("box", Shape.BOX);
            shapeMap.put("ellipse", Shape.ELLIPSE);
            shapeMap.put("oval", Shape.OVAL);
            shapeMap.put("circle", Shape.CIRCLE);
            shapeMap.put("point", Shape.POINT);
            shapeMap.put("egg", Shape.EGG);
            shapeMap.put("triangle", Shape.TRIANGLE);
            shapeMap.put("plaintext", Shape.PLAIN_TEXT);
            shapeMap.put("plain", Shape.PLAIN);
            shapeMap.put("diamond", Shape.DIAMOND);
            shapeMap.put("trapezium", Shape.TRAPEZIUM);
            shapeMap.put("parallelogram", Shape.PARALLELOGRAM);
            shapeMap.put("house", Shape.HOUSE);
            shapeMap.put("pentagon", Shape.PENTAGON);
            shapeMap.put("hexagon", Shape.HEXAGON);
            shapeMap.put("septagon", Shape.SEPTAGON);
            shapeMap.put("octagon", Shape.OCTAGON);
            shapeMap.put("doublecircle", Shape.DOUBLE_CIRCLE);
            shapeMap.put("doubleoctagon", Shape.DOUBLE_OCTAGON);
            shapeMap.put("tripleoctagon", Shape.TRIPLE_OCTAGON);
            shapeMap.put("invtriangle", Shape.INV_TRIANGLE);
            shapeMap.put("invtrapezium", Shape.INV_TRAPEZIUM);
            shapeMap.put("invhouse", Shape.INV_HOUSE);
            shapeMap.put("Mdiamond", Shape.M_DIAMOND);
            shapeMap.put("Msquare", Shape.M_SQUARE);
            shapeMap.put("Mcircle", Shape.M_CIRCLE);
            shapeMap.put("rect", Shape.RECT);
            shapeMap.put("rectangle", Shape.RECTANGLE);
            shapeMap.put("square", Shape.SQUARE);
            shapeMap.put("star", Shape.STAR);
            shapeMap.put("none", Shape.NONE);
            shapeMap.put("underline", Shape.UNDERLINE);
            shapeMap.put("cylinder", Shape.CYLINDER);
            shapeMap.put("note", Shape.NOTE);
            shapeMap.put("tab", Shape.TAB);
            shapeMap.put("folder", Shape.FOLDER);
            shapeMap.put("box3d", Shape.BOX_3D);
            shapeMap.put("component", Shape.COMPONENT);
            shapeMap.put("promoter", Shape.PROMOTER);
            shapeMap.put("cds", Shape.CDS);
            shapeMap.put("terminator", Shape.TERMINATOR);
            shapeMap.put("utr", Shape.UTR);
            shapeMap.put("primersite", Shape.PRIMER_SITE);
            shapeMap.put("restrictionsite", Shape.RESTRICTION_SITE);
            shapeMap.put("fivepoverhang", Shape.FIVE_P_OVERHANG);
            shapeMap.put("threepoverhang", Shape.THREE_P_OVERHANG);
            shapeMap.put("noverhang", Shape.N_OVERHANG);
            shapeMap.put("assembly", Shape.ASSEMBLY);
            shapeMap.put("signature", Shape.SIGNATURE);
            shapeMap.put("insulator", Shape.INSULATOR);
            shapeMap.put("ribosite", Shape.RIBO_SITE);
            shapeMap.put("rnastab", Shape.RNA_STAB);
            shapeMap.put("proteasesite", Shape.PROTEASE_SITE);
            shapeMap.put("proteinstab", Shape.PROTEIN_STAB);
            shapeMap.put("rpromoter", Shape.R_PROMOTER);
            shapeMap.put("rarrow", Shape.R_ARROW);
            shapeMap.put("larrow", Shape.L_ARROW);
            shapeMap.put("lpromoter", Shape.L_PROMOTER);
        }

        public static Shape getShape(String key){
            return shapeMap.getOrDefault(key, Shape.BOX);
        }
    }
}
