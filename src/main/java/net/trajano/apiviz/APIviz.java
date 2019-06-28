package net.trajano.apiviz;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class APIviz implements Doclet {

    private Locale locale;

    private Reporter reporter;

    private boolean author;

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.locale = locale;
        this.reporter = reporter;
    }

    @Override
    public String getName() {
        return "APIviz";
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Set.of(
                new Option() {
                    @Override
                    public int getArgumentCount() {
                        return 0;
                    }

                    @Override
                    public String getDescription() {
                        return "Includes the @author text in the generated docs.";
                    }

                    @Override
                    public Kind getKind() {
                        return Kind.STANDARD;
                    }

                    @Override
                    public List<String> getNames() {
                        return List.of("author");
                    }

                    @Override
                    public String getParameters() {
                        return null;
                    }

                    @Override
                    public boolean process(String option, List<String> arguments) {
                        reporter.print(Diagnostic.Kind.ERROR, "option=" + option);
                        APIviz.this.author = true;
                        return true;
                    }
                }
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean run(DocletEnvironment docEnv) {
        DocTrees docTrees = docEnv.getDocTrees();
        for (TypeElement t : ElementFilter.typesIn(docEnv.getIncludedElements())) {
            System.out.println(t.getKind() + ":" + t);
            for (Element e : t.getEnclosedElements()) {
                printElement(docTrees, e);
            }
        }
        return true;
    }

    private void printElement(DocTrees trees, Element e) {
        DocCommentTree docCommentTree = trees.getDocCommentTree(e);
        if (docCommentTree != null) {
            System.out.println("Element (" + e.getKind() + ": "
                    + e + ") has the following comments:");
            System.out.println("Entire body: " + docCommentTree.getFullBody());
            System.out.println("Block tags: " + docCommentTree.getBlockTags());
        }
    }
//    @Override
//    public boolean run(DocletEnvironment docEnv) {
////        System.out.println(docEnv.getDocTrees().getSourcePositions());
////        System.out.println();
////        System.out.println(docEnv.getElementUtils());
////        System.out.println(docEnv.getElementUtils().getAllModuleElements());
////        final Set<? extends Element> specifiedElements = docEnv.getSpecifiedElements();
////        for (Element e : specifiedElements) {
////            final PackageElement pe = (PackageElement) e;
////            System.out.println(docEnv.getDocTrees().getDocCommentTree(pe));
////        }
////        System.out.println("Spec elements="+ specifiedElements.iterator().next().getKind());
//////        System.out.println("package=" + docEnv.getElementUtils().getAllPackageElements(null));
////        System.out.println(docEnv.getTypeUtils());
////        System.out.println(docEnv.getSourceVersion());
//        WrappedDocletEnvironment wrappedDocletEnvironment = new WrappedDocletEnvironment(docEnv);
//        wrappedDocletEnvironment.setDocCommentTreeTransformer(
//                (e, t) -> {
//                    System.out.println(e + " " + t);
//                    return t;
//                }
//        );
//        return super.run(wrappedDocletEnvironment);
//    }
//
//    @Override
//    public String getName() {
//        return "APIviz";
//    }
}
