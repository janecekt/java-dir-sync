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
package com.jdirsync.synchronizer;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.jdirsync.model.DiffRecord;
import com.jdirsync.model.DirectoryNode;
import com.jdirsync.model.Node;
import com.jdirsync.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchronizerImpl implements Synchronizer {
    private static Logger logger = LoggerFactory.getLogger(SynchronizerImpl.class);


    @Override
    public void synchronize(Path leftBaseDir, Path rightBaseDir, List<DiffRecord> diffList,
                            AtomicLong bytesCounter, AtomicLong totalBytesCounter) {

        // Calculate how many bytes need to be copied
        if (totalBytesCounter != null) {
            long totalCopySize = 0;
            for (DiffRecord diffRecord : diffList) {
                switch (diffRecord.getAction()) {
                    case USE_LEFT:
                        if (diffRecord.getLeftNode() != null) {
                            totalCopySize = totalCopySize + diffRecord.getLeftNode().getSize();
                        }
                        break;
                    case USE_RIGHT:
                        if (diffRecord.getRightNode() != null) {
                            totalCopySize = totalCopySize + diffRecord.getRightNode().getSize();
                        }
                        break;
                }
            }
            totalBytesCounter.set(totalCopySize);
        }

        // Synchronize trees
        for (DiffRecord diffRecord : diffList) {
            switch (diffRecord.getAction()) {
                case USE_LEFT:
                    doSynchronize(diffRecord, bytesCounter,
                            leftBaseDir, diffRecord.getLeftNode(),
                            rightBaseDir, diffRecord.getRightParent(), diffRecord.getRightNode());
                    break;
                case USE_RIGHT:
                    doSynchronize(diffRecord, bytesCounter,
                            rightBaseDir, diffRecord.getRightNode(),
                            leftBaseDir, diffRecord.getLeftParent(), diffRecord.getLeftNode());
                    break;
            }
        }
    }

    private void doSynchronize(DiffRecord record, AtomicLong bytesCounter,
                              Path fromBaseDir, Node fromChild,
                              Path toBaseDir, DirectoryNode toParent, Node toChild) {

        // Delete left dir
        if (toChild != null) {
            // Build full path
            Path deletePath = buildPath(toBaseDir, record.getPath(), toChild);

            // Delete on filesystem
            logger.info("Deleting " + deletePath.toAbsolutePath() );
            FileUtil.deleteRecursively(deletePath);

            // Delete in tree
            toParent.remove(toChild);
        }


        if (fromChild != null) {
            // Build full path
            Path sourcePath = buildPath(fromBaseDir, record.getPath(), fromChild);
            Path targetPath = (fromChild instanceof DirectoryNode)
                ? buildPath(toBaseDir, record.getPath(), fromChild)
                : buildPath(toBaseDir, record.getPath(), fromChild);

            // Copy on file system
            logger.info("Copying "  + sourcePath.toAbsolutePath() + " to " + targetPath.toString() );
            FileUtil.copyRecursively(sourcePath, targetPath, bytesCounter, StandardCopyOption.COPY_ATTRIBUTES);

            // Copy in tree
            toParent.add( fromChild.copy() );
        }
    }


    private Path buildPath(Path basePath, String[] path, Node node) {
        Path result = basePath;

        for (String part : path) {
            result = result.resolve(part);
        }

        if (node != null) {
            result = result.resolve(node.getName());
        }

        return result;
    }
}
