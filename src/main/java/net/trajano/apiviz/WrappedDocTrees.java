package net.trajano.apiviz;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.tree.*;
import com.sun.source.util.*;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.List;

public class WrappedDocTrees extends DocTrees {
    private final DocTrees docTrees;

    public WrappedDocTrees(DocTrees docTrees) {
        this.docTrees = docTrees;
    }

    public void setTransformer(DocCommentTreeTransformer docCommentTreeTransformer) {
        this.docCommentTreeTransformer = docCommentTreeTransformer;
    }

    private DocCommentTreeTransformer docCommentTreeTransformer;

    @Override
    public BreakIterator getBreakIterator() {
        return null;
    }

    @Override
    public DocCommentTree getDocCommentTree(TreePath path) {
        return docTrees.getDocCommentTree(path);
    }

    @Override
    public DocCommentTree getDocCommentTree(Element e) {
        return docCommentTreeTransformer.apply(e, docTrees.getDocCommentTree(e));
    }

    @Override
    public DocCommentTree getDocCommentTree(FileObject fileObject) {
        return docTrees.getDocCommentTree(fileObject);
    }

    @Override
    public DocCommentTree getDocCommentTree(Element e, String relativePath) throws IOException {
        return docTrees.getDocCommentTree(e, relativePath);
    }

    @Override
    public DocTreePath getDocTreePath(FileObject fileObject, PackageElement packageElement) {
        return docTrees.getDocTreePath(fileObject, packageElement);
    }

    @Override
    public Element getElement(DocTreePath path) {
        return docTrees.getElement(path);
    }

    @Override
    public List<DocTree> getFirstSentence(List<? extends DocTree> list) {
        return docTrees.getFirstSentence(list);
    }

    @Override
    public DocSourcePositions getSourcePositions() {
        return docTrees.getSourcePositions();
    }

    @Override
    public Tree getTree(Element element) {
        return docTrees.getTree(element);
    }

    @Override
    public ClassTree getTree(TypeElement element) {
        return docTrees.getTree(element);
    }

    @Override
    public MethodTree getTree(ExecutableElement method) {
        return docTrees.getTree(method);
    }

    @Override
    public Tree getTree(Element e, AnnotationMirror a) {
        return docTrees.getTree(e, a);
    }

    @Override
    public Tree getTree(Element e, AnnotationMirror a, AnnotationValue v) {
        return docTrees.getTree(e, a, v);
    }

    @Override
    public TreePath getPath(CompilationUnitTree unit, Tree node) {
        return docTrees.getPath(unit, node);
    }

    @Override
    public TreePath getPath(Element e) {
        return docTrees.getPath(e);
    }

    @Override
    public TreePath getPath(Element e, AnnotationMirror a) {
        return docTrees.getPath(e, a);
    }

    @Override
    public TreePath getPath(Element e, AnnotationMirror a, AnnotationValue v) {
        return docTrees.getPath(e, a, v);
    }

    @Override
    public Element getElement(TreePath path) {
        return docTrees.getElement(path);
    }

    @Override
    public TypeMirror getTypeMirror(TreePath path) {
        return docTrees.getTypeMirror(path);
    }

    @Override
    public Scope getScope(TreePath path) {
        return docTrees.getScope(path);
    }

    @Override
    public String getDocComment(TreePath path) {
        return docTrees.getDocComment(path);
    }

    @Override
    public boolean isAccessible(Scope scope, TypeElement type) {
        return docTrees.isAccessible(scope, type);
    }

    @Override
    public boolean isAccessible(Scope scope, Element member, DeclaredType type) {
        return docTrees.isAccessible(scope, member, type);
    }

    @Override
    public TypeMirror getOriginalType(ErrorType errorType) {
        return docTrees.getOriginalType(errorType);
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg, Tree t, CompilationUnitTree root) {
        docTrees.printMessage(kind, msg, t, root);
    }

    @Override
    public TypeMirror getLub(CatchTree tree) {
        return docTrees.getLub(tree);
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg, DocTree t, DocCommentTree c, CompilationUnitTree root) {
        docTrees.printMessage(kind, msg, t, c, root);
    }

    @Override
    public void setBreakIterator(BreakIterator breakiterator) {
        docTrees.setBreakIterator(breakiterator);
    }

    @Override
    public DocTreeFactory getDocTreeFactory() {
        return docTrees.getDocTreeFactory();
    }
}
