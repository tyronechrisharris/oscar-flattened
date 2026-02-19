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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Custom class loader for finding native libraries anywhere on the classpath.
 * </p><p>
 * Native libraries must be stored in a folder named "lib/native/{os.name}/{os.arch}/"
 * (e.g. lib/native/linux/x86_64/libsomething.so)
 * </p>
 *
 * @author Alex Robin
 * @since Sep 3, 2015
 */
public class NativeClassLoader extends URLClassLoader
{
    static final String NATIVES_CACHE_FOLDER = "osh_natives";
    
    private Logger log; // cannot be a static logger in case it is used as system classloader
    private Map<String, String> loadedLibraries = new HashMap<>();
    private File tmpDir;
    

    public NativeClassLoader()
    {
        this(NativeClassLoader.class.getClassLoader());
    }


    public NativeClassLoader(ClassLoader parent)
    {
        super("NativeClassLoader", new URL[0], parent);
        createTempDir();
        parseClasspath();
    }
    
    
    protected void parseClasspath()
    {
        String classPath = System.getProperty("java.class.path");
        //System.out.println(classPath);
         
        try
        {
            for (String filePath: classPath.split(File.pathSeparator))
            {
                String url = "file:" + filePath;
                if (filePath.toLowerCase().endsWith(".jar"))
                    url = "jar:" + url + "!/";
                else if (!url.endsWith("/"))
                    url += "/";
                    
                this.addURL(new URL(url));
            }
        }
        catch (Exception e)
        {
            log.error("Cannot parse classpath:\n{}", classPath, e);
        }
    }
    
    
    protected void createTempDir()
    {
        try
        {
            this.tmpDir = FileUtils.createTempDirectory(NATIVES_CACHE_FOLDER);
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run()
                {
                    // remove temp folder
                    try
                    {
                        if (tmpDir != null)
                            FileUtils.deleteRecursively(tmpDir);
                    }
                    catch (IOException e)
                    {
                        if (log != null)
                            log.error("Cannot delete folder " + tmpDir, e);
                    }
                }
            });
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Cannot create temporary native lib filder", e);
        }
    }


    static String osName()
    {
        String osname = System.getProperty("os.name");
        if (osname.startsWith("Linux"))
            return "linux";
        if (osname.startsWith("Mac OS X"))
            return "macosx";
        if (osname.startsWith("SunOS"))
            return "solaris";
        if (osname.startsWith("Windows"))
            return "windows";
        return "unknown";
    }


    static String osArch()
    {
        String osarch = System.getProperty("os.arch");
        
        if ("amd64".equals(osarch))
            return "x86_64";
        
        if ("i386".equals(osarch))
            return "x86";
        
        if ("arm".equals(osarch))
        {
            String armabi = System.getProperty("os.armabi");
            if (armabi == null)
                armabi = "v6_hf";
            return osarch + armabi;
        }
        
        return osarch;
    }


    @Override
    protected String findLibrary(String libName)
    {
        ensureLogger();
        
        // get path directly if we have already found this library
        String libPath = loadedLibraries.get(libName);
        
        // first try with specific OS name
        if (libPath == null)
        {
            String libFileName = System.mapLibraryName(libName);
            libPath = findLibraryFile(libName, libFileName);
        }
        
        // otherwise try directly with library name
        if (libPath == null)
            libPath = findLibraryFile(libName, libName);
        
        return libPath;
    }
    
    
    protected String findLibraryFile(String libName, String libFileName)
    {
        // try to get it from embedded native lib folder
        URL url = findResource("lib/native/" + osName() + "/" + osArch() + "/" + libFileName);
        
        // if we have nothing return null to let VM search for it in java.library.path
        if (url == null)
            return null;
        
        return getResourcePath(libName, url);
    }
    
    
    private String getResourcePath(String libName, URL url)
    {
        String libPath;
        
        try
        {
            URLConnection con = url.openConnection();
                        
            if (con instanceof JarURLConnection)
            {                
                // extract resource from jar
                JarURLConnection jarItemConn = (JarURLConnection)con;
                File libFile = extractResource(jarItemConn);
                
                // use path to temp file
                libPath = libFile.getAbsolutePath();
            }
            else
            {
                // if not in JAR, use filesystem path directly
                libPath = url.getFile();
            }
            
            loadedLibraries.put(libName, libPath);
            log.debug("Using native library from: {}", libPath);
            return libPath;
        }
        catch (Exception e)
        {
            log.trace("Cannot find library {} in {}", libName, url, e);
            return null;
        }
    }
    
    
    private File extractResource(JarURLConnection jarItemConn) throws IOException
    {
        // prepare temp location
        File jarFile = new File(jarItemConn.getJarFile().getName());
        File jarTmpDir = new File(tmpDir, jarFile.getName());
        File outFile = new File(jarTmpDir, jarItemConn.getJarEntry().getName());
        outFile.getParentFile().mkdirs();
        
        // extract to temp location
        try (InputStream in = new BufferedInputStream(jarItemConn.getInputStream());
             OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile)))
        {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0)
                out.write(buffer, 0, len);
        }
        
        return outFile;
    }


    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        Class<?> c = findLoadedClass(name);
        
        if (c == null)
        {
            try
            {
                c = getParent().getParent().loadClass(name);
            }
            catch (ClassNotFoundException e)
            {
                c = findClass(name);
                // cannot log in a classloader because the logger class itself may not be loaded!
            }
        }

        if (resolve)
        {
            resolveClass(c);
        }
        
        return c;
    }
    
    
    private void ensureLogger()
    {
        // setup logger cause it doesn't work the static way when used as system class loader
        if (log == null)
            log = LoggerFactory.getLogger(NativeClassLoader.class);
    }

    /**
     * Called by the VM to support dynamic additions to the class path
     *
     * @see java.lang.instrument.Instrumentation#appendToSystemClassLoaderSearch
     */
    void appendToClassPathForInstrumentation(String path) {
        try
        {
            addURL(new URL("file:" + path));
        }
        catch (MalformedURLException e)
        {
            if (log != null)
                log.error("Error adding instrumentation to classpath", e);
        }
    }
}
