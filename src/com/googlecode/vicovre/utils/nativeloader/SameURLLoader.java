/**
 * Copyright (c) 2009, University of Manchester
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3) Neither the name of the and the University of Manchester nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.googlecode.vicovre.utils.nativeloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * Loads a native library from the same url as the class.
 * @author Andrew G D Rowley
 * @version 1.0
 */
public class SameURLLoader implements Loader {

    private static final int BUFFER_SIZE = 1024;

    /**
     * {@inheritDoc}
     * @throws LoadException
     * @see com.googlecode.vicovre.utils.nativeloader.Loader#load(
     *     java.lang.Class, java.lang.String)
     */
    public void load(final Class<?> loadingClass, final String name)
            throws LoadException {
        String pathToClass = "/"
            + loadingClass.getCanonicalName().replace(".", "/") + ".class";
        URL classUrl = loadingClass.getResource(pathToClass);
        if (classUrl.getProtocol().equals("jar")) {
            String url = classUrl.toString();
            String jarUrl = url.substring("jar".length() + 1,
                    url.indexOf("!"));
            String dirUrl = jarUrl.substring(0, jarUrl.lastIndexOf("/"));

            try {
                URL lib = new URL(dirUrl + "/" + name);
                if (lib.getProtocol().equals("file")) {
                    File file = new File(lib.getFile());
                    if (file.exists()) {
                        System.load(file.getAbsolutePath());
                        return;
                    }
                } else {
                    URLConnection connection = lib.openConnection();
                    InputStream input = connection.getInputStream();
                    File file = new File(NativeLoader.USER_LIB_DIR, name);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        FileOutputStream output = new FileOutputStream(file);
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead = input.read(buffer);
                        while (bytesRead != -1) {
                            output.write(buffer, 0, bytesRead);
                            bytesRead = input.read(buffer);
                        }
                        output.close();
                        input.close();
                    }
                    System.load(file.getAbsolutePath());
                    return;
                }
            } catch (Throwable e) {
                throw new LoadException(e);
            }
        }
        throw new UnsatisfiedLinkError("Could not find " + name
                + " at same url as jar");
    }

}
