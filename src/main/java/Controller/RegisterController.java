package Controller;


import Vue.RegistersView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterController implements ActionListener {
    private final RegistersView viewctx;

    public RegisterController(RegistersView viewctx) {
        this.viewctx = viewctx;
    }

    public String getRegValue(String reg) {
        if (viewctx.getContext() == null || !viewctx.getContext().isRunning())
            return "Not Initialized";
        return switch (reg) {
            case "AF" -> String.format("%04X", viewctx.getContext().getCpu().getAF());
            case "BC" -> String.format("%04X", (short) viewctx.getContext().getCpu().getBC());
            case "DE" -> String.format("%04X", (short) viewctx.getContext().getCpu().getDE());
            case "HL" -> String.format("%04X", (short) viewctx.getContext().getCpu().getHL());
            case "SP" -> String.format("%04X", (short) viewctx.getContext().getCpu().getSP());
            case "PC" -> String.format("%04X", (short) viewctx.getContext().getCpu().getPC());
            case "IME" -> viewctx.getContext().getCpu().isIME() ? "SET" : "UNSET";
            case "HALT" -> viewctx.getContext().getCpu().isHalted() ? "SET" : "UNSET";
            case "INSTR" -> String.format("%02X", viewctx.getContext().read(viewctx.getContext().getCpu().getPC()));
            case "DIV" -> String.format("%08X", viewctx.getContext().getTimer().getDivReg());
            case "TIMA" -> String.format("%04X", (short) viewctx.getContext().getTimer().getTimaReg());
            case "TMA" -> String.format("%02X", viewctx.getContext().getTimer().getTmaReg());
            case "TAC" -> String.format("%02X", viewctx.getContext().getTimer().getTacReg());
            case "LY" -> String.format("%02X", viewctx.getContext().getIoRegisters().getLcd().getLY());
            case "LCDC" ->
                    String.format("%s", Integer.toBinaryString(viewctx.getContext().getIoRegisters().getLcd().getLCDC() & 0xFF));
            case null, default -> "TODO";
        };
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        viewctx.update();
    }
}