
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 *
 */
@SuppressWarnings("serial")
public final class JSONExpressionGenerator extends JFrame {

    /**
     *
     */
    private static final int TEXT_FIELD_LENGTH = 40;

    /**
     *
     */
    private final JButton save;

    /**
     *
     */
    private final JTextField input;

    /**
     *
     */
    private final InputEventHandler handler;

    /**
     *
     */
    private JSONExpressionGenerator() {
        super("JSON Expression Generator");
        this.setLookAndFeel();
        this.setLayout(new FlowLayout());
        this.add(new JLabel("Expression:"));
        this.input = new JTextField(TEXT_FIELD_LENGTH);
        this.handler = new InputEventHandler(this.input);
        this.input.addActionListener(this.handler);
        this.handler.setEnabled(false);

        Document doc = this.input.getDocument();
        if (doc instanceof AbstractDocument) {
            AbstractDocument abDoc = (AbstractDocument) doc;
            abDoc.setDocumentFilter(new DocumentInputFilter());
        }
        this.save = new JButton("Save JSON...");
        this.save.addActionListener(this.handler);
        this.save.setEnabled(false);

        this.input.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent event) {
//                JTextField input = (JTextField) event.getSource();
//                String value = input.getText();
//                System.out.println("keyPressed: " + value);
//            }

            @Override
            public void keyReleased(KeyEvent event) {
                JTextField input = (JTextField) event.getSource();
                String value = input.getText();
                if (JSONGenerator.canBeParsed(value)) {
                    JSONExpressionGenerator.this.save.setEnabled(true);
                    JSONExpressionGenerator.this.handler.setEnabled(true);
                } else {
                    JSONExpressionGenerator.this.save.setEnabled(false);
                    JSONExpressionGenerator.this.handler.setEnabled(false);
                }
                // System.out.println("keyReleased: " + value);
            }

//            @Override
//            public void keyTyped(KeyEvent event) {
//                JTextField input = (JTextField) event.getSource();
//                String value = input.getText();
//                System.out.println("keyTyped: " + value);
//            }
        });

        this.add(this.input);
        this.save.addActionListener(new InputEventHandler(this.input));
        this.add(this.save);
        this.pack();
    }

    /**
     * Tries to set the look and feel intelligently.
     */
    private void setLookAndFeel() {
        // Trying to set Nimbus look and feel
        boolean nimbusSelected = false;
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    nimbusSelected = true;
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Couldn't use Nimbus look and feel.");
        } catch (InstantiationException e) {
            System.err.println("Couldn't use Nimbus look and feel.");
        } catch (IllegalAccessException e) {
            System.err.println("Couldn't use Nimbus look and feel.");
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Couldn't use Nimbus look and feel.");
        }
        if (!nimbusSelected) {
            // Trying to use system look and feel
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't use system look and feel.");
            } catch (InstantiationException e) {
                System.err.println("Couldn't use system look and feel.");
            } catch (IllegalAccessException e) {
                System.err.println("Couldn't use system look and feel.");
            } catch (UnsupportedLookAndFeelException e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }
    }

    /**
     * @param args
     *            the command-line arguments; unused
     */
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JSONExpressionGenerator();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(false);
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
    }

    /**
     *
     */
    private final class InputEventHandler implements ActionListener {

        /**
         *
         */
        private JTextField input;

        /**
         *
         */
        private JFileChooser fc = new JFileChooser(new File("."));

        /**
         *
         */
        private boolean enabled = false;

        /**
         * @param input
         *            the JTextField
         */
        InputEventHandler(JTextField input) {
            this.input = input;
        }

        /**
         * Put method description here.
         *
         * @param enabled
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            final int suffixLength = 4;

            if (this.enabled) {
                String exp = this.input.getText();
                int retVal = this.fc
                        .showSaveDialog(JSONExpressionGenerator.this);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    String file = this.fc.getSelectedFile().getAbsolutePath();
                    if (!file.substring(file.length() - suffixLength)
                            .equalsIgnoreCase(".json")) {
                        file += ".json";
                    }
                    try {
                        JSONGenerator.save(exp, file);
                        JOptionPane.showMessageDialog(
                                JSONExpressionGenerator.this,
                                "Expression saved to file "
                                        + new File(file).getAbsolutePath());
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(
                                JSONExpressionGenerator.this,
                                "Can not save expression");
                    } catch (ParseException e) {
                        JOptionPane.showMessageDialog(
                                JSONExpressionGenerator.this,
                                "Can not parse expression");
                    }
                }
            }
        }
    }

    /**
     * This class will check for any invalid input and present a Dialog Message
     * to user, for entering appropriate input. you can let it make sound when
     * user tries to enter the invalid input. Do see the beep() part for that
     * inside the class's body.
     */
    private static final class DocumentInputFilter extends DocumentFilter {

        @Override
        public void insertString(FilterBypass fb, int offset, String text,
                AttributeSet as) throws BadLocationException {
            // System.out.println("insertString: " + text);
            int len = text.length();
            if (len > 0) {
                /*
                 * Here you can place your other checks that you need to perform
                 * and do add the same checks for replace method as well.
                 */
                char ch = text.charAt(len - 1);
                if (Character.isDigit(ch) || ch == '+' || ch == '-' || ch == '*'
                        || ch == '/' || ch == '%' || ch == '(' || ch == ')'
                        || ch == '^' || ch == 'v') {
                    super.insertString(fb, offset, text, as);
                } else {
//                    JOptionPane.showMessageDialog(null,
//                            "Please Enter a valid Integer Value.",
//                            "Invalid Input : ", JOptionPane.ERROR_MESSAGE);
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length,
                String text, AttributeSet as) throws BadLocationException {
            // System.out.println("replace: " + text);
            int len = text.length();
            if (len > 0) {
                char ch = text.charAt(len - 1);
                if (Character.isDigit(ch) || ch == '+' || ch == '-' || ch == '*'
                        || ch == '/' || ch == '%' || ch == '(' || ch == ')'
                        || ch == '^' || ch == 'v') {
                    super.replace(fb, offset, length, text, as);
                } else {
//                    JOptionPane.showMessageDialog(null,
//                            "Please Enter a valid Integer Value.",
//                            "Invalid Input : ", JOptionPane.ERROR_MESSAGE);
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }

    }

}
