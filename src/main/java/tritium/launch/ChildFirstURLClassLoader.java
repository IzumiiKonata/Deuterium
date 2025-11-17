package tritium.launch;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

public class ChildFirstURLClassLoader extends URLClassLoader {

    public ChildFirstURLClassLoader(URL[] urls) {
        super(urls);
    }

    public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            
            if (c == null) {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException e) {
                    ClassLoader parent = getParent();
                    if (parent != null) {
                        c = parent.loadClass(name);
                    } else {
                        c = getSystemClassLoader().loadClass(name);
                    }
                }
            }
            
            if (resolve) {
                resolveClass(c);
            }
            
            return c;
        }
    }
    
    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null) {
            ClassLoader parent = getParent();
            if (parent != null) {
                url = parent.getResource(name);
            } else {
                url = getSystemClassLoader().getResource(name);
            }
        }
        return url;
    }
    
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> urls = findResources(name);
        if (urls == null || !urls.hasMoreElements()) {
            ClassLoader parent = getParent();
            if (parent != null) {
                urls = parent.getResources(name);
            } else {
                urls = getSystemClassLoader().getResources(name);
            }
        }
        return urls;
    }

}