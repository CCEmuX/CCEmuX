package net.ceriat.clgd.ccemux.emulation;

import dan200.computercraft.api.filesystem.IMount;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HybridMount implements IMount {
    private IMount dominant, secondary;

    public HybridMount(IMount dominant, IMount secondary) {
        this.dominant = dominant;
        this.secondary = secondary;
    }

    @Override
    public boolean exists(String s) throws IOException {
        return dominant.exists(s) || secondary.exists(s);
    }

    @Override
    public boolean isDirectory(String s) throws IOException {
        if (dominant.exists(s)) {
            return dominant.isDirectory(s);
        }

        if (secondary.exists(s)) {
            return secondary.isDirectory(s);
        }

        throw new IOException("No such directory");
    }

    @Override
    public void list(String s, List<String> list) throws IOException {
        if (!dominant.exists(s) && !secondary.exists(s)) {
            throw new IOException("No such directory");
        }

        List<String> finalList = new ArrayList<String>();

        if (dominant.exists(s)) {
            dominant.list(s, finalList);
        }

        if (secondary.exists(s)) {
            secondary.list(s, finalList);
        }

        for (String file : finalList) {
            if (!list.contains(file)) { // avoid duplicates
                list.add(file);
            }
        }
    }

    @Override
    public long getSize(String s) throws IOException {
        if (dominant.exists(s)) {
            return dominant.getSize(s);
        }

        if (secondary.exists(s)) {
            return dominant.getSize(s);
        }

        throw new IOException("No such file");
    }

    @Override
    public InputStream openForRead(String s) throws IOException {
        if (dominant.exists(s) && !dominant.isDirectory(s)) {
            return dominant.openForRead(s);
        }

        if (secondary.exists(s) && !secondary.isDirectory(s)) {
            return secondary.openForRead(s);
        }

        throw new IOException("Can't open file");
    }
}
