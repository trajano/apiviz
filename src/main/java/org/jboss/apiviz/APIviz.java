/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.apiviz;

import com.sun.javadoc.*;
import jdepend.framework.JDepend;
import jdepend.framework.JavaClass;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;
import jdk.javadoc.doclet.StandardDoclet;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jboss.apiviz.Constant.*;

/**
 * @author The APIviz Project (apiviz-dev@lists.jboss.org)
 * @author Trustin Lee (tlee@redhat.com)
 */
public class APIviz {

    private static final Pattern INSERTION_POINT_PATTERN = Pattern.compile(
            "((<\\/PRE>)(?=\\s*(<P>|<div[^>]*block))|(?=<TABLE BORDER=\"1\")|(<div[^>]*contentContainer[^>]*>))",
            Pattern.CASE_INSENSITIVE);

    public static boolean start(RootDoc root) {
        StandardDoclet standard = new StandardDoclet();
        standard.run(null);
        root = new APIvizRootDoc(root);
//        if (!standard.init(root)) {
//            return false;
//        }

        if (!Graphviz.isAvailable(root)) {
            root.printWarning("Graphviz is not found.");
            root.printWarning("Please install graphviz and specify -Dgraphviz.home Otherwise, you might have specified incorrect graphviz home Graphviz is not found in the system path.");
            root.printWarning("Skipping diagram generation.");
            return true;
        }

        try {
            File outputDirectory = getOutputDirectory(root.options());
            ClassDocGraph graph = new ClassDocGraph(root);
            if (shouldGeneratePackageDiagram(root.options())) {
                generateOverviewSummary(root, graph, outputDirectory);
            }
            generatePackageSummaries(root, graph, outputDirectory);
            generateClassDiagrams(root, graph, outputDirectory);
        } catch (Throwable t) {
            root.printError(
                    "An error occurred during diagram generation: " +
                            t.toString());
            t.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean validOptions(String[][] options, DocErrorReporter errorReporter) {
        for (String[] o : options) {
            if (OPTION_SOURCE_CLASS_PATH.equals(o[0])) {
                File[] cp = getClassPath(options);
                if (cp.length == 0) {
                    errorReporter.printError(
                            OPTION_SOURCE_CLASS_PATH +
                                    " requires at least one valid class path.");
                    return false;
                }
                for (File f : cp) {
                    if (!f.exists() || !f.canRead()) {
                        errorReporter.printError(
                                f.toString() +
                                        " doesn't exist or is not readable.");
                        return false;
                    }
                }
            }
        }

        List<String[]> newOptions = new ArrayList<String[]>();
        for (String[] o : options) {
            if (OPTION_CATEGORY.equals(o[0])) {
                continue;
            }
            if (OPTION_SOURCE_CLASS_PATH.equals(o[0])) {
                continue;
            }
            if (OPTION_NO_PACKAGE_DIAGRAM.equals(o[0])) {
                continue;
            }

            newOptions.add(o);
        }

        return false;
//        return StandardDoclet.validOptions(
//                newOptions.toArray(new String[newOptions.size()][]),
//                errorReporter);
    }

    public static int optionLength(String option) {
        if (OPTION_CATEGORY.equals(option)) {
            return 2;
        }

        if (OPTION_SOURCE_CLASS_PATH.equals(option)) {
            return 2;
        }

        if (OPTION_NO_PACKAGE_DIAGRAM.equals(option)) {
            return 1;
        }

//        int answer = StandardDoclet.optionLength(option);
        int answer = 0;
        if (option.equals(OPTION_HELP)) {
            // Print the options provided by APIviz.
            System.out.println();
            System.out.println("Provided by APIviz doclet:");
            System.out.println(OPTION_SOURCE_CLASS_PATH + " <pathlist>     Specify where to find source class files");
            System.out.println(OPTION_NO_PACKAGE_DIAGRAM + "               Do not generate the package diagram in the overview summary");
            System.out.println(OPTION_CATEGORY + "                       <category>[:<fillcolor>[:<linecolor>]] ");
            System.out.println("                                    Color for items marked with " + TAG_CATEGORY);
        }

        return answer;
    }

    public static LanguageVersion languageVersion() {
        //return Standard.languageVersion();
        return null;
    }

    private static void generateOverviewSummary(RootDoc root, ClassDocGraph graph, File outputDirectory) throws IOException {
        final Map<String, PackageDoc> packages = getPackages(root);

        PackageFilter packageFilter = PackageFilter.all();

        for (Map.Entry<String, PackageDoc> entry : packages.entrySet()) {
            String packageName = entry.getKey();
            PackageDoc p = entry.getValue();

            if (!ClassDocGraph.isHidden(p)) {
                packageFilter.including(packageName);
            }
        }

        JDepend jdepend = new JDepend(packageFilter.excludingRest());

        File[] classPath = getClassPath(root.options());
        for (File e : classPath) {
            if (e.isDirectory()) {
                root.printNotice(
                        "Included into dependency analysis: " + e);
                jdepend.addDirectory(e.toString());
            } else {
                root.printNotice(
                        "Excluded from dependency analysis: " + e);
            }
        }

        jdepend.analyze();

        if (checkClasspathOption(root, jdepend)) {
            instrumentDiagram(
                    root, outputDirectory, "overview-summary",
                    graph.getOverviewSummaryDiagram(jdepend));
        } else {
            root.printWarning(
                    "Please make sure that the '" +
                            OPTION_SOURCE_CLASS_PATH +
                            "' option was specified correctly.");
            root.printWarning(
                    "Package dependency diagram will not be generated " +
                            "to avoid the inaccurate result.");
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean checkClasspathOption(RootDoc root, JDepend jdepend) {
        // Sanity check
        boolean correctClasspath = true;
        if (jdepend.countClasses() == 0) {
            root.printWarning(
                    "JDepend was not able to locate any compiled class files.");
            correctClasspath = false;
        } else {
            for (ClassDoc c : root.classes()) {
                if (c.containingPackage() == null ||
                        c.containingPackage().name() == null ||
                        ClassDocGraph.isHidden(c.containingPackage())) {
                    continue;
                }

                boolean found = false;
                String fqcn = c.containingPackage().name() + '.' + c.name().replace('.', '$');
                JavaPackage jpkg = jdepend.getPackage(c.containingPackage().name());
                if (jpkg != null) {
                    Collection<JavaClass> jclasses = jpkg.getClasses();
                    if (jclasses != null) {
                        for (JavaClass jcls : jclasses) {
                            if (fqcn.equals(jcls.getName())) {
                                found = true;
                                break;
                            }
                        }
                    }
                }

                if (!found) {
                    root.printWarning(
                            "JDepend was not able to locate some compiled class files: " + fqcn);
                    correctClasspath = false;
                    break;
                }
            }
        }
        return correctClasspath;
    }

    private static void generatePackageSummaries(RootDoc root, ClassDocGraph graph, File outputDirectory) throws IOException {
        for (PackageDoc p : getPackages(root).values()) {
            instrumentDiagram(
                    root, outputDirectory,
                    p.name().replace('.', File.separatorChar) +
                            File.separatorChar + "package-summary",
                    graph.getPackageSummaryDiagram(p));
        }
    }

    private static void generateClassDiagrams(RootDoc root, ClassDocGraph graph, File outputDirectory) throws IOException {
        for (ClassDoc c : root.classes()) {
            if (c.containingPackage() == null) {
                instrumentDiagram(
                        root, outputDirectory,
                        c.name(),
                        graph.getClassDiagram(c));
            } else {
                instrumentDiagram(
                        root, outputDirectory,
                        c.containingPackage().name().replace('.', File.separatorChar) +
                                File.separatorChar + c.name(),
                        graph.getClassDiagram(c));
            }
        }
    }

    static Map<String, PackageDoc> getPackages(RootDoc root) {
        Map<String, PackageDoc> packages = new TreeMap<String, PackageDoc>();
        for (ClassDoc c : root.classes()) {
            PackageDoc p = c.containingPackage();
            if (!packages.containsKey(p.name())) {
                packages.put(p.name(), p);
            }
        }

        return packages;
    }

    private static void instrumentDiagram(RootDoc root, File outputDirectory, String filename, String diagram) throws IOException {
        // TODO - it would be nice to have a debug flag that would spit out the graphviz source as well
        //System.out.println(diagram);

        boolean needsBottomMargin = filename.contains("overview-summary") || filename.contains("package-summary");

        File htmlFile = new File(outputDirectory, filename + ".html");
        File pngFile = new File(outputDirectory, filename + ".png");
        File mapFile = new File(outputDirectory, filename + ".map");

        if (!htmlFile.exists()) {
            // Shouldn't reach here anymore.
            // I'm retaining the code just in case.
            for (; ; ) {
                int idx = filename.lastIndexOf(File.separatorChar);
                if (idx > 0) {
                    filename = filename.substring(0, idx) + '.' +
                            filename.substring(idx + 1);
                } else {
                    // Give up (maybe missing)
                    return;
                }
                htmlFile = new File(outputDirectory, filename + ".html");
                if (htmlFile.exists()) {
                    pngFile = new File(outputDirectory, filename + ".png");
                    mapFile = new File(outputDirectory, filename + ".map");
                    break;
                }
            }
        }

        root.printNotice("Generating " + pngFile + "...");
        Graphviz.writeImageAndMap(root, diagram, outputDirectory, filename);

        try {
            String oldContent = FileUtil.readFile(htmlFile);
            String mapContent = FileUtil.readFile(mapFile);

            Matcher matcher = INSERTION_POINT_PATTERN.matcher(oldContent);
            if (!matcher.find()) {
                throw new IllegalStateException(
                        "Failed to find an insertion point: " + htmlFile);
            }
            String style = "text-align: center;";
            if (needsBottomMargin) {
                style += "margin-bottom: 1em;";
            }
            String newContent =
                    oldContent.substring(0, matcher.end()) + NEWLINE +
                            mapContent +
                            "<div id=\"apivizContainer\" style=\"" + style + "\">" +
                            "<img src=\"" + pngFile.getName() +
                            "\" usemap=\"#APIVIZ\" border=\"0\"></div>" +
                            oldContent.substring(matcher.end());
            FileUtil.writeFile(htmlFile, newContent);
        } finally {
            mapFile.delete();
        }
    }

    private static File getOutputDirectory(String[][] options) {
        for (String[] o : options) {
            if (o[0].equals("-d")) {
                return new File(o[1]);
            }
        }

        // Fall back to the current working directory.
        return new File(System.getProperty("user.dir", "."));
    }

    private static boolean shouldGeneratePackageDiagram(String[][] options) {
        for (String[] o : options) {
            if (o[0].equals(OPTION_NO_PACKAGE_DIAGRAM)) {
                return false;
            }
        }
        return true;
    }

    private static File[] getClassPath(String[][] options) {
        Set<File> cp = new LinkedHashSet<File>();

        for (String[] o : options) {
            if (o[0].equals(OPTION_SOURCE_CLASS_PATH)) {
                String[] cps = o[1].split(File.pathSeparator);
                for (String p : cps) {
                    cp.add(new File(p));
                }
            }
        }

        for (String[] o : options) {
            if (o[0].equals("-classpath")) {
                String[] cps = o[1].split(File.pathSeparator);
                for (String p : cps) {
                    cp.add(new File(p));
                }
            }
        }

        return cp.toArray(new File[cp.size()]);
    }
}
