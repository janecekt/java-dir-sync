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

import com.jdirsync.ui.MainApplication;

/**
 * Wrapper class for main function.
 */
public class Main {
    private static void printUsage() {
        System.err.println("Usage: java -jar ./jdirsync.jar -ui <leftName> <leftPath> <rightName> <rightPath>");
    }

    /**
     * Program main function.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length <= 1) {
                printUsage();
                System.exit(1);
            }

            if ("-ui".equals(args[0]) && args.length == 5) {
                MainApplication.startUIMode(args);
            } else {
                printUsage();
                System.exit(1);
            }
        } catch (Exception ex) {
            System.err.println("Exception occurred during initialization :" + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
