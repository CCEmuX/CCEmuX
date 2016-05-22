package net.ceriat.clgd.ccemux;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class CCEmuXConfig {
    private Properties props = new Properties();

    public CCEmuXConfig(File configFile) {
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();

                props.setProperty("editor-executable", "");
                try (FileOutputStream os = new FileOutputStream(configFile)) {
                    props.store(os, "");
                }
            }

            try (FileInputStream in = new FileInputStream(configFile)) {
                props.load(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getEditorPath() {
        String path = props.getProperty("editor-executable").replace('\\', '/');

        if (path.isEmpty()) {
            return null;
        }

        File f = new File(path);
        if (!f.exists()) {
            CCEmuX.instance.logger.warning("editor-executable is invalid!");
            return null;
        }

        return path;
    }
}
