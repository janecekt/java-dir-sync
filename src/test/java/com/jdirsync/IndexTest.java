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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.jdirsync.builder.DiffBuilder;
import com.jdirsync.builder.IndexBuilder;
import com.jdirsync.builder.IndexBuilderNio;
import com.jdirsync.model.DiffRecord;
import com.jdirsync.model.DirectoryNode;
import com.jdirsync.serialization.IndexSerializer;
import com.jdirsync.serialization.StringIndexSerializer;
import org.junit.Ignore;
import org.junit.Test;

public class IndexTest {
    private long profileBuildIndex(Path path, IndexBuilder builder, int count) {
        long start = System.currentTimeMillis();
        for (int i=0; i<count; i++) {
            builder.buildIndex(path, new AtomicInteger(0));
        }
        return (System.currentTimeMillis() - start) / count;
    }


    @Ignore
    @Test
    public void testIndexBuild() throws FileNotFoundException {
        Path testPath = Paths.get("C:/Windows");
        long nioTime = profileBuildIndex(testPath, new IndexBuilderNio(), 3);

        System.out.println("NIO=" + nioTime/1000.0 + "s");
    }

    @Ignore
    @Test
    public void testIndexSerialization() throws FileNotFoundException {
        IndexSerializer serializer = new StringIndexSerializer();

        // Read from file
        DirectoryNode node = serializer.indexFromStream(new FileInputStream("target/testindex.dat"));

        // Write to file
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.indexToStream(node, out);
        System.out.println( new String(out.toByteArray(), Charset.forName("UTF8")) );
    }



    @Ignore
    @Test
    public void testDiff() throws FileNotFoundException {
        IndexBuilder builder = new IndexBuilderNio();
        DirectoryNode index1 = builder.buildIndex(Paths.get("C:/Programs"), new AtomicInteger(0));
        DirectoryNode index2 = builder.buildIndex(Paths.get("E:/Programs"), new AtomicInteger(0));

        DiffBuilder diffBuilder = new DiffBuilder();
        List<DiffRecord> diffRecordList = diffBuilder.buildDiff(index1, index2);
        for (DiffRecord diffRecord : diffRecordList) {
            System.out.println(diffRecord.toString());
        }
    }

}
