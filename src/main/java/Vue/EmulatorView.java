package Vue;

import Controller.DebugController;
import Controller.RegistersController;
import Model.*;

import javax.swing.*;
import java.awt.*;

public class EmulatorView extends JFrame {
    public final EmulationContext ctx;
    public final RegistersController registersController = new RegistersController(this);
    public final DebugController debugController = new DebugController(this);
    private final String[] registers = {"A", "F", "BC", "DE", "HL", "SP", "PC",
            "IME", "IE", "HALT", "INSTR", "DIV", "TIMA", "TMA", "TAC", "LY", "STAT"};
    public JTextArea debugScreen;
    JToolBar toolBar;
    JPanel gameCanvas;
    JPanel tileMap;
    JPanel registersPanel;

    // Registers
    //JTextField regAField;
    public EmulatorView(EmulationContext ctx) {
        this.ctx = ctx;
        setTitle("Gamegirl Emu - " + ctx.cartridge.getTitle());
        setPreferredSize(new Dimension(1360, 720));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        makeToolbar();
        makeRegisterPanel();
        debugScreen = new JTextArea();
        debugScreen.setMinimumSize(new Dimension(450, 60));
        debugScreen.setMaximumSize(new Dimension(450, 90));
        debugScreen.setEditable(false);
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(registersPanel, BorderLayout.WEST);
        getContentPane().add(debugScreen, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    public void makeToolbar() {
        JButton fileButton = new JButton("File");
        JButton editButton = new JButton("Edit");
        JButton helpButton = new JButton("Help");
        toolBar = new JToolBar(SwingConstants.HORIZONTAL);
        toolBar.add(fileButton);
        toolBar.add(editButton);
        toolBar.add(helpButton);
    }

    public void update() {
        debugController.updateDebug();
    }

    public void makeRegisterPanel() {
        registersPanel = new JPanel(new GridLayout(registers.length + 1, 0));
        registersPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        for (String reg : registers) {
            JPanel panel = new JPanel(new FlowLayout());
            var label = new JLabel(reg + ":");
            label.setHorizontalAlignment(SwingConstants.LEFT);
            panel.add(label);
            var textField = new JTextField();
            textField.setPreferredSize(new Dimension(90, 20));
            textField.setText(registersController.getRegValue(reg));
            textField.setEditable(false);
            panel.add(textField);
            registersPanel.add(panel);
        }
        var updateButton = new JButton("Update Values");
        updateButton.addActionListener(registersController);
        registersPanel.add(updateButton);
        this.getContentPane().add(registersPanel, BorderLayout.WEST);
        pack();
        setVisible(true);
    }
}
