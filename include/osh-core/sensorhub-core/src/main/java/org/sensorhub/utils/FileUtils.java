/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.utils;

import static java.nio.file.attribute.PosixFilePermission.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.regex.Pattern;


/**
 * <p>
 * Static utility methods for dealing with files
 * </p>
 *
 * @author Alex Robin
 * @since Aug 27, 2015
 */
public class FileUtils
{
    private static final String ALLOWED_FILE_CHARS = "a-zA-Z0-9_.-";
    
    
    private FileUtils() {}
    
    
    /**
     * Creates a valid file name from the given string
     * @param name
     * @return a valid file name
     */
    public static String safeFileName(String name)
    {
        return name.replaceAll("[^" + ALLOWED_FILE_CHARS + "]+", "_");
    }
    
    
    /**
     * Checks if string can be used as file name
     * @param name
     * @return true if the name is a valid file name
     */
    public static boolean isSafeFileName(String name)
    {
        return (name != null) && (name.matches("[" + ALLOWED_FILE_CHARS + "]*"));
    }
    
    
    /**
     * Checks if string can be used as file path
     * @param path
     * @return true if the path is a valid file path
     */
    public static boolean isSafeFilePath(String path)
    {
        if (path == null || path.isEmpty())
            return false;
        
        Pattern regex = Pattern.compile("[" + ALLOWED_FILE_CHARS + "]*");
        String[] parts = path.split("/|" + Pattern.quote(File.separator));
        
        for (int i = 0; i < parts.length; i++)
        {
            String part = parts[i];
            
            // allow ':' suffix in first part for drive letters
            if (i == 0 && part.endsWith(":"))
                part = part.substring(0, part.length()-1);
            
            if (!regex.matcher(part).matches())
                return false;
        }
        
        return true;
    }
    
    
    /**
     * Recursively deletes a folder and all its content
     * @param folder
     * @throws IOException
     */
    public static void deleteRecursively(File folder) throws IOException
    {
        Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException
            {
                if (e == null)
                {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
                else
                {
                    throw e;
                }
            }

            @Override
            public FileVisitResult visitFile(Path f, BasicFileAttributes att) throws IOException
            {
                Files.delete(f);
                return FileVisitResult.CONTINUE;
            }
            
        });
    }
    
    
    public static File createTempDirectory(String prefix) throws IOException
    {
        boolean isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
        File tmpDir;
        
        // ensure temp directory is only accessible to user running osh
        if (isPosix) 
        {
            var attr = PosixFilePermissions.asFileAttribute(
                EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE));
            tmpDir = Files.createTempDirectory(prefix, attr).toFile();
        }
        else
        {
            tmpDir = Files.createTempDirectory(prefix).toFile();
            tmpDir.setReadable(false); // deny for all
            tmpDir.setWritable(false);
            tmpDir.setExecutable(false);
            tmpDir.setReadable(true, true); // allow for owner
            tmpDir.setWritable(true, true);
            tmpDir.setWritable(true, true);
        }
        
        return tmpDir;
    }

}
