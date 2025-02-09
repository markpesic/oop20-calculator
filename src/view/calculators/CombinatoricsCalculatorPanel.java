package view.calculators;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;

import controller.calculators.logics.CombinatoricsLogics;
import controller.calculators.logics.CombinatoricsLogicsImpl;
import utils.CCColors;
import utils.CustomJToolTip;
import view.components.CCDisplay;
import view.components.CCNumPad;

/**
 * 
 * Combinatorics Calculator GUI.
 *
 */
public class CombinatoricsCalculatorPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * It creates a BorderLayout panel: the display is North, the buttons are in the center and the label with examples for the operations is South.
     */
    public CombinatoricsCalculatorPanel() {
        final CombinatoricsLogics logics = new CombinatoricsLogicsImpl();
        final var display = new CCDisplay();
        this.setLayout(new BorderLayout());
        this.add(display, BorderLayout.NORTH);
        final var explLabel = new JLabel();

        final ActionListener btnAl = e -> {
            final var btn = (JButton) e.getSource();
            display.updateText(logics.numberAction(btn.getText()));
        };

        final ActionListener calculateAl = e -> {
            display.updateUpperText(logics.calculateAction());
            display.updateText(logics.getBufferToString());
            explLabel.setText("");
        };

        final ActionListener backspaceAl = e -> {
            display.updateText(logics.backspaceAction());
        };

        final var numpad = new CCNumPad(btnAl, calculateAl, backspaceAl);
        numpad.getButtons().get("(").setEnabled(false);
        numpad.getButtons().get(")").setEnabled(false);
        numpad.getButtons().get(".").setEnabled(false);
        this.add(numpad, BorderLayout.CENTER);
        this.add(explLabel, BorderLayout.SOUTH);
        this.add(new OperationsPanel(logics, display, explLabel), BorderLayout.EAST);
    }

    /**
     * 
     * JPanel containing the buttons of the operations(and the explanation buttons).
     *
     */
    static class OperationsPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        private final String sep = File.separator;
        private final String directory = System.getProperty("user.dir") + this.sep + "resources" + this.sep;

        OperationsPanel(final CombinatoricsLogics logics, final CCDisplay display, final JLabel explLabel) {
            this.setLayout(new GridLayout(8, 2));
            for (final CombinatoricsLogics.Operations op : CombinatoricsLogics.Operations.values()) {
                this.createOpButton(op.getOpBtnName(), op.getOpModelName(), logics, display);
                this.createExplButton(op.getOpBtnName(), explLabel);
            }
        }

        private void createOpButton(final String btnName, final String opName, final CombinatoricsLogics logics, final CCDisplay display) {
            final var btn = new JButton(btnName);
            btn.addActionListener(e -> {
                display.updateText(logics.operationAction(btnName, opName));
            });
            btn.setBackground(CCColors.OPERATION_BUTTON);
            this.add(btn);
        }

        private void createExplButton(final String opName, final JLabel explLabel) {
            final var file = this.directory + opName;
            final var btn = new JButton("?") {
                private static final long serialVersionUID = 1L;
                @Override
                public JToolTip createToolTip() {
                    return new CustomJToolTip(this);
                }
            };
            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
            btn.setToolTipText(this.readFromFile(file + "(TT)"));
            final String labelText = this.readFromFile(file);
            btn.addActionListener(e -> {
                explLabel.setText(explLabel.getText().equals(labelText) ? "" : labelText);
            });
            btn.setBackground(CCColors.EXPLANATION_BUTTON);
            this.add(btn);
        }

        private String readFromFile(final String file) {
            String result = "<html><p width=\"500\">";
            try (BufferedReader br = new BufferedReader(new FileReader(file + ".txt"))) {
                String str = br.readLine();
                while (str != null) {
                    result = result.concat(str + "<br>");
                    str = br.readLine();
                }
            } catch (FileNotFoundException e1) {
                return "FILE NOT FOUND " + file;
            } catch (IOException e1) {
                return "I/O ERROR";
            }
            return result + "</p></html>";
        }
    }
}
