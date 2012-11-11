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
package com.jdirsync;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.jdirsync.builder.DiffBuilder;
import com.jdirsync.builder.IndexBuilder;
import com.jdirsync.builder.IndexBuilderNio;
import com.jdirsync.model.DiffRecord;
import com.jdirsync.model.DirectoryNode;
import com.jdirsync.util.FileUtil;
import org.junit.Test;

public class FastSyncTest {
    @Test
    public void test() throws IOException {
        Path leftRoot = Paths.get("target", "leftRoot");
        Path rightRoot = Paths.get("target", "rightRoot");

        FileUtil.deleteRecursively(leftRoot);
        FileUtil.deleteRecursively(rightRoot);

        // Prepare left filesystem
        FileUtil.createDirectories(leftRoot.resolve("dir1"));
        FileUtil.createDirectories(leftRoot.resolve("dir2"));
        FileUtil.createDirectories(leftRoot.resolve("dir3"));
        FileUtil.writeStringToFile(leftRoot.resolve(Paths.get("dir1", "file1.txt")), "data1");
        FileUtil.writeStringToFile(leftRoot.resolve(Paths.get("dir1", "file2.txt")), "data2");

        // Copy left -> right
        FileUtil.copyRecursively(leftRoot, rightRoot, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

        // Introduce changes to right tree
        FileUtil.writeStringToFile(rightRoot.resolve(Paths.get("dir1", "file2.txt")), "data2-other");
        FileUtil.deleteRecursively(rightRoot.resolve("dir2"));
        FileUtil.deleteRecursively(rightRoot.resolve("dir3"));
        FileUtil.writeStringToFile(rightRoot.resolve("dir3"), "data3");

        // Build indexes for left and right
        IndexBuilder indexBuilder = new IndexBuilderNio();
        DirectoryNode leftIndex = indexBuilder.buildIndex(leftRoot, new AtomicInteger(0));
        DirectoryNode rightIndex = indexBuilder.buildIndex(rightRoot, new AtomicInteger(0));

        // Build diff
        DiffBuilder diffBuilder = new DiffBuilder();
        List<DiffRecord> diffList = diffBuilder.buildDiff(leftIndex, rightIndex);
        for (DiffRecord diffRecord : diffList) {
            System.out.println(diffRecord.toString());
        }
    }

}
