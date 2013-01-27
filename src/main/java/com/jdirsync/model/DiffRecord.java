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

import java.util.Comparator;

public class DiffRecord {
    public static enum Action { NONE, USE_LEFT, USE_RIGHT }
    public static enum DiffType { MISSING_LEFT, MISSING_RIGHT,
        LEFT_DIR_RIGHT_FILE, LEFT_FILE_RIGHT_DIR,
        LEFT_NEWER, RIGHT_NEWER,
        SIZE }

    private String[] path;
    private DirectoryNode leftParent;
    private Node leftNode;
    private DirectoryNode rightParent;
    private Node rightNode;
    private Action action;
    private DiffType diffType;
    private boolean isMoved;

    public static class PathAndNameComparator implements Comparator<DiffRecord> {
        private static Comparator<DiffRecord> instance;

        public static Comparator<DiffRecord> getInstance() {
            if (instance == null) {
                instance = new PathAndNameComparator();
            }
            return instance;
        }

        private PathAndNameComparator() {}

        @Override
        public int compare(DiffRecord o1, DiffRecord o2) {
            if (o1 != null && o2 != null) {
                int cmp = comparePath(o1.getPath(), o2.getPath());
                if (cmp != 0) {
                    return cmp;
                }
                return o1.getName().compareTo(o2.getName());
            }

            // One of the records is null
            if (o1 == null) {
                return o2 == null ? 0 : -1;
            }

            // Both are NULL
            return 0;
        }

        private int comparePath(String[] path1, String[] path2) {
            int i;
            for (i = 0; i<Math.max(path1.length, path2.length); i++) {
                String part1 = (i < path1.length) ? path1[i] : "";
                String part2 = (i < path2.length) ? path2[i] : "";
                int cmp = part1.compareTo(part2);
                if (cmp != 0) {
                    return cmp;
                }
            }
            return 0;
        }
    }


    public DiffRecord(String[] path, DirectoryNode leftParent, Node leftNode, DirectoryNode rightParent, Node rightNode) {
        if (leftNode == null && rightNode == null) {
            throw new IllegalArgumentException("Both left and right nodes are NULL !");
        }
        this.path = path;
        this.leftParent = leftParent;
        this.leftNode = leftNode;
        this.rightParent = rightParent;
        this.rightNode = rightNode;
        this.action = Action.NONE;
        this.isMoved = false;

        // Analyze diff
        if (leftNode == null || rightNode == null) {
            diffType = (leftNode == null) ? DiffType.MISSING_LEFT : DiffType.MISSING_RIGHT;
        } else if (!leftNode.getClass().equals(rightNode.getClass())) {
            diffType = (leftNode instanceof FileNode) ? DiffType.LEFT_FILE_RIGHT_DIR : DiffType.LEFT_DIR_RIGHT_FILE;
        } else if (leftNode instanceof FileNode && rightNode instanceof FileNode) {
            FileNode leftFile = (FileNode) leftNode;
            FileNode rightFile = (FileNode) rightNode;

            int compare = leftFile.getModificationTime().compareTo(rightFile.getModificationTime());
            if (compare < 0) {
                diffType = DiffType.RIGHT_NEWER;
            } else if (compare > 0) {
                diffType = DiffType.LEFT_NEWER;
            } else if (leftFile.getSize() != rightFile.getSize()) {
                diffType = DiffType.SIZE;
            } else {
                throw new IllegalArgumentException("File nodes have same size and date !");
            }
        } else {
            throw new IllegalArgumentException("Both nodes are directories !");
        }
    }


    public String[] getPath() {
        return path;
    }

    public String getPathString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<path.length; i++) {
            if (i>0) {
                sb.append("/");
            }
            sb.append(path[i]);
        }
        return sb.toString();
    }


    public String getName() {
        return (leftNode != null) ? leftNode.getName() : rightNode.getName();
    }



    public DirectoryNode getLeftParent() {
        return leftParent;
    }


    public Node getLeftNode() {
        return leftNode;
    }


    public DirectoryNode getRightParent() {
        return rightParent;
    }


    public Node getRightNode() {
        return rightNode;
    }

    public Action getAction() {
        return action;
    }


    public void setAction(Action action) {
        this.action = action;
    }


    public DiffType getDiffType() {
        return diffType;
    }


    public boolean isMoved() {
        return isMoved;
    }


    public void setMoved(boolean moved) {
        isMoved = moved;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DiffRecord[");
        sb.append("path=").append(getPathString());
        sb.append(", ");
        sb.append("left=").append(leftNode != null ? leftNode : "NONE");
        sb.append(", ");
        sb.append("right=").append(rightNode != null ? rightNode : "NONE");
        sb.append(", ");
        sb.append("diffType=").append(diffType);
        sb.append(", ");
        sb.append("action=").append(action);
        if (isMoved) {
            sb.append(", MOVED");
        }
        sb.append("]");
        return sb.toString();
    }
}

