package net.ceriat.clgd.ccemux.emulation;

import static org.lwjgl.glfw.GLFW.*;

public enum CCCtrlCommand {
    TERMINATE(GLFW_KEY_T),
    REBOOT(GLFW_KEY_R),
    SHUTDOWN(GLFW_KEY_S),
    PASTE(GLFW_KEY_V);

    public int triggerKey;

    CCCtrlCommand(int triggerKey) {
        this.triggerKey = triggerKey;
    }
}
