package Controller;

import Vue.EmulatorView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class RegistersController implements ActionListener {
    private final EmulatorView viewctx;

    public RegistersController(EmulatorView viewctx) {
        this.viewctx = viewctx;
    }

    public String getRegValue(String reg) {
        return switch (reg) {
            case "A" -> String.format("%02X", viewctx.ctx.cpu.getRegA());
            case "F" -> viewctx.ctx.cpu.getFlagReg().toString();
            case "BC" -> String.format("%04X", (short) viewctx.ctx.cpu.getRegBC());
            case "DE" -> String.format("%04X", (short) viewctx.ctx.cpu.getRegDE());
            case "HL" -> String.format("%04X", (short) viewctx.ctx.cpu.getRegHL());
            case "SP" -> String.format("%04X", (short) viewctx.ctx.cpu.getRegSP());
            case "PC" -> String.format("%04X", (short) viewctx.ctx.cpu.getRegPC());
            case "IME" -> viewctx.ctx.cpu.isIME() ? "SET" : "UNSET";
            case "HALT" -> viewctx.ctx.cpu.isHalted() ? "SET" : "UNSET";
            case "INSTR" -> String.format("%02X", viewctx.ctx.bus_read(viewctx.ctx.cpu.getRegPC()));
            case "DIV" -> String.format("%08X", viewctx.ctx.ioRegisters.timer.getDiv());
            case "TIMA" -> String.format("%04X", (short) viewctx.ctx.ioRegisters.timer.getTima());
            case "TMA" -> String.format("%02X", viewctx.ctx.ioRegisters.timer.getTma());
            case "TAC" -> String.format("%02X", viewctx.ctx.ioRegisters.timer.getTac());
            case null, default -> "TODO";
        };
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        viewctx.update();
    }
}
