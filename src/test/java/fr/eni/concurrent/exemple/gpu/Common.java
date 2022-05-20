package fr.eni.concurrent.exemple.gpu;

import java.io.File;

/**
 *
 * @author ljoyeux
 */
public class Common {
    public static File getOutputFolder() {
        
        final String outputFolder = System.getProperty("output.folder");
        assert outputFolder!=null;
        assert !outputFolder.trim().isEmpty();
        
        final File folder = new File(outputFolder);
        if(!folder.exists()) {
            boolean mkdirs = folder.mkdirs();
            assert mkdirs;
        }
        
        return folder;
    }
    
}
