package Vue;

import Controller.RegisterController;
import Model.Emulator;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

@Setter
@Getter
public class RegistersView extends JPanel {
    private final String[] registers = {"AF", "BC", "DE", "HL", "SP", "PC",
            "IME", "IE", "HALT", "INSTR", "DIV", "TIMA", "TMA", "TAC", "LY", "LCDC"};
    private Emulator context = null;
    private RegisterController registersController = new RegisterController(this);
    private HashMap<String, JTextField> componentMap;

    RegistersView(Emulator context) {
        this.context = context;
        componentMap = new HashMap<>();
        setLayout(new GridLayout(registers.length + 1, 0));
        setBorder(BorderFactory.createLineBorder(Color.black));
        for (String reg : registers) {
            JPanel panel = new JPanel(new FlowLayout());
            var label = new JLabel(reg + ":");
            label.setHorizontalAlignment(SwingConstants.LEFT);
            panel.add(label);
            var textField = new JTextField();
            textField.setPreferredSize(new Dimension(90, 20));
            textField.setText(registersController.getRegValue(reg));
            textField.setName(reg);
            textField.setEditable(false);
            componentMap.put(reg, textField);
            panel.add(textField);
            add(panel);
        }
        var updateButton = new JButton("Update Values");
        updateButton.setFocusable(false);
        this.setFocusable(false);
        updateButton.addActionListener(registersController);
        add(updateButton);
        setVisible(true);
    }

    public void update() {
        componentMap.forEach((k, v) -> v.setText(registersController.getRegValue(k)));
    }
}
