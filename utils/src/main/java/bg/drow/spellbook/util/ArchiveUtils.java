package bg.drow.spellbook.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A simple util class for dealing with archives.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.3
 */
public class ArchiveUtils {
    private static final String DB_FILE_NAME = "spellbook.h2.db";

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveUtils.class);

    public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);

        in.close();
        out.close();
    }

    /**
     * Extract a zip archive.
     *
     * @param pathToArchive the full path to the file to be extracted
     * @return the full path to the extracted file
     */
    public static String extractDbFromArchive(String pathToArchive) {
        // Get the current path, where the database will be extracted
        String currentPath = System.getProperty("user.home") + File.separator + ".spellbook" + File.separator;
        LOGGER.info("Current path: " + currentPath);

        Enumeration entries;
        ZipFile zipFile;

        try {
            zipFile = new ZipFile(pathToArchive);

            entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                if (entry.isDirectory()) {
                    // Assume directories are stored parents first then children.
                    System.err.println("Extracting directory: " + entry.getName());
                    // This is not robust, just for demonstration purposes.
                    boolean created = (new File(currentPath + entry.getName())).mkdir();
                    
                    continue;
                }

                System.err.println("Extracting file: " + entry.getName());
                copyInputStream(zipFile.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(currentPath + entry.getName())));
            }

            zipFile.close();
        } catch (IOException ioe) {
            System.err.println("Unhandled exception:");
            ioe.printStackTrace();
        }

        currentPath += "db" + File.separator + DB_FILE_NAME;

        if (!currentPath.isEmpty()) {
            LOGGER.info("DB placed in : " + currentPath);
        }

        return currentPath;
    }
}
