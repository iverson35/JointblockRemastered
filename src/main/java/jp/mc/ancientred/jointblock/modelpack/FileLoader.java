package jp.mc.ancientred.jointblock.modelpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Entry access abstraction over either a directory or a zip file.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.resouce.FileLoader} (note the original package typo),
 * ported verbatim: exactly one of {@link #rootFile} / {@link #zip} is set and every accessor
 * branches on which one it is. Two behaviours are deliberately preserved:
 * <ul>
 *   <li>{@link #entries()} in zip mode yields <em>every</em> zip entry including directory entries;
 *       in directory mode it yields files only. Callers must keep their {@code isDirectory()}
 *       guards.</li>
 *   <li>{@link #getEntry(String)} is an exact, case sensitive zip lookup, while the directory form
 *       depends on the host filesystem.</li>
 * </ul>
 */
public class FileLoader {
    public File rootFile;
    public ZipFile zip;

    public FileLoader(File argRoot) {
        try {
            this.rootFile = argRoot.getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileLoader(ZipFile argZip) {
        this.zip = argZip;
    }

    public Enumeration<FileEntry> entries() {
        List<FileEntry> list = new ArrayList<>();
        if (this.zip != null) {
            Enumeration<? extends ZipEntry> enumeration = this.zip.entries();
            while (enumeration.hasMoreElements()) {
                list.add(new FileEntryZip(enumeration.nextElement()));
            }
        } else if (this.rootFile != null) {
            for (File file : listFileTree(this.rootFile)) {
                list.add(new FileEntryFile(this.rootFile, file));
            }
        }

        return Collections.enumeration(list);
    }

    public static Set<File> listFileTree(File dir) {
        Set<File> fileTree = new HashSet<>();
        if (dir == null || dir.listFiles() == null) {
            return fileTree;
        }

        for (File entry : dir.listFiles()) {
            if (entry.isFile()) {
                fileTree.add(entry);
            } else {
                fileTree.addAll(listFileTree(entry));
            }
        }

        return fileTree;
    }

    /** Returns {@code null} when the entry does not belong to this loader or the file is missing. */
    public InputStream getInputStream(FileEntry argEntry) throws IOException {
        if (this.zip != null && argEntry instanceof FileEntryZip) {
            return this.zip.getInputStream(((FileEntryZip) argEntry).zipEntry);
        }

        if (this.rootFile != null && argEntry instanceof FileEntryFile) {
            File f = ((FileEntryFile) argEntry).fileEntry;
            if (f.exists() && !f.isDirectory()) {
                return new FileInputStream(f);
            }
        }

        return null;
    }

    public FileEntry getEntry(String argEntryPath) {
        if (this.zip != null) {
            ZipEntry zipEntry = this.zip.getEntry(argEntryPath);
            if (zipEntry != null) {
                return new FileEntryZip(zipEntry);
            }
        } else if (this.rootFile != null) {
            try {
                File fileEntry = new File(this.rootFile, argEntryPath).getCanonicalFile();
                if (fileEntry.getPath().startsWith(this.rootFile.getPath()) && fileEntry.exists() && !fileEntry.isDirectory()) {
                    return new FileEntryFile(this.rootFile, fileEntry);
                }
            } catch (IOException ignored) {
            }
        }

        return null;
    }

    public static class FileEntryZip implements FileEntry {
        public final ZipEntry zipEntry;

        public FileEntryZip(ZipEntry argEntry) {
            this.zipEntry = argEntry;
        }

        @Override
        public boolean isDirectory() {
            return this.zipEntry.isDirectory();
        }

        @Override
        public String getName() {
            return this.zipEntry.getName();
        }

        @Override
        public long getSize() {
            return this.zipEntry.getSize();
        }
    }

    public static class FileEntryFile implements FileEntry {
        public final File root;
        public final File fileEntry;
        public String fileEntryPath;

        public FileEntryFile(File argRoot, File argEntry) {
            this.root = argRoot;
            this.fileEntry = argEntry;
        }

        @Override
        public boolean isDirectory() {
            return this.fileEntry.isDirectory();
        }

        @Override
        public String getName() {
            if (this.fileEntryPath == null) {
                this.fileEntryPath = this.root.toURI().relativize(this.fileEntry.toURI()).getPath();
            }

            return this.fileEntryPath;
        }

        @Override
        public long getSize() {
            return this.fileEntry.length();
        }
    }

    public interface FileEntry {
        boolean isDirectory();

        String getName();

        long getSize();
    }
}
