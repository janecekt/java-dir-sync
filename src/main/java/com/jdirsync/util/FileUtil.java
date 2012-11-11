/*
 *  Copyright (c) 2008 - Tomas Janecek.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.jdirsync.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;

public final class FileUtil {
    private FileUtil() {
    }

    private static final Charset UTF8_CHARSET = Charset.forName("UTF8");
    private static final String[] SIZE_NAMES = {"B", "kB", "MB", "GB", "TB"};


    public static String readResourceToString(String resourcePath) {
        try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(FileUtil.class.getResourceAsStream(resourcePath), UTF8_CHARSET))) {

            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (IOException ex) {
            throw new RuntimeException("Reading of resource " + resourcePath + " failed", ex);
        }
    }



    public static void writeStringToFile(Path path, String data) {
        try (BufferedWriter writer = Files.newBufferedWriter(path,
                                            UTF8_CHARSET,
                                            StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            writer.write(data);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write to file " + path.toAbsolutePath().toString(), ex);
        }
    }


    public static void deleteRecursively(Path path) {
        try {
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                return;
            }

            if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                Files.delete(path);
            } else {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc != null) {
                            throw exc;
                        }
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete " + path.toAbsolutePath().toString(), ex);
        }

    }


    public static void copyRecursively(Path source, Path target, CopyOption... copyOptions) {
        copyRecursively(source, target, null, copyOptions);
    }


    public static void copyRecursively(Path source, Path target, AtomicLong bytesCounter, CopyOption... copyOptions) {
        try {
            if (!Files.exists(source, LinkOption.NOFOLLOW_LINKS)) {
                throw new RuntimeException("Source path does not exist " + source.toAbsolutePath().toString());
            }

            if (!Files.isDirectory(source)) {
                copyRecursivelyHelper(source, target, bytesCounter, copyOptions);
            } else {
                if (Files.exists(target) && !Files.isDirectory(target)) {
                    throw new RuntimeException("Source is a directory " + source.toAbsolutePath().toString()
                        + " target exists but is not a directory " + target.toAbsolutePath().toString());
                }

                // Create target parent directory if it does not exist
                Files.createDirectories(target.getParent());

                copyRecursivelyHelper(source, target, bytesCounter, copyOptions);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to copy " + source.toAbsolutePath().toString()
                    + " to " + target.toAbsolutePath().toString(), ex);
        }
    }


    /**
     * Assumes that
     * 1) source exists
     * 2) target parent exists
     */
    private static void copyRecursivelyHelper(Path source, Path target, AtomicLong bytesCounter, CopyOption... copyOptions) {
        try {
            if (!Files.isDirectory(source)) {
                Files.copy(source, target, copyOptions);
                if (bytesCounter != null) {
                    BasicFileAttributes attributes = Files.readAttributes(source, BasicFileAttributes.class);
                    bytesCounter.addAndGet(attributes.size());
                }

            } else {
                if (Files.exists(target) && !Files.isDirectory(target)) {
                    throw new RuntimeException("Source is a directory " + source.toAbsolutePath().toString()
                            + " target exists but is not a directory " + target.toAbsolutePath().toString());
                }

                // Copy directory itself
                Files.copy(source, target, copyOptions);

                // Copy directory content recursively
                for(Path sourceChild : Files.newDirectoryStream(source)) {
                    Path targetChild = target.resolve(sourceChild.getFileName());
                    copyRecursivelyHelper(sourceChild, targetChild, bytesCounter, copyOptions);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to copy " + source.toAbsolutePath().toString()
                    + " to " + target.toAbsolutePath().toString(), ex);
        }
    }


    public static String formatSize(long sizeInBytes) {
        double displaySize = sizeInBytes;
        int unitIndex;
        for (unitIndex=0; unitIndex<SIZE_NAMES.length; unitIndex++) {
            if (displaySize < 1024) {
                break;
            }
            displaySize = displaySize / 1024.0;
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(displaySize) + SIZE_NAMES[unitIndex];
    }





    public static void createDirectories(Path path, FileAttribute<?>... attrs) {
        try {
            Files.createDirectories(path, attrs);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create directory " + path.toAbsolutePath().toString(), ex);
        }
    }
}
