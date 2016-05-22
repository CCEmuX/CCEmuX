package net.ceriat.clgd.ccemux.emulation;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.filesystem.FileSystemException;

import java.lang.reflect.Field;

public class ROMPatcher {
    private static boolean globalROMPatched = false;

    public static boolean patchROM(Computer computer, IMount rom) {
        try {
            // we've patched s_romMount. our rom will automatically be used
            if (globalROMPatched) {
                return true;
            }

            Field fileSys = Computer.class.getDeclaredField("m_fileSystem");
            fileSys.setAccessible(true);

            // if the rom mount is null, do not patch.
            // our patch might get reset
            Field romMount = Computer.class.getDeclaredField("s_romMount");
            romMount.setAccessible(true);

            if (romMount.get(null) == null || fileSys.get(computer) == null) {
                return false;
            }

            FileSystem fs = (FileSystem)fileSys.get(computer);
            fs.unmount("rom");
            fs.mount("rom", "rom", rom);

            romMount.set(null, rom);
            globalROMPatched = true;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        return true;
    }
}
