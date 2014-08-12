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
package com.jdirsync.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.jdirsync.model.DiffRecord;
import com.jdirsync.model.DirectoryNode;
import com.jdirsync.model.FileNode;
import com.jdirsync.model.Node;
import com.jdirsync.util.ArrayUtil;

public class DiffBuilder {
    private static <T> T nextOrNull(Iterator<T> iterator) {
        return iterator.hasNext() ? iterator.next() : null;
    }

    public List<DiffRecord> buildDiff(DirectoryNode leftRoot, DirectoryNode rightRoot) {
        List<DiffRecord> diffList = new ArrayList<>();
        buildDiff(new String[0], leftRoot, rightRoot, diffList);
        Collections.sort(diffList, DiffRecord.PathAndNameComparator.getInstance());
        return groupRelatedRecords(diffList);
    }

    private List<DiffRecord> extractItemsWithSameName(DiffRecord refRecord, DiffRecord.DiffType requiredDiffType, List<DiffRecord> source) {
        List<DiffRecord> group = new ArrayList<>();

        for (Iterator<DiffRecord> it = source.iterator(); it.hasNext(); ) {
            DiffRecord record = it.next();
            if (refRecord.getName().equals(record.getName()) && (record.getDiffType() == requiredDiffType)) {
                refRecord.setMoved(true);
                record.setMoved(true);
                group.add(record);
                it.remove();
            }
        }

        return group;
    }

    private List<DiffRecord> groupRelatedRecords(List<DiffRecord> source) {
        List<DiffRecord> result = new ArrayList<>();
        while (source.size() > 0) {
            // Remove first item from source
            DiffRecord refRecord = source.remove(0);
            if (refRecord.getDiffType() != DiffRecord.DiffType.MISSING_RIGHT) {
                // Copy it directly into the result
                result.add(refRecord);
            } else {
                // Find all records with the same name but MISSING_LEFT
                List<DiffRecord> missingLeftWithSameName = extractItemsWithSameName(refRecord, DiffRecord.DiffType.MISSING_LEFT, source);
                if (missingLeftWithSameName.size() > 0) {
                    // Find all records with same name but MISSING_RIGHT
                    List<DiffRecord> missingRightWithSameName = extractItemsWithSameName(refRecord, DiffRecord.DiffType.MISSING_RIGHT, source);
                    // Add refRecord - this one is also MISSING_RIGHT
                    missingRightWithSameName.add(0, refRecord);
                    // Add all MISSING_RIGHT with same name
                    result.addAll(missingRightWithSameName);
                    // Add all MISSING_LEFT with same name
                    result.addAll(missingLeftWithSameName);
                }
            }
        }
        return result;
    }


    private void buildDiff(String[] path, DirectoryNode left, DirectoryNode right, List<DiffRecord> diffList) {
        Iterator<Node> leftIterator = left.getChildren().iterator();
        Iterator<Node> rightIterator = right.getChildren().iterator();

        Node leftChild = nextOrNull(leftIterator);
        Node rightChild = nextOrNull(rightIterator);

        while (leftChild != null || rightChild  != null) {
            if (leftChild != null && rightChild != null) {
                int compareResult = leftChild.getName().compareTo(rightChild.getName());
                if (compareResult < 0) {
                    diffList.add(new DiffRecord(path, left, leftChild, right, null));
                    leftChild = nextOrNull(leftIterator);
                    continue;
                }
                if (compareResult > 0) {
                    diffList.add(new DiffRecord(path, left, null, right, rightChild));
                    rightChild = nextOrNull(rightIterator);
                    continue;
                }

                // leftChild.getName = rightChild.getName
                if (leftChild instanceof FileNode) {
                    FileNode leftFileNode = (FileNode) leftChild;
                    if (!leftFileNode.equals(rightChild)) {
                        diffList.add( new DiffRecord(path, left, leftChild, right, rightChild) );
                    }
                } else if (rightChild instanceof FileNode) {
                    FileNode rightFileNode = (FileNode) rightChild;
                    if (!rightFileNode.equals(leftChild)) {
                        diffList.add( new DiffRecord(path, left, leftChild, right, rightChild) );
                    }
                } else {
                    buildDiff(ArrayUtil.arrayExtend(path, leftChild.getName()), (DirectoryNode) leftChild, (DirectoryNode) rightChild, diffList);
                }
                leftChild = nextOrNull(leftIterator);
                rightChild = nextOrNull(rightIterator);
            } else if (leftChild != null) {
                // leftChild == null && rightChild == null
                diffList.add(new DiffRecord(path, left, leftChild, right, null));
                leftChild = nextOrNull(leftIterator);
            } else {
                // leftChild == null && rightChild != null
                diffList.add(new DiffRecord(path, left, null, right, rightChild));
                rightChild = nextOrNull(rightIterator);
            }
        }
    }
}
