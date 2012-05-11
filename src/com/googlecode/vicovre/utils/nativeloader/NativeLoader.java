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
import java.util.HashMap;
import java.util.Vector;



/**
 * A class for loading native libraries.
 * @author Andrew G D Rowley
 * @version 1.0
 */
public final class NativeLoader {

    /**
     * The directory to store libraries in.
     */
    protected static final File USER_LIB_DIR =
            new File(System.getProperty("user.home"), ".native");

    private static final Loader[] LOADERS = new Loader[]{
        new SameURLLoader(),
        new ResourceLoader()
    };

    private static final Vector<String> PREFIXES = new Vector<String>();

    private static void addOSParts(final String prefix) {
        PREFIXES.add(prefix);
        String[] os = System.getProperty("os.name").split(" ");
        String version = System.getProperty("os.version");

        String osName = "";
        for (String part : os) {
            if (!osName.equals("")) {
                osName += "/";
            }
            osName += part.toLowerCase();
            PREFIXES.add(prefix + osName + "/");
        }
        osName += "/" + version.toLowerCase() + "/";
        PREFIXES.add(prefix + osName);
    }

    static {
        String arch = System.getProperty("os.arch");
        addOSParts("");
        addOSParts("native/");
        addOSParts(arch + "/");
        addOSParts("native/" + arch.toLowerCase() + "/");
    }

    private static final HashMap<String, Integer> LOADING =
        new HashMap<String, Integer>();

    private static final HashMap<String, String> LOADERROR =
        new HashMap<String, String>();

    private NativeLoader() {
        // Does Nothing
    }

    /**
     * Loads the library with the given name.
     * @param loadingClass The class that is loading the library
     * @param name The system-independent name of the library to load
     */
    public static void loadLibrary(final Class<?> loadingClass,
            final String name) {
        Integer sync = null;
        synchronized (LOADING) {
            sync = LOADING.get(name);
            if (sync == null) {
                sync = new Integer(0);
                LOADING.put(name, sync);
            }
        }

        synchronized (sync) {
            String loadError = LOADERROR.get(name);
            if (loadError != null) {
                if (loadError.equals("")) {
                    return;
                }
                throw new UnsatisfiedLinkError(loadError);
            }

            try {
                System.loadLibrary(name);
                return;
            } catch (UnsatisfiedLinkError e) {
                USER_LIB_DIR.mkdirs();
                String libraryName = System.mapLibraryName(name);
                for (Loader loader : LOADERS) {
                    for (String prefix : PREFIXES) {
                        try {
                            loader.load(loadingClass, prefix + libraryName);
                            LOADERROR.put(name, "");
                            return;
                        } catch (UnsatisfiedLinkError error) {
                            // Do Nothing
                        } catch (LoadException error) {
                            error.printStackTrace();
                            loadError = error.getMessage();
                        }
                    }
                }
                String error = "Could not find library " + name;
                if (loadError != null) {
                    error += ": " + loadError;
                }
                LOADERROR.put(name, error);
                throw new UnsatisfiedLinkError(error);
            }
        }
    }

    /**
     * Main method for testing architectures.
     * @param args Ignored
     */
    public static void main(final String[] args) {
        System.err.println(
            "Shared libraries can go into one of the following locations:");
        for (String prefix : PREFIXES) {
            System.err.println("    /" + prefix);
        }
    }
}
