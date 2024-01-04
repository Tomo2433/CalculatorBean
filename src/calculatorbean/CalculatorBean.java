package calculatorbean;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanProperty;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class CalculatorBean extends JPanel implements Serializable {
  
    private JTextField display;  
    private JPanel buttonsPanel;  
    private JButton cancelButton;
        
    private Double currentValue = null;
        
    private Double result;
        
    private String lastOperation;
        
    private String checkError;
        
    private boolean isEquals = false;
        
    private boolean isContinueOperation = false;
        
    private boolean isWelcome = true;
        
    private boolean isSoundOn = true;


        // Dodane pola do dostosowania wyglądu
        
    private Integer maxInputLength = 10;
        
    private Integer buttonFontSize = 12;
        
    private Integer displayFontSize = 24;
        
    private Color buttonBackgroundColor = Color.LIGHT_GRAY;
        
    private Color buttonForegroundColor = Color.BLACK;
        
    private Color defaultColor = Color.WHITE;
        
    private Color plusColor = Color.GREEN;
        
    private Color minusColor = Color.RED;
        
    private String welcomeMessage = "WITAJ!";
        
    private String buttonFont = "Tahoma";
        
    private String displayFont = "Segoe Print";
        
    private DecimalFormat decimalFormat;
        
    private Integer precision = 2;    
    private transient Clip additionSound;   
    private transient Clip subtractionSound;    
    private transient Clip divideSound;   
    private transient Clip multiplySound;   
    private transient Clip errorSound;  
    private transient Clip clickSound;  
    private transient Clip cancelSound;
    
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
        
        if(isSoundOn){
            try {
                AudioInputStream additionStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/boom.wav"));
                additionSound = AudioSystem.getClip();
                additionSound.open(additionStream);

                AudioInputStream subtractionStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/huh.wav"));
                subtractionSound = AudioSystem.getClip();
                subtractionSound.open(subtractionStream);

                AudioInputStream divideStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/sul.wav"));
                divideSound = AudioSystem.getClip();
                divideSound.open(divideStream);

                AudioInputStream multiplyStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/oof.wav"));
                multiplySound = AudioSystem.getClip();
                multiplySound.open(multiplyStream);

                AudioInputStream clickStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/click.wav"));
                clickSound = AudioSystem.getClip();
                clickSound.open(clickStream);

                AudioInputStream errorStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/error.wav"));
                errorSound = AudioSystem.getClip();
                errorSound.open(errorStream);
                
                AudioInputStream cancelStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/mineCave.wav"));
                cancelSound = AudioSystem.getClip();
                cancelSound.open(cancelStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (String buttonLabel : buttonLabels) {
            JButton button = new JButton(buttonLabel);
            button.setBackground(buttonBackgroundColor);
            button.setForeground(buttonForegroundColor);
            button.setFont(new Font(buttonFont, Font.PLAIN, buttonFontSize));
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
                        }
                        checkError = display.getText();
                        if(checkError.equals("Error")) {
                            display.setText("");
                        }
                        playSound(clickSound);
                        display.setBackground(defaultColor);
                        if (display.getText().length() < maxInputLength) {
                            display.setText(display.getText() + buttonText);
                        }

                    } else if ("+-*/".contains(buttonText)) {
                        isWelcome = false;
                        lastOperation = buttonText;
                        
                        if(buttonText == "+") playSound(additionSound);
                        if(buttonText == "-") playSound(subtractionSound);
                        if(buttonText == "*") playSound(multiplySound);
                        if(buttonText == "/") playSound(divideSound);
                     
                        //if(buttonText == "-") playSound(subtractionSound);
                        if(isEquals) isContinueOperation = true;
                        performOperation(); 
                    } else if ("=".equals(buttonText)) {
                        isEquals = true;
                        if(isContinueOperation) {
                            isContinueOperation = false;
                        }
                        performOperation();
                        //lastOperation = null;
                    }
                }
            });
            buttonsPanel.add(button);
        }
        cancelButton = new JButton("C");
        cancelButton.setBackground(buttonBackgroundColor);
        cancelButton.setForeground(buttonForegroundColor);
        cancelButton.setFont(new Font(buttonFont, Font.PLAIN, buttonFontSize));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSound(cancelSound);
                currentValue = null;
                display.setText("");
                lastOperation = null;
                display.setBackground(defaultColor);
            }
        });
        
        display.setFont(new java.awt.Font(displayFont, 0, displayFontSize));
        if(isWelcome) display.setText(welcomeMessage);
        // Układ interfejsu
        setLayout(new java.awt.BorderLayout());
        add(display, java.awt.BorderLayout.NORTH);
        add(buttonsPanel, java.awt.BorderLayout.CENTER);
        add(cancelButton, java.awt.BorderLayout.SOUTH);
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
                    currentValue = add(currentValue, inputValue);
                    display.setText("");
                    break;
                case "-":
                    currentValue = subtract(currentValue, inputValue);
                    display.setText("");
                    break;
                case "*":
                    currentValue = multiply(currentValue, inputValue);;
                    display.setText("");
                    break;
                case "/":
                    if (inputValue != 0) {
                        currentValue = divide(currentValue, inputValue);
                    } else {
                        display.setText("Error");
                        playSound(errorSound);
                        return;
                    }
                    break;
            }
            if(isEquals) {
                display.setText(String.valueOf(decimalFormat.format(currentValue)));
                result = currentValue;
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
                playSound(errorSound);
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
    public Integer getPrecision() {
        return precision;
    }
    @BeanProperty
    public boolean getIsSoundOn() {
        return isSoundOn;
    }
    @BeanProperty
    public Clip getAdditionSound() {
        return additionSound;
    }
    @BeanProperty
    public Clip getSubtractionSound() {
        return subtractionSound;
    }
    @BeanProperty
    public Clip getDivideSound() {
        return divideSound;
    }
    @BeanProperty
    public Clip getMultiplySound() {
        return multiplySound;
    }
    @BeanProperty
    public Clip getErrorSound() {
        return errorSound;
    }
    @BeanProperty
    public Clip getClickSound() {
        return clickSound;
    }
    @BeanProperty
    public Integer getButtonFontSize() {
        return buttonFontSize;
    }
    @BeanProperty
    public Integer getMaxInputLength() {
        return maxInputLength;
    }
    @BeanProperty
    public String getButtonFont() {
        return buttonFont;
    }
    @BeanProperty
    public String getDisplayFont() {
        return displayFont;
    }
    @BeanProperty
    public Integer getDisplayFontSize() {
        return displayFontSize;
    }
    
    
    public Double getResult() {
        return result;
    }
    
    public void setResult(Double result) {
        this.result = result;
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
    public void setAdditionSound(String file) {
        updateSound(file, additionSound);
    }
    @BeanProperty
    public void setSubtractionSound(String file) {
        updateSound(file, subtractionSound);
    }
    @BeanProperty
    public void setDivideSound(String file) {
        updateSound(file, divideSound);
    }
    @BeanProperty
    public void setMultiplySound(String file) {
        updateSound(file, multiplySound);
    }
    @BeanProperty
    public void setErrorSound(String file) {
        updateSound(file, errorSound);
    }
    @BeanProperty
    public void setClickSound(String file) {
        updateSound(file, clickSound);
    }
    @BeanProperty
    public void setMaxInputLength(int maxInputLength) {
        this.maxInputLength = maxInputLength;
    }
    @BeanProperty
    public void setPrecision(Integer precision) {
        this.precision = precision;
        if(precision != 0){
            decimalFormat = new DecimalFormat("0." + "0".repeat(precision));
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            decimalFormat.setDecimalFormatSymbols(symbols);
        }else{
            decimalFormat = new DecimalFormat("0");
        }
    }
    public void setIsSoundOn(boolean _isSoundOn) {
        isSoundOn =_isSoundOn;
        if(!isSoundOn)
        {
            additionSound.close();
            subtractionSound.close();
            divideSound.close();
            multiplySound.close();
            clickSound.close();
            errorSound.close();
            cancelSound.close();
        }else{
            try {
                AudioInputStream additionStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/boom.wav"));
                additionSound = AudioSystem.getClip();
                additionSound.open(additionStream);

                AudioInputStream subtractionStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/huh.wav"));
                subtractionSound = AudioSystem.getClip();
                subtractionSound.open(subtractionStream);

                AudioInputStream divideStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/sul.wav"));
                divideSound = AudioSystem.getClip();
                divideSound.open(divideStream);

                AudioInputStream multiplyStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/oof.wav"));
                multiplySound = AudioSystem.getClip();
                multiplySound.open(multiplyStream);

                AudioInputStream clickStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/click.wav"));
                clickSound = AudioSystem.getClip();
                clickSound.open(clickStream);

                AudioInputStream errorStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/error.wav"));
                errorSound = AudioSystem.getClip();
                errorSound.open(errorStream);
                
                AudioInputStream cancelStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/mineCave.wav"));
                cancelSound = AudioSystem.getClip();
                cancelSound.open(cancelStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @BeanProperty
    public void setButtonFontSize(int fontSize) {
        this.buttonFontSize = fontSize;
        updateButtonFont();
    }
    @BeanProperty
    public void setButtonFont(String _buttonFont) {
        buttonFont = _buttonFont;
        updateButtonFont();
    }
    @BeanProperty
    public void setDisplayFont(String displayFont) {
        this.displayFont = displayFont;
        updateDisplay();
    }
    @BeanProperty 
    public void setDisplayFontSize(Integer _displayFontSize) {
        displayFontSize = _displayFontSize;
        updateDisplay();
    }
    
    
        
    private Double add(Double valueA, Double valueB) {
        return valueA + valueB;
    }
        
    private Double subtract(Double valueA, Double valueB) {
        if(valueB > 0) valueB *= -1;
        return valueA + valueB;
    }
        
    private Double multiply(Double valueA, Double valueB) {
        return valueA * valueB;
    }
        
    private Double divide(Double valueA, Double valueB) {
        return valueA / valueB;
    }
    
      
    private void updateButtonFont() {
        for (Component component : buttonsPanel.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                button.setFont(new Font(buttonFont, Font.PLAIN, buttonFontSize));
            }
        }
        cancelButton.setFont(new Font(buttonFont, Font.PLAIN, buttonFontSize));
    }
    
        
    private void updateDisplay() {
        display.setFont(new Font(displayFont, 0, displayFontSize));
    }
     
    private void updateButtonColors() {
        // Aktualizacja kolorów przycisków
        for (Component component : buttonsPanel.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                button.setBackground(buttonBackgroundColor);
                button.setForeground(buttonForegroundColor);
            }
        }
        cancelButton.setBackground(buttonBackgroundColor);
        cancelButton.setForeground(buttonForegroundColor);
    }
    
    public void updateButtonListeners() {
        for (Component component : buttonsPanel.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
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
                        }
                        checkError = display.getText();
                        if(checkError.equals("Error")) {
                            display.setText("");
                        }
                        playSound(clickSound);
                        display.setBackground(defaultColor);
                        if (display.getText().length() < maxInputLength) {
                            display.setText(display.getText() + buttonText);
                        }

                    } else if ("+-*/".contains(buttonText)) {
                        isWelcome = false;
                        lastOperation = buttonText;
                        
                        if("+".equals(buttonText)) playSound(additionSound);
                        if("-".equals(buttonText)) playSound(subtractionSound);
                        if("*".equals(buttonText)) playSound(multiplySound);
                        if("/".equals(buttonText)) playSound(divideSound);
                     
                        //if(buttonText == "-") playSound(subtractionSound);
                        if(isEquals) isContinueOperation = true;
                        performOperation(); 
                    } else if ("=".equals(buttonText)) {
                        isEquals = true;
                        if(isContinueOperation) {
                            isContinueOperation = false;
                        }
                        performOperation();
                        //lastOperation = null;
                    }
                }
            });
            }
        }
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSound(cancelSound);
                currentValue = null;
                display.setText("");
                lastOperation = null;
                display.setBackground(defaultColor);
            }
        });
    }
       
    private void updateSound(String file, Clip sound) {
        try {
            AudioInputStream soundStream = AudioSystem.getAudioInputStream(getClass().getResource(file));
            sound = AudioSystem.getClip();
            sound.open(soundStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  
    private void playSound(Clip sound) {
        if (sound != null) {
            sound.setFramePosition(0);
            sound.start();
        }
    }
}

