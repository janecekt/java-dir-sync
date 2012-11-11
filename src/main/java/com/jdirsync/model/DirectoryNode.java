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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DirectoryNode extends BaseNode {
    private static class NodeNameComparator implements Comparator<Node> {
        @Override
        public int compare(Node node1, Node node2) {
            return node1.getName().compareTo(node2.getName());
        }
    }
    private static final Comparator<Node> NODE_COMPARATOR = new NodeNameComparator();

    private String name;
    private List<Node> children = new ArrayList<>();

    public DirectoryNode(String name) {
        this.name = name;
    }


    @Override
    public Node copy() {
        DirectoryNode result = new DirectoryNode(name);
        for (Node child : children) {
            result.add( child.copy() );
        }
        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        long result = 0;
        for (Node child : children) {
            result = result + child.getSize();
        }
        return result;
    }



    @Override
    protected String getType() {
        return "d";
    }




    @Override
    protected Date getDate() {
        throw new UnsupportedOperationException("Directory has no DATE");
    }


    public List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }


    public void remove(Node node) {
        children.remove(node);
    }


    public void add(Node node) {
        int index = Collections.binarySearch(children, node, NODE_COMPARATOR);
        if (index < 0) {
            index = (-index) - 1;
        }
        children.add(index, node);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Directory[");
        sb.append("name=").append(name);
        sb.append("]");
        return sb.toString();
    }
}
