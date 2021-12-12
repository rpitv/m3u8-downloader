package tv.rpi.m3u8.model.common;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import tv.rpi.m3u8.Main;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class M3U8FileUtil {

    public static File downloadFile(String src, String dest, ProgressFeedback feedback) throws IOException {
        final URL srcUrl = new URL(src);
        final File destFile = new File(dest);
        FileUtils.createParentDirectories(destFile);

        final HttpURLConnection connection = (HttpURLConnection) srcUrl.openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        final int totalLength = connection.getContentLength();
        final InputStream httpStream = connection.getInputStream();
        final FileOutputStream fileStream = new FileOutputStream(dest);
        final byte[] buffer = new byte[1024];
        int bytesRead;
        int totalBytesRead = 0;
        while((bytesRead = httpStream.read(buffer)) > 0) {
            totalBytesRead += bytesRead;
            fileStream.write(buffer, 0, bytesRead);
            if (feedback != null) {
                feedback.callback((float) totalBytesRead / (float) totalLength, null);
            }
        }
        httpStream.close();
        fileStream.close();
        return new File(dest);
    }

    public static void extractArchive(File archiveFile, File destinationDirectory, String archiveType, ProgressFeedback feedback) throws IOException, ArchiveException {

        final String[] supportedFormats = {"ar", "arj", "cpio", "dump", "jar", "tar", "zip", "7z"};
        // TODO assert archiveType is a supported format.

        final InputStream inputStream = new FileInputStream(archiveFile);
        final ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();
        final ArchiveInputStream archiveInputStream = archiveStreamFactory.createArchiveInputStream(archiveType, inputStream);
        ArchiveEntry archiveEntry;
        final long estimatedFinalSize = archiveFile.length() * 3;
        int totalBytesRead = 0;
        while((archiveEntry = archiveInputStream.getNextEntry()) != null) {
            final Path path = Paths.get(destinationDirectory.getAbsolutePath(), archiveEntry.getName());
            final File file = path.toFile();
            if(archiveEntry.isDirectory()) {
                if(!file.isDirectory()) {
                    if(!file.mkdirs()) {
                        throw new IOException("Failed to create the necessary parent directories.");
                    }
                }
            } else {
                final File parent = file.getParentFile();
                if(!parent.isDirectory()) {
                    if(!parent.mkdirs()) {
                        throw new IOException("Failed to create the necessary parent directories.");
                    }
                }
                try (OutputStream outputStream = Files.newOutputStream(path)) {
                    totalBytesRead += IOUtils.copy(archiveInputStream, outputStream);
                }

                if(feedback != null) {
                    feedback.callback(Math.min((double) totalBytesRead / estimatedFinalSize, 1.0), null);
                }
            }
        }
        archiveInputStream.close();
        inputStream.close();
    }

    public static void decompressXz(File xzFile, File destinationFile, ProgressFeedback feedback) throws IOException {
        final long inputSize = xzFile.length();
        final long estOutputSize = inputSize * 3;

        final InputStream fin = new FileInputStream(xzFile);
        final BufferedInputStream in = new BufferedInputStream(fin);
        final OutputStream out = new FileOutputStream(destinationFile);
        final XZCompressorInputStream xzIn = new XZCompressorInputStream(in);
        final byte[] buffer = new byte[1024]; // Arbitrarily picked
        int n;
        float nTotal = 0;
        while (-1 != (n = xzIn.read(buffer))) {
            out.write(buffer, 0, n);
            nTotal += n;
            feedback.callback(Math.min(nTotal / estOutputSize, 1.0), null);
        }
        in.close();
        fin.close();
        out.close();
        xzIn.close();
    }
}
