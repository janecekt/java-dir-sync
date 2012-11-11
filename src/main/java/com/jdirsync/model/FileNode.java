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

import java.util.Date;

public class FileNode extends BaseNode {
    private String name;
    private long size;
    private Date modificationTime;


    public FileNode(String name, long size, Date modificationTime) {
        this.name = name;
        this.size = size;
        this.modificationTime = modificationTime;
    }


    @Override
    public Node copy() {
        return new FileNode(name, size, modificationTime);
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    protected String getType() {
        return "f";
    }


    @Override
    public long getSize() {
        return size;
    }


    @Override
    protected Date getDate() {
        return getModificationTime();
    }


    public Date getModificationTime() {
        return modificationTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("File[");
        sb.append("name=").append(name);
        sb.append(", ");
        sb.append("size=").append(size);
        sb.append(", ");
        sb.append("mtime=").append(modificationTime);
        sb.append("]");
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileNode fileNode = (FileNode) o;

        if (size != fileNode.size) return false;
        if (modificationTime != null ? !modificationTime.equals(fileNode.modificationTime) : fileNode.modificationTime != null)
            return false;
        if (name != null ? !name.equals(fileNode.name) : fileNode.name != null) return false;

        return true;
    }


    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (modificationTime != null ? modificationTime.hashCode() : 0);
        return result;
    }
}
