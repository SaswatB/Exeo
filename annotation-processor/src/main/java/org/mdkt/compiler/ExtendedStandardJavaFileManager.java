package org.mdkt.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by trung on 5/3/15.
 */
public class ExtendedStandardJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private HashMap<String, CompiledCode> compiledCodes = new HashMap<>();
    private DynamicClassLoader cl;

    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager delegate to this file manager
     * @param cl
     */
    public ExtendedStandardJavaFileManager(JavaFileManager fileManager, DynamicClassLoader cl) {
        super(fileManager);
        this.cl = cl;
    }

    public void addCompiledCode(CompiledCode compiledCode, String clss) {
        cl.setCode(compiledCode);
        compiledCodes.put(clss, compiledCode);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        return compiledCodes.get(className);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return cl;
    }
}
