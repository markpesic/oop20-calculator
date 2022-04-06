package utils.calculate;

import utils.ast.*;
import utils.tokens.*;

/**
 * @author pesic
 *
 */
public class EvaluatorAST {

    private Operation evaluateSubTree(final AbstractSyntaxNode node) {
        final Token t = node.getToken();
        switch (t.getTypeToken()) {
        case NUMBER:
            return new Constant(String.valueOf(((NumberToken) t).getValueToken()));
        case CONSTANT:
            return evaluateConstant(node);
        case VARIABLE:
            return new SimpleVar();
        case FUNCTION:
            return evaluateFunction(node);
        case OPERATOR:
            return evaluateOperator(node);
        default:
            throw new IllegalStateException("Invalid Token Expression");
        }
    }

    private Operation evaluateConstant(final AbstractSyntaxNode node) {
        final ConstantToken token = (ConstantToken) node.getToken();
        switch (token.getSymbol()) {
        case "pi":
            return new Constant(String.valueOf(Math.PI));
        case "e":
            return new Constant(String.valueOf(Math.E));
        default:
            throw new IllegalStateException("The constant doesn't exist");
        }
    }

    private Operation evaluateFunction(final AbstractSyntaxNode node) {
        if (node.getRight().isEmpty()) {
            throw new IllegalStateException("Function needs arguments");
        }
        final Operation right = evaluateSubTree(node.getRight().get());
        final FunctionToken token = (FunctionToken) node.getToken();
        switch (token.getSymbol()) {
        case "acos":
            return new Acos(right);
        case "asin":
            return new Asin(right);
        case "atan":
            return new Atan(right);
        case "log":
            return new Log(right);
        case "cos":
            return new Cos(right);
        case "sin":
            return new Sin(right);
        case "sqrt":
            return new Sqrt(right);
        case "tan":
            return new Tan(right);
        case "exp":
            return new Exp(right);
        case "abs":
            return new Abs(right);
        default:
            throw new IllegalStateException("Function error");
        }
    }

    private Operation evaluateOperator(final AbstractSyntaxNode node) {

        if (node.getLeft().isPresent() && node.getRight().isPresent()) {
            return evaluateBinaryOperator(node);
        } else if (node.getRight().isPresent() && node.getLeft().isEmpty()) {
            return evaluateUnaryOperator(node);
        }

        throw new IllegalStateException("Error with operator: " + node.getToken().getSymbol() + " and node.left: "
                + node.getLeft().isPresent() + " and node.right: " + node.getRight().isPresent());
    }

    private Operation evaluateUnaryOperator(final AbstractSyntaxNode node) {
        final Operation right = evaluateSubTree(node.getRight().get());
        final OperatorToken token = (OperatorToken) node.getToken();

        if (token.getSymbol().equals("-")) {
            return new Negate(right);
        }

        throw new IllegalStateException("Unary Operator doesn't work");
    }

    private Operation evaluateBinaryOperator(final AbstractSyntaxNode node) {
        final Operation right = evaluateSubTree(node.getRight().get());
        final Operation left = evaluateSubTree(node.getLeft().get());
        final OperatorToken token = (OperatorToken) node.getToken();
        switch (token.getSymbol()) {
        case "+":
            return new Addition(left, right);
        case "-":
            return new Subtraction(left, right);
        case "*":
            return new Product(left, right);
        case "/":
            return new Division(left, right);
        case "^":
            return new Pow(left, right);
        default:
            throw new IllegalStateException("Unary Operator doesn't work");
        }
    }
    /**
     * @param root
     * @return c
     */
    public Operation evaluate(final AbstractSyntaxNode root) {
        if (root == null) {
            throw new IllegalStateException();
        }
        return evaluateSubTree(root);
    }

    public static void main(String[] args) {
        // (0)*((x)^(2.0))+(3.0)*(((x)^(2.0))*((0)*(log(x))+((2.0)*(1))/(x)))
        var parser = new ParserAST();
        var root = parser.parseToAST("3x+5");
        var eval = new EvaluatorAST();
        var result = eval.evaluate(root);
        System.out.println("Result: " + result.toString());
    }

}