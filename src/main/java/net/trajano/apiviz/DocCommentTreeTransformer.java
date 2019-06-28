package net.trajano.apiviz;

import com.sun.source.doctree.DocCommentTree;

import javax.lang.model.element.Element;

@FunctionalInterface
public interface DocCommentTreeTransformer {
    DocCommentTree apply(Element e, DocCommentTree tree);
}
