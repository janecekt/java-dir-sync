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
package com.jdirsync.task;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.jdirsync.builder.IndexBuilder;
import com.jdirsync.core.WrappedFuture;
import com.jdirsync.model.DirectoryNode;

public class BuildIndexTask extends WrappedFuture<DirectoryNode> {
    private AtomicInteger fileCount;


    private BuildIndexTask(Future<DirectoryNode> future, AtomicInteger fileCount) {
        super(future);
        this.fileCount = fileCount;
    }


    public int getFileCount() {
        return fileCount.get();
    }


    public static BuildIndexTask start(ExecutorService executorService, final IndexBuilder indexBuilder, final Path path) {
        // Initialize counter
        final AtomicInteger fileCount = new AtomicInteger(0);

        // Execute task
        final Future<DirectoryNode> future = executorService.submit(new Callable<DirectoryNode>() {
            @Override
            public DirectoryNode call() throws Exception {
                return indexBuilder.buildIndex(path, fileCount);
            }
        });

        // Build result
        return new BuildIndexTask(future, fileCount);
    }


}
