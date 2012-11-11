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
package com.jdirsync.ui.viewmodel;

import com.jdirsync.logback.EventAppender;
import com.jdirsync.util.FileUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogObservingViewModel {
    private static final Logger logger = LoggerFactory.getLogger(LogObservingViewModel.class);
    private static final String TEMPLATE = FileUtil.readResourceToString("/com/jdirsync/ui/viewmodel/log-template.html");
    private final StringProperty logAsHtmlProperty = new SimpleStringProperty(TEMPLATE);

    // CAREFUL: EvenAppender uses WeakReference to listeners - we need to keep a reference to the listener class
    //    so that it is not garbage-collected.
    @SuppressWarnings("FieldCanBeLocal")
    private final EventAppender.LogEventListener listener = new EventAppender.LogEventListener() {
        @Override
        public void onLogEvent(Severity severity, String logMessage) {
            // Update HTML
            StringBuilder sb = new StringBuilder();
            sb.append("<DIV class=\"entry\"><PRE class=\"")
                    .append(severity.name())
                    .append("\">")
                    .append(logMessage.trim())
                    .append("</PRE></DIV><!--TOKEN:END-->");

            // Append to HTML (on the JAVA FX thread)
            final String newLogHtml = logAsHtmlProperty.getValue().replace("<!--TOKEN:END-->", sb.toString());
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    logAsHtmlProperty.setValue(newLogHtml);
                }
            });
        }
    };


    public LogObservingViewModel() {
        EventAppender<?> eventAdapter = EventAppender.getByName("UI-APPENDER");
        if (eventAdapter != null) {
            eventAdapter.addListener(listener);
        }
        logger.info("Handler initialized !");
    }


    @SuppressWarnings("UnusedDeclaration")
    public StringProperty logAsHtmlProperty() {
        return logAsHtmlProperty;
    }
}