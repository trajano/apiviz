package net.trajano.apiviz;

import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.DocletEnvironment;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.util.Set;

public class WrappedDocletEnvironment implements DocletEnvironment {
    private final DocletEnvironment docletEnvironment;
    private WrappedDocTrees docTrees;

    public WrappedDocletEnvironment(DocletEnvironment docletEnvironment) {
        this.docletEnvironment = docletEnvironment;
        docTrees = new WrappedDocTrees(docletEnvironment.getDocTrees());
    }

    public void setDocCommentTreeTransformer(DocCommentTreeTransformer transformer) {
        docTrees.setTransformer(transformer);
    }

    @Override
    public Set<? extends Element> getSpecifiedElements() {
        return docletEnvironment.getSpecifiedElements();
    }

    @Override
    public Set<? extends Element> getIncludedElements() {
        return docletEnvironment.getIncludedElements();
    }

    @Override
    public DocTrees getDocTrees() {
        return docTrees;
    }

    @Override
    public Elements getElementUtils() {
        return docletEnvironment.getElementUtils();
    }

    @Override
    public Types getTypeUtils() {
        return docletEnvironment.getTypeUtils();
    }

    @Override
    public boolean isIncluded(Element e) {
        return docletEnvironment.isIncluded(e);
    }

    @Override
    public boolean isSelected(Element e) {
        return docletEnvironment.isSelected(e);
    }

    @Override
    public JavaFileManager getJavaFileManager() {
        return docletEnvironment.getJavaFileManager();
    }

    @Override
    public SourceVersion getSourceVersion() {
        return docletEnvironment.getSourceVersion();
    }

    @Override
    public ModuleMode getModuleMode() {
        return docletEnvironment.getModuleMode();
    }

    @Override
    public JavaFileObject.Kind getFileKind(TypeElement type) {
        return docletEnvironment.getFileKind(type);
    }
}
