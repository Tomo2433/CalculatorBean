package calculatorbean;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanProperty;
import java.io.Serializable;
import java.text.DecimalFormat;

public class CalculatorBean extends JPanel implements Serializable {

    private JTextField display;
    private JPanel buttonsPanel;
    private Double currentValue = null;
    private String lastOperation;
    private String checkError;
    private boolean isEquals = false;
    private boolean isContinueOperation = false;
    private boolean isWelcome = true;

    // Dodane pola do dostosowania wyglądu
    private Color buttonBackgroundColor = Color.LIGHT_GRAY;
    private Color buttonForegroundColor = Color.BLACK;
    private Color defaultColor = Color.WHITE;
    private Color plusColor = Color.GREEN;
    private Color minusColor = Color.RED;
    private String welcomeMessage = "WITAJ!";
    private DecimalFormat decimalFormat;
    private int precision = 2;

    public CalculatorBean() {
        initComponents();
        setPrecision(precision);
    }

    private void initComponents() {
        display = new JTextField(10);
        display.setEditable(false);

        // Dodawanie przycisków do interfejsu
        String[] buttonLabels = {"7", "8", "9", "/", "4", "5", "6", "*", "1", "2", "3", "-", "0", ".", "=", "+"};
        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new java.awt.GridLayout(4, 4));

        for (String buttonLabel : buttonLabels) {
            JButton button = new JButton(buttonLabel);
            button.setBackground(buttonBackgroundColor);
            button.setForeground(buttonForegroundColor);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton source = (JButton) e.getSource();
                    String buttonText = source.getText();

                    // Obsługa przycisków
                    if (Character.isDigit(buttonText.charAt(0)) || buttonText.equals(".")) {
                        if(isWelcome) {
                            isWelcome = false;
                            display.setText("");
                        }
                        if(isEquals) {
                            if(!isContinueOperation){
                                currentValue = null;
                                display.setText("");
                                isEquals = false;
                            }
                            isContinueOperation = false;
                            display.setText("");
                        }
                        checkError = display.getText();
                        if(checkError.equals("Error")) {
                            display.setText("");
                        }
                        display.setText(display.getText() + buttonText);

                    } else if ("+-*/".contains(buttonText)) {
                        isWelcome = false;
                        lastOperation = buttonText;
                        if(isEquals) isContinueOperation = true;
                        performOperation(); 
                    } else if ("=".equals(buttonText)) {
                        isEquals = true;
                        performOperation();
                        //lastOperation = null;
                    }
                }
            });
            buttonsPanel.add(button);
        }
        display.setFont(new java.awt.Font("Segoe Print", 0, 24));
        if(isWelcome) display.setText(welcomeMessage);
        // Układ interfejsu
        setLayout(new java.awt.BorderLayout());
        add(display, java.awt.BorderLayout.NORTH);
        add(buttonsPanel, java.awt.BorderLayout.CENTER);
    }
    private void performOperation() {
        if (lastOperation != null && currentValue != null) {
            if (isContinueOperation) {
                display.setText("");
                return;
            }
            double inputValue = Double.parseDouble(display.getText());
            switch (lastOperation) {
                case "+":
                    currentValue += inputValue;
                    display.setText("");
                    break;
                case "-":
                    if(inputValue > 0) inputValue *= -1;
                    currentValue += inputValue;
                    display.setText("");
                    break;
                case "*":
                    currentValue *= inputValue;
                    display.setText("");
                    break;
                case "/":
                    if (inputValue != 0) {
                        currentValue /= inputValue;
                        display.setText(String.valueOf(decimalFormat.format(currentValue)));
                    } else {
                        display.setText("Error");
                        return;
                    }
                    break;
            }
            if(isEquals) {
                display.setText(String.valueOf(decimalFormat.format(currentValue)));
                if(currentValue < 0) { 
                    display.setBackground(minusColor);
                } else if(currentValue > 0) {
                    display.setBackground(plusColor);
                } else {
                    display.setBackground(defaultColor);
                }
            }

        } else {
            if(isEquals){
                display.setText("Error");
                isEquals = false;
                return;
            }
            if(lastOperation == "-") {
                try{
                    currentValue = Double.parseDouble(display.getText());
                } catch (NumberFormatException e){
                    currentValue = null;
                } 
                display.setText("-");
                //lastOperation = null;
                return;
            }
            currentValue = Double.parseDouble(display.getText());
            display.setText("");
        }
    }
            
    @BeanProperty
    public Color getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }
    @BeanProperty
    public Color getButtonForegroundColor() {
        return buttonForegroundColor;
    }
    @BeanProperty
    public String getWelcomeMessage() {
        return welcomeMessage;
    }
    @BeanProperty
    public Color getDefaultColor() {
        return defaultColor;
    }
    @BeanProperty
    public Color getPlusColor() {
        return plusColor;
    }
    @BeanProperty
    public Color getMinusColor() {
        return minusColor;
    }
    @BeanProperty
    public int getPrecision() {
        return precision;
    }
    // Dodane metody ustawiające dla dostosowywania wyglądu
    @BeanProperty
    public void setButtonBackgroundColor(Color color) {
        buttonBackgroundColor = color;
        updateButtonColors();
    }
    @BeanProperty
    public void setButtonForegroundColor(Color color) {
        buttonForegroundColor = color;
        updateButtonColors();
    }
    @BeanProperty
    public void setWelcomeMessage(String m) {
        welcomeMessage = m;
        display.setText(welcomeMessage);
    }
    @BeanProperty
    public void setDefaultColor(Color color) {
        defaultColor = color;
        display.setBackground(color);
    }
    @BeanProperty
    public void setPlusColor(Color color) {
        plusColor = color;
    }
    @BeanProperty
    public void setMinusColor(Color color) {
        minusColor = color;
    }
    @BeanProperty
    public void setPrecision(int precision) {
        decimalFormat = new DecimalFormat("#." + "0".repeat(precision));
    }
    
    private void updateButtonColors() {
        // Aktualizacja kolorów przycisków
        for (java.awt.Component component : buttonsPanel.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                button.setBackground(buttonBackgroundColor);
                button.setForeground(buttonForegroundColor);
            }
        }
    }
}

