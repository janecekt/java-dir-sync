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
package com.jdirsync.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jdirsync.util.FileUtil;

public abstract class BaseNode implements Node {
    private static Pattern FORMAT_TOKEN_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    protected abstract String getType();

    protected abstract Date getDate();


    @Override
    public String toFormattedString(String format) {
        Matcher matcher = FORMAT_TOKEN_PATTERN.matcher(format);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String tokenName = matcher.group(1);
            switch (tokenName) {
                case "type":
                    matcher.appendReplacement(sb, getType());
                    break;
                case "name":
                    matcher.appendReplacement(sb, getName());
                    break;
                case "size":
                    // Construct user friendly size
                    matcher.appendReplacement(sb, FileUtil.formatSize(getSize()));
                    break;
                case "date":
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    matcher.appendReplacement(sb, dateFormat.format(getDate()));
                    break;
                default:
                    throw new RuntimeException("Unsupported format toke " + tokenName);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
