package net.ceriat.clgd.ccemux;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CCAssets {
    public Texture font;

    /**
     * Extracts assets from the ComputerCraft jar.
     * @param ccjar
     */
    public CCAssets(File ccjar) throws Exception {
        try (ZipFile zip = new ZipFile(ccjar)) {
            ZipEntry termFont = zip.getEntry("assets/computercraft/textures/gui/termFont.png");
            font = new Texture(zip.getInputStream(termFont));
        }
    }
}
