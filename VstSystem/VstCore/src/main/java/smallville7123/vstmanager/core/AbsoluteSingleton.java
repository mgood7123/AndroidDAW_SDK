package smallville7123.vstmanager.core;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * There can be only one - ie. even if the class is loaded in several different classloaders,
 * there will be only one instance of the object.
 */
public class AbsoluteSingleton implements SingletonInterface {

    /**
     *  This is effectively an instance of this class (although actually it may be instead a
     *  java.lang.reflect.Proxy wrapping an instance from the original classloader).
     */
    public static SingletonInterface instance = null;
    /**
     * Retrieve an instance of AbsoluteSingleton from the original classloader. This is a true
     * Singleton, in that there will only be one instance of this object in the virtual machine,
     * even though there may be several copies of its class file loaded in different classloaders.
     * @param classLoader
     */
    public synchronized static SingletonInterface getInstance(ClassLoader classLoader) {
        ClassLoader myClassLoader = AbsoluteSingleton.class.getClassLoader();

        // try to obtain myClassLoader from parent class loader
        ClassLoader parent;
        Class A;
        Class B;
        ClassLoader xx;
        if (instance==null) {
            // on Android, we need to acquire the classloader of a package context
            // this is created by context.createPackageContext(packageID,flags).getClassLoader();
            if (classLoader != null) {
                try {
                    parent = myClassLoader.getParent();
                    A = parent.loadClass(myClassLoader.getClass().getName());
                    B = parent.loadClass(classLoader.getClass().getName());

                    // And get the other version of our current class
                    Class otherClassInstance = classLoader.loadClass(AbsoluteSingleton.class.getName());
                    // And call its getInstance method - this gives the correct instance of ourself
                    Method getInstanceMethod = otherClassInstance.getDeclaredMethod("getInstance", ClassLoader.class);
                    Object otherAbsoluteSingleton = getInstanceMethod.invoke(null, new Object[] { null } );
                    // But, we can't cast it to our own interface directly because classes loaded from
                    // different classloaders implement different versions of an interface.
                    // So instead, we use java.lang.reflect.Proxy to wrap it in an object that *does*
                    // support our interface, and the proxy will use reflection to pass through all calls
                    // to the object.
                    instance = (SingletonInterface) Proxy.newProxyInstance(myClassLoader,
                            new Class[] { SingletonInterface.class },
                            new PassThroughProxyHandler(otherAbsoluteSingleton));
                    // And catch the usual tedious set of reflection exceptions
                    // We're cheating here and just catching everything - don't do this in real code
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                // We're in the root classloader, so the instance we have here is the correct one
            } else {
                instance = new AbsoluteSingleton();
            }
        }

        return instance;
    }

    private AbsoluteSingleton() {
    }

    RET ret = null;

    public RET getRET() {
        return ret;
    }

    public void setRET(RET ret) {
        this.ret = ret;
    }

    private String value = "";
    public String getValue() { return value; }
    public void setValue(String value) {
        this.value = value;
    }

}