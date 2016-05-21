package net.ceriat.clgd.ccemux;

import net.ceriat.clgd.ccemux.graphics.Texture;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CCAssets implements Closeable {
    public Texture font;

    /**
     * Extracts assets from the ComputerCraft jar.
     * @param ccjar
     */
    public CCAssets(File ccjar) throws Exception {
        CCEmuX.instance.logger.fine("Extracting assets from " + ccjar.getAbsolutePath());

        try (ZipFile zip = new ZipFile(ccjar)) {
            ZipEntry termFont = zip.getEntry("assets/computercraft/textures/gui/termFont.png");

            try (InputStream is = zip.getInputStream(termFont)) {
                font = new Texture(is);
                CCEmuX.instance.logger.fine("Loaded font texture");
            }
        }
    }

    public void close() {
        if (font != null) {
            font.close();
        }
    }
}
