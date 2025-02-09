package controller.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import controller.calculators.CalculatorController;
import utils.CalcException;
import utils.NumberFormatter;
import utils.Type;

/**
 * Class that implements methods to parse and execute mathematical expressions.
 */
public class CCEngine implements EngineInterface {

    private static final String SYNTAX_ERROR = "Syntax error";
    private final CalculatorController calcController;
    private static final String VARIABLE = "x";

    /**
     * Construct a new CCEngine that will use the given CalculatorController to perform the calculations.
     * @param calcController
     */
    public CCEngine(final CalculatorController calcController) {
        this.calcController = calcController;
    }

    @Override
    public double calculate(final List<String> input) throws CalcException {
        List<String> rpnInput;

        rpnInput = this.parseToRPN(this.unifyTerms(input));
        return this.evaluateRPN(rpnInput);
    }

    @Override
    public String calculateAndFormat(final List<String> input) throws CalcException {
        final double result = this.calculate(input);
        final int maxIntDigits = 10;
        final int maxDecDigits = 10;
        final int decimalThreshold = 5;

        return NumberFormatter.format(result, maxIntDigits, maxDecDigits, decimalThreshold);
    }

    /**
     * Parses an expression in infix notation, stored as a list of strings, to an equivalent expression in reverse polish notation.
     * This method uses the shunting-yard algorithm. The main reference was the pseudocode on the Wikipedia page https://en.wikipedia.org/wiki/Shunting-yard_algorithm.
     * @throws CalcException If there is a parenthesis mismatch.
     */
    @Override
    public List<String> parseToRPN(final List<String> infix) throws CalcException {

        final List<String> output = new ArrayList<>();
        final Stack<String> stack = new Stack<>();

        final var it = infix.iterator();
        while (it.hasNext()) {

            final String token = it.next();
            if (isNumber(token) || VARIABLE.equals(token)) {
                output.add(token);
            } else if (isUnaryOperator(token)) {
                stack.add(token);
            } else if (isBinaryOperator(token)) {

                if (!stack.isEmpty()) {
                    String o2 = stack.lastElement();
                    while (!"(".equals(o2) && (precedence(o2) > precedence(token) || precedence(o2) == precedence(token) && type(token) == Type.LEFT)) {
                        output.add(stack.pop());
                        if (stack.isEmpty()) {
                            break;
                        }
                        o2 = stack.lastElement();
                    }
                }

                stack.add(token);

            } else if ("(".equals(token)) {
                stack.add(token);
            } else if (")".equals(token)) {
                while (!stack.isEmpty() && !"(".equals(stack.lastElement())) {
                    output.add(stack.pop());
                }
                if (!stack.isEmpty() && "(".equals(stack.lastElement())) {
                    stack.pop();
                } else {
                    throw new CalcException("Parenthesis mismatch");
                }
                if (!stack.isEmpty() && isUnaryOperator(stack.lastElement())) {
                    output.add(stack.pop());
                }
            }

        }

        while (!stack.isEmpty()) {
            if ("(".equals(stack.lastElement())) {
                throw new CalcException("Parenthesis mismatch");
            }
            output.add(stack.pop());
        }
        return output;
    }

    private List<String> unifyTerms(final List<String> input) throws CalcException {
        final List<String> unified = new ArrayList<>();

        final List<String> currentNumber = new ArrayList<>();

        for (final String s : input) {
            if (isNumber(s) || ".".equals(s)) {
                currentNumber.add(s);
            } else {
                 if (!currentNumber.isEmpty()) {
                    double actualNum;
                    actualNum = convert(currentNumber);
                    currentNumber.clear();
                    unified.add(String.valueOf(actualNum));
                 }
                unified.add(s);
            }
        }

        if (!currentNumber.isEmpty()) {
            final double actualNum = convert(currentNumber);
            currentNumber.clear();
            unified.add(String.valueOf(actualNum));
        }
        return unified;
    }

    private double convert(final List<String> currentNumber) throws CalcException {
        if (currentNumber.stream().filter(s -> ".".equals(s)).count() > 1) {
            throw new CalcException(SYNTAX_ERROR);
        }
        final String num = currentNumber.stream().reduce("", (a, b) -> a + b);
        return Double.parseDouble(num);

    }

    /**
     * Evaluates the result of an expression in reverse polish notation.
     * The algorithm is based on the implementation on the Rosetta Code web page https://rosettacode.org/wiki/Parsing/RPN_calculator_algorithm#Java_2.
     * @param rpn List of string representing the expression to evaluate.
     * @return double result of the expression.
     * @throws CalcException If the expression contains too many operands.
     */
    private double evaluateRPN(final List<String> rpn) throws CalcException {
        final Stack<Double> stack = new Stack<>();

        final var it = rpn.iterator();
        while (it.hasNext()) {
            final String token = it.next();

            if (isNumber(token)) {
                stack.add(Double.valueOf(token));
            } else if (isBinaryOperator(token)) {
                if (stack.size() < 2) {
                    throw new CalcException(SYNTAX_ERROR);
                }
                final double secondOperand = Double.valueOf(stack.pop());
                final double firstOperand = Double.valueOf(stack.pop());
                stack.add(getCalculator().applyBinaryOperation(token, firstOperand, secondOperand));
            } else if (isUnaryOperator(token)) {
                if (stack.isEmpty()) {
                    throw new CalcException(SYNTAX_ERROR);
                }
                final double firstOperand = Double.valueOf(stack.pop());
                stack.add(getCalculator().applyUnaryOperation(token, firstOperand));
            }
        }

        if (stack.size() > 1 || stack.isEmpty()) {
            throw new CalcException(SYNTAX_ERROR);
        }
        return stack.pop();
    }

    private CalculatorController getCalculator() {
        return this.calcController;
    }

    private boolean isUnaryOperator(final String token) {
        return getCalculator().isUnaryOperator(token);
    }

    private boolean isBinaryOperator(final String token) {
        return getCalculator().isBinaryOperator(token);
    }

    private boolean isNumber(final String token) {
        try {
            Double.valueOf(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private Type type(final String token) {
        return getCalculator().getType(token);
    }

    private int precedence(final String token) {
        return getCalculator().getPrecedence(token);
    }

}
