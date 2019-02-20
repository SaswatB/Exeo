package com.hstar.exeo.server.websockets.annotations;

import javafx.util.Pair;
import org.mdkt.compiler.CompiledCode;
import org.mdkt.compiler.DynamicClassLoader;
import org.mdkt.compiler.ExtendedStandardJavaFileManager;
import org.mdkt.compiler.SourceCode;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Processor that generates a javascript file with constants from this project
 * Created by Saswat on 10/28/2016.
 */
//@AutoService(Processor.class)
public class JavascriptConstantProcessor extends AbstractProcessor {

    private Messager messager;

    private Path javaDirectory;
    private Path annotationSrcDirectory;
    private File constantsFile;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();

        try {
            Filer filer = processingEnv.getFiler();
            FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "tmp", (Element[]) null);
            Path projDir = Paths.get(resource.toUri()).getParent().getParent().getParent().getParent();
            Path mainDir = projDir.resolve("src").resolve("main");
            constantsFile = mainDir.resolve("webapp").resolve("js").resolve("utils").resolve("java-constants.js").toFile();
            javaDirectory = mainDir.resolve("java");
            annotationSrcDirectory = projDir.resolve("annotation-processor").resolve("src").resolve("main").resolve("java");
            resource.delete();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot access java-constants.js", e);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(annotations.size() > 0) {
            if(constantsFile.exists()) {
                constantsFile.delete();
            }
        } else {
            return true;
        }
        try(FileWriter fw = new FileWriter(constantsFile, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter writer = new PrintWriter(bw)) {

            writer.println("//AUTO-GENERATED FILE, DO NOT EDIT");

            HashSet<String> const_classes = new HashSet<>();
            HashSet<String> enum_classes = new HashSet<>();
            for(Element e : roundEnv.getElementsAnnotatedWith(JavascriptConstant.class)) {
                const_classes.add(e.getEnclosingElement().toString());
            }
            for(Element e : roundEnv.getElementsAnnotatedWith(JavascriptEnum.class)) {
                enum_classes.add(e.toString());
            }
            for(String clss : const_classes) {
                compileClass(clss, JavascriptConstant.class, (classes)-> processConstants(classes.getKey(), classes.getValue(), writer));
            }
            for(String clss : enum_classes) {
                compileClass(clss, JavascriptEnum.class, (classes) -> processEnum(classes.getKey(), classes.getValue(), writer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    private void processConstants(Class clss, Class annotation, PrintWriter output) {
        for(Field f : clss.getDeclaredFields()) {
            //noinspection unchecked
            if(f.isAnnotationPresent(annotation)) {
                String value = null;
                try {
                    value = f.get(null).toString();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if(f.getType().equals(String.class)) {
                    value = "\"" + value.replace("\"", "\\\"") + "\"";
                }
                printConstant(output, f.getName(), value);
            }
        }
    }

    private void processEnum(Class clss, Class annotation, PrintWriter output) {
        if(!clss.isEnum()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "JavascriptEnum annotation is only for enums");
            return;
        }
        printConstant(output, clss.getSimpleName(), "{}");//todo fix this hack when we switch to macros
        for(Object consts : clss.getEnumConstants()) {
            printConstant(output, clss.getSimpleName()+"."+((Enum)consts).name(), "\""+consts.toString()+"\"");
        }
    }

    //this is awesome...
    private void compileClass(String clss, Class annotation, Consumer<Pair<Class, Class>> consumer) throws IOException {
        String source = new String(Files.readAllBytes(canonical2path(clss, javaDirectory)));
        String jcSource = new String(Files.readAllBytes(canonical2path(annotation.getCanonicalName(), annotationSrcDirectory))).replace("@Retention(RetentionPolicy.SOURCE)", "@Retention(RetentionPolicy.RUNTIME)");

        try {

            JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
            SourceCode sourceCode = new SourceCode(clss, source);
            SourceCode jcSourceCode = new SourceCode(annotation.getCanonicalName(), jcSource);
            CompiledCode compiledCode = new CompiledCode(clss);
            CompiledCode jcCompiledCode = new CompiledCode(annotation.getCanonicalName());
            Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(sourceCode, jcSourceCode);
            DynamicClassLoader cl = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
            ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), cl);
            fileManager.addCompiledCode(compiledCode, clss);
            fileManager.addCompiledCode(jcCompiledCode, annotation.getCanonicalName());
            JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, null, null, null, compilationUnits);
            boolean result = task.call();
            Class c = cl.loadClass(clss);
            Class jc = cl.loadClass(annotation.getCanonicalName());

            consumer.accept(new Pair<>(c, jc));
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.toString());
            e.printStackTrace();
        }
    }

    public Path canonical2path(String clss, Path base) {
        String parts[] = clss.split("\\.");
        parts[parts.length-1] += ".java";
        Path path = base;
        for(String part : parts) {
            path = path.resolve(part);
        }
        return path;
    }

    public void printConstant(PrintWriter writer, String name, String value) {
        //todo figure out sweetjs and re-enable this code
        //writer.println("syntax "+name+" = function(ctx) {");
        //writer.println("    return " + value + ";");
        //writer.println("}");
        writer.println("window."+name+" = "+value+";");//todo use a macro processor instead of this -_-
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Arrays.asList(JavascriptConstant.class.getCanonicalName(), JavascriptEnum.class.getCanonicalName()));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
