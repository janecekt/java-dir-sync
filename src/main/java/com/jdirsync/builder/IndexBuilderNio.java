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
package com.jdirsync.builder;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.jdirsync.model.DirectoryNode;
import com.jdirsync.model.FileNode;
import com.jdirsync.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexBuilderNio implements IndexBuilder {
    private static Logger logger = LoggerFactory.getLogger(IndexBuilderNio.class);

    @Override
    public DirectoryNode buildIndex(Path path, AtomicInteger fileCounter) {
        return (DirectoryNode) buildNode(path, true, fileCounter);
    }

    private Node buildNode(Path path, boolean isRoot, AtomicInteger fileCounter) {
        fileCounter.incrementAndGet();

        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> fileIterator = Files.newDirectoryStream(path)) {
                DirectoryNode directoryNode = isRoot
                        ? new DirectoryNode(null)
                        : new DirectoryNode(path.getFileName().toString());
                for (Path childPath : fileIterator) {
                    Node childNode = buildNode(childPath, false, fileCounter);
                    if (childNode != null) {
                        directoryNode.add(childNode);
                    }
                }
                return directoryNode;

            } catch (IOException ex) {
                logger.warn(path.toAbsolutePath().toString() + " cannot be traversed !");
                return null;
            }
        } else if (Files.isRegularFile(path)) {
            try {
                BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                return new FileNode(path.getFileName().toString(), attributes.size(), new Date(attributes.lastModifiedTime().toMillis()));
            } catch (IOException ex) {
                throw new RuntimeException("Failed to read attributes of " + path.toAbsolutePath().toString());
            }
        } else {
            logger.warn(path.toAbsolutePath().toString() + " is neither file nor directory !");
            return null;
        }
    }
}
