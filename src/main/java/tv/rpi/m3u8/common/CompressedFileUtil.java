package tv.rpi.m3u8.common;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CompressedFileUtil {

    public static void extractArchive(File zipFile, File destinationDirectory, String archiveType) throws IOException, ArchiveException {

        final String[] supportedFormats = {"ar", "arj", "cpio", "dump", "jar", "tar", "zip", "7z"};
        // TODO assert archiveType is a supported format.

        InputStream inputStream = new FileInputStream(zipFile);
        ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();
        ArchiveInputStream archiveInputStream = archiveStreamFactory.createArchiveInputStream(archiveType, inputStream);
        ArchiveEntry archiveEntry;
        while((archiveEntry = archiveInputStream.getNextEntry()) != null) {
            Path path = Paths.get(destinationDirectory.getAbsolutePath(), archiveEntry.getName());
            File file = path.toFile();
            if(archiveEntry.isDirectory()) {
                if(!file.isDirectory()) {
                    file.mkdirs();
                }
            } else {
                File parent = file.getParentFile();
                if(!parent.isDirectory()) {
                    parent.mkdirs();
                }
                try (OutputStream outputStream = Files.newOutputStream(path)) {
                    IOUtils.copy(archiveInputStream, outputStream);
                }
            }
        }
    }

    public static void decompressXz(File xzFile, File destinationFile) throws IOException {
        InputStream fin = new FileInputStream(xzFile);
        BufferedInputStream in = new BufferedInputStream(fin);
        OutputStream out = new FileOutputStream(destinationFile);
        XZCompressorInputStream xzIn = new XZCompressorInputStream(in);
        final byte[] buffer = new byte[4096]; // Arbitrarily picked
        int n;
        while (-1 != (n = xzIn.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.close();
        xzIn.close();
    }

    public static void decompressGzip(File gzipFile, File destinationFile) throws IOException {
        InputStream fin = new FileInputStream(gzipFile);
        BufferedInputStream in = new BufferedInputStream(fin);
        OutputStream out = new FileOutputStream(destinationFile);
        GzipCompressorInputStream xzIn = new GzipCompressorInputStream(in);
        final byte[] buffer = new byte[4096]; // Arbitrarily picked
        int n;
        while (-1 != (n = xzIn.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.close();
        xzIn.close();
    }

    public static void extractTar(File tarFile, File destinationDirectory) throws IOException, ArchiveException {
        InputStream inputStream = new FileInputStream(tarFile);
        ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();
        ArchiveInputStream archiveInputStream = archiveStreamFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, inputStream);
        ArchiveEntry archiveEntry;
        while((archiveEntry = archiveInputStream.getNextEntry()) != null) {
            Path path = Paths.get(destinationDirectory.getAbsolutePath(), archiveEntry.getName());
            File file = path.toFile();
            if(archiveEntry.isDirectory()) {
                if(!file.isDirectory()) {
                    file.mkdirs();
                }
            } else {
                File parent = file.getParentFile();
                if(!parent.isDirectory()) {
                    parent.mkdirs();
                }
                try (OutputStream outputStream = Files.newOutputStream(path)) {
                    IOUtils.copy(archiveInputStream, outputStream);
                }
            }
        }
    }
}
