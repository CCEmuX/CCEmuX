package net.ceriat.clgd.ccemux.emulation;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.core.computer.Computer;
import net.ceriat.clgd.ccemux.CCEmuX;
import org.luaj.vm2.LuaError;

import java.io.File;
import java.io.IOException;

public class CCEmuXAPI implements ILuaAPI {
    private Computer computer;

    public CCEmuXAPI(Computer computer) {
        this.computer = computer;
    }

    @Override
    public String[] getNames() {
        return new String[] { "ccemux" };
    }

    @Override
    public void startup() {

    }

    @Override
    public void advance(double v) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public String[] getMethodNames() {
        return new String[] {
            "getVersion",
            "editFile"
        };
    }

    @Override
    public Object[] callMethod(ILuaContext iLuaContext, int i, Object[] objects) throws LuaException, InterruptedException {
        switch (i) {
            case 0: // getVersion
                return new Object[] { CCEmuX.VERSION };

            case 1: // editFile
            {
                if (!(objects[0] instanceof String)) {
                    throw new LuaError("expected string for argument #1");
                }

                String file = (String)objects[0];

                try {
                    String path = "\"" + new File(CCEmuX.instance.assetsDir, "computer/" + computer.getID() + "/" + file).getAbsolutePath() + "\"";
                    Runtime.getRuntime().exec(
                        CCEmuX.instance.config.getEditorPath() + " " + path
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return new Object[]{};
            }
        }

        return new Object[0];
    }
}
