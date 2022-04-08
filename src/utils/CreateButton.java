package utils;

import javax.swing.JButton;

import controller.calculators.CalculatorController;
import view.components.CCDisplay;
/**
 * 
 * 
 *
 */
public final class CreateButton {
    /**
     * 
     */
    private CreateButton() {
    }
   /**
    * 
    * @param btnName
    * @param opName
    * @param controller
    * @param display
    * @return aaaa
    */

   public static JButton createOpButton(final String btnName, final String opName, final CalculatorController controller, final CCDisplay display) {
       final JButton btn = new JButton(btnName);
       final boolean isBinary = controller.isBinaryOperator(opName);
       btn.addActionListener(e -> {
           if (isBinary) {
               display.updateText(controller.getManager().getCurrentState().stream().reduce("", (a, b) -> a + b) + btnName + " ");
           } else {
               display.updateText(btnName + "(" + controller.getManager().getCurrentState().stream().reduce("", (a, b) -> a + b) + ")");
           }
           controller.getManager().read(opName);
       });
       return btn;
   }


}