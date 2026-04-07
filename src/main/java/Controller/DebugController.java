package Controller;

import Vue.EmulatorView;

public class DebugController {

    private final EmulatorView viewctx;

    public DebugController(EmulatorView viewctx) {
        this.viewctx = viewctx;
    }

    public void updateDebug() {

        viewctx.ctx.dbg_update();
        if (!viewctx.ctx.dbg_msg.isEmpty())
            viewctx.debugScreen.setText(String.valueOf(viewctx.ctx.dbg_msg));
    }
}
