package net.ceriat.clgd.ccemux.emulation;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.core.filesystem.JarMount;
import net.ceriat.clgd.ccemux.CCEmuX;

import java.io.File;
import java.io.IOException;

public class EmuEnvironment implements IComputerEnvironment {
    private boolean isColour;
    private int nextID = 0;

    public EmuEnvironment(boolean colour) {
        this.isColour = colour;
    }

    @Override
    public int getDay() {
        return (int)((CCEmuX.instance.ticksSinceStart + 6000L) / 24000L) + 1;
    }

    @Override
    public double getTimeOfDay() {
        return (CCEmuX.instance.ticksSinceStart + 6000) % 24000 / 1000.0;
    }

    @Override
    public boolean isColour() {
        return isColour;
    }

    @Override
    public long getComputerSpaceLimit() {
        return Long.MAX_VALUE;
    }

    @Override
    public String getHostString() {
        return "ComputerCraft " + ComputerCraft.getVersion() + " (CCEmuX v" + CCEmuX.VERSION + ")";
    }

    @Override
    public int assignNewID() {
        return nextID++;
    }

    @Override
    public IWritableMount createSaveDirMount(String s, long l) {
        return new FileMount(new File(CCEmuX.instance.assetsDir, s), l);
    }

    @Override
    public IMount createResourceMount(String domain, String subPath) {
        try {
            JarMount jarMount = new JarMount(CCEmuX.instance.ccJarFile, "assets/" + domain + "/" + subPath);
            return jarMount;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
