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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import com.jdirsync.core.WrappedFuture;
import com.jdirsync.model.DiffRecord;
import com.jdirsync.synchronizer.Synchronizer;

public class SynchronizeTask extends WrappedFuture<Void> {
    private AtomicLong bytesCopied;
    private AtomicLong totalCopyBytes;

    private SynchronizeTask(Future<Void> future, AtomicLong bytesCopied, AtomicLong totalCopyBytes) {
        super(future);
        this.bytesCopied = bytesCopied;
        this.totalCopyBytes = totalCopyBytes;
    }


    public long getBytesCopied() {
        return bytesCopied.get();
    }


    public long getTotalCopyBytes() {
        return totalCopyBytes.get();
    }


    public static SynchronizeTask start(ExecutorService executorService, final Synchronizer synchronizer,
                                       final Path leftBaseDir, final Path rightBaseDir, final List<DiffRecord> diffList) {
        // Initialize counters
        final AtomicLong bytesCopied = new AtomicLong(0);
        final AtomicLong totalCopyBytes = new AtomicLong(0);

        // Execute task
        final Future<Void> future = executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                synchronizer.synchronize(leftBaseDir, rightBaseDir, diffList, bytesCopied, totalCopyBytes);
                return null;
            }
        });

        // Build result
        return new SynchronizeTask(future, bytesCopied, totalCopyBytes);
    }
}
