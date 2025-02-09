package controller.calculators.logics;

import java.util.ArrayList;
import java.util.List;

import controller.calculators.CalculatorController;
/**
 * 
 *
 *
 */
public class InputFormatterLogicsImpl implements InputFormatterLogics {
    private final CalculatorController controller;
    /**
     * 
     * @param controller
     */
    public InputFormatterLogicsImpl(final CalculatorController controller) {
        this.controller = controller;
    }
    /**
     * @param input
     */
    public void read(final String input) {
        /*se il numero premuto prima è un numero allora l'operatore unario wrappa il contenuto con il proprio unario
         * 1+2 => 1+(1/(2))       1+2 => 1+((2)^2)
         * se il numero premuto prima non è un numero allora l'operatore unario viene concatenato con una parentesi
         * 1+ => 1+(1/(1))        1+ => 1+(0)^2
         */
        if (controller.isUnaryOperator(input)) {
            if (this.isPreviousValueANumber()) {
                this.wrapNumberInOperator(input);
            } else {
                this.readInvalidOperand(input);
            }
        } else {
            controller.getManager().memory().read(input);
        }
        this.checkParenthesis();
    }

    @Override
    public void deleteLast() {
        controller.getManager().memory().deleteLast();
    }

    @Override
    public void calculate() {
        controller.getManager().engine().calculate();
    }

    private void checkParenthesis() {
        final List<String> state = new ArrayList<>(controller.getManager().memory().getCurrentState());
        for (int i = 0; i < state.size(); i++) {
            if (i != state.size() - 1 && "(".equals(state.get(i)) && ")".equals(state.get(i + 1))) {
                    state.remove(i);
                    state.remove(i);
            }
        }
        controller.getManager().memory().clear();
        controller.getManager().memory().readAll(state);
    }

    private void wrapNumberInOperator(final String op) {
        final List<String> state = new ArrayList<>(controller.getManager().memory().getCurrentState());
        int index = state.size() - 1;
        while (index >= 0) {
            //[1+1.1111,square]
            if (!this.isNumber(index)) {
                index++;
                break;
            }
            index--;
        }
        index = index == -1 ? 0 : index;
        state.add(index, "(");
        if ("x²".equals(op)) {
            state.add(")");
            state.add(op);
        } else {
            state.add(index, op);
            state.add(")");
        }
        controller.getManager().memory().clear();
        controller.getManager().memory().readAll(state);
    }

    private boolean isPreviousValueANumber() {
        final List<String> state = new ArrayList<>(controller.getManager().memory().getCurrentState());
        return this.isNumber(state.size() - 1);
    }

    private boolean isNumber(final int i) {
        final List<String> state = controller.getManager().memory().getCurrentState();
        try {
            if (state.isEmpty()) { //se lo state è vuoto allora devo ritornare che prima non c'è un numero
                return false;
            }
            if (".".equals(state.get(i)) || ")".equals(state.get(i))) {
               return true; 
            }
            Double.parseDouble(state.get(i));
            return true; 
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void readInvalidOperand(final String op) {
        switch (op) {
            case "x²":
                controller.getManager().memory().read("(");
                controller.getManager().memory().read("0");
                controller.getManager().memory().read(")");
                controller.getManager().memory().read(op);
                break;
            case "1/x":
                controller.getManager().memory().read("(");
                controller.getManager().memory().read(op);
                controller.getManager().memory().read("1");
                controller.getManager().memory().read(")");
                break;
            case "√":
                controller.getManager().memory().read("(");
                controller.getManager().memory().read(op);
                controller.getManager().memory().read("0");
                controller.getManager().memory().read(")");
                break;
            default :
                break;
        }

    }

}
