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
package com.jdirsync.serialization;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;

import com.jdirsync.model.DirectoryNode;
import com.jdirsync.model.FileNode;
import com.jdirsync.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringIndexSerializer implements IndexSerializer {
    private static final Logger logger = LoggerFactory.getLogger(StringIndexSerializer.class);
    private static final Charset UTF8 = Charset.forName("UTF8");
    private static final String SEPARATOR = "\t";


    @Override
    public void indexToStream(DirectoryNode rootNode, OutputStream outputStream) {
        try (Writer writer = new OutputStreamWriter(outputStream, UTF8)) {
            for (Node child : rootNode.getChildren()) {
                nodeToStream(0, child, writer);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write index to stream !", ex);
        }
    }


    @Override
    public DirectoryNode indexFromStream(InputStream inputStream) {
        try (EntryIterator iterator = new EntryIterator(new BufferedReader(new InputStreamReader(inputStream, UTF8)))) {
            DirectoryNode rootNode = new DirectoryNode(null);
            while (iterator.getCurrentData() != null) {
                rootNode.add( nodeFromStream(iterator) );
            }
            return rootNode;
        }
    }


    private void nodeToStream(int level, Node node, Writer writer) throws IOException {
        if (node instanceof DirectoryNode) {
            DirectoryNode directoryNode = (DirectoryNode) node;
            writer.write(level + SEPARATOR + directoryNode.getName() + "\n" );

            level = level + 1;
            for (Node child :  directoryNode.getChildren()) {
                nodeToStream(level, child, writer);
            }
        } else if (node instanceof FileNode) {
            FileNode fileNode = (FileNode) node;
            writer.write(level
                    + SEPARATOR + fileNode.getName()
                    + SEPARATOR + fileNode.getSize()
                    + SEPARATOR + fileNode.getModificationTime().getTime()
                    + "\n" );
        }
    }


    private Node nodeFromStream(EntryIterator iterator) {
        if (iterator.getCurrentData() != null) {
            int level = iterator.getCurrentLevel();

            // Directory
            if (iterator.getCurrentData().length == 2) {
                DirectoryNode directoryNode = new DirectoryNode(iterator.getCurrentData()[1]);
                iterator.moveToNext();
                while (iterator.getCurrentLevel() == (level+1)) {
                    directoryNode.add(nodeFromStream(iterator));
                }
                return directoryNode;
            }

            // File
            if (iterator.getCurrentData().length == 4) {
                try {
                    String name = iterator.getCurrentData()[1];
                    long fileSize = Long.parseLong(iterator.getCurrentData()[2]);
                    long modificationTimestamp = Long.parseLong(iterator.getCurrentData()[3]);
                    iterator.moveToNext();
                    return new FileNode(name, fileSize, new Date(modificationTimestamp));
                } catch (NumberFormatException ex) {
                    throw new RuntimeException("Failed to read file entry on line " + iterator.getCurrentLineNumber(), ex);
                }
            }
        }

        throw new RuntimeException("Invalid entry on line " + iterator.getCurrentLineNumber());
    }


    private static class EntryIterator implements Closeable {
        private BufferedReader reader;
        private long currentLineNumber = 1;
        private String[] currentData = null;
        private Integer currentLevel = null;

        public EntryIterator(BufferedReader reader) {
            this.reader = reader;
            moveToNext();
        }

        public void moveToNext() {
            try {
                String line = reader.readLine();
                if (line == null) {
                    currentData = null;
                    currentLevel = null;
                } else {
                    currentData = line.split(SEPARATOR);
                    currentLevel = Integer.parseInt(currentData[0]);
                    currentLineNumber++;
                }
            } catch (NumberFormatException ex) {
                throw new RuntimeException("Line " + (currentLineNumber+1) + " contains invalid level " + currentData[0], ex);
            } catch (IOException ex) {
                throw new RuntimeException("Line " + (currentLineNumber+1) + " could not be read !", ex);
            }
        }


        public long getCurrentLineNumber() {
            return currentLineNumber;
        }


        public String[] getCurrentData() {
            return currentData;
        }


        public Integer getCurrentLevel() {
            return currentLevel;
        }


        @Override
        public void close() {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    logger.debug("Failed to close EntryIterator", ex);
                }
            }
        }
    }

}
