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
package com.jdirsync.ui.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.jdirsync.core.Action1;
import com.jdirsync.core.Action2;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UITaskUtil {
    private UITaskUtil() { }

    private static final Logger logger = LoggerFactory.getLogger(UITaskUtil.class);


    private static <T extends Future<?>> void waitForFutureCompletion(T future) throws InterruptedException {
        try {
            future.get();
        } catch (ExecutionException ex) {
            logger.trace("Task exited with exception", ex);
        }
    }


    public static <T1 extends Future<?>> Future<?> onCompleted(ExecutorService executorService, final T1 future1, final Action1<T1> callback) {
        return executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    waitForFutureCompletion(future1);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    return;
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        callback.invoke(future1);
                    }
                });
            }
        });
    }


    public static <T1 extends Future<?>, T2 extends Future<?>> Future<?> onCompleted(ExecutorService executorService, final T1 future1, final T2 future2, final Action2<T1, T2> callback) {
        return executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    waitForFutureCompletion(future1);
                    waitForFutureCompletion(future2);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    return;
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        callback.invoke(future1, future2);
                    }
                });
            }
        });
    }


    public static <T1 extends Future> Future<?> onProgress(ExecutorService executorService, final T1 future1, final long intervalInMillis,  final Action1<T1> callback) {
        return executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!future1.isDone()) {
                    // Update progress
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            callback.invoke(future1);
                        }
                    });

                    // Wait
                    try {
                        Thread.sleep(intervalInMillis);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        return;
                    }
                }
            }
        });
    }



    public static <T1 extends Future, T2 extends Future> Future<?> onProgress(ExecutorService executorService, final T1 future1, final T2 future2, final long intervalInMillis,  final Action2<T1, T2> callback) {
        return executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!future1.isDone() || !future2.isDone()) {
                    // Update progress
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            callback.invoke(future1, future2);
                        }
                    });

                    // Wait
                    try {
                        Thread.sleep(intervalInMillis);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        return;
                    }
                }
            }
        });
    }
}
