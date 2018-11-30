package com.gradescope.jh61b.grader;

/*************************************************************************
 *  APIChecker.java
 * 
 *  For each class name provided as a command line argument, this program
 *  checks whether the class has the same API as the class with the same
 *  name prepended with "AGAPI". For example, the class "Foo" would be
 *  checked against "AGAPIFoo". Error messages are printed to the standard
 *  output stream if the paramter "print" is set to true.
 * 
 *  If the class's API has dependencies on other student-submitted
 *  classes, the reference solution should use the API version of the
 *  names. For example, if something in "Foo" takes a parameter of type
 *  "Bar", another student implemented class, "AGAPIFoo" should list the
 *  parameter type as "AGAPIBar".
 * 
 *  Because of this convention, the only names with the prefex "AGAPI" that
 *  appear in the reference solution should be the names of its own and
 *  other reference solution classes.
 * 
 *  Bug: If generics are used, the names for the type variables in the student
 *  and reference classes must match exactly.
 *
 *  Bug: If a no-arg constructor is declared private in AGAPIFoo, then it
 *  still accepts a class Foo with a public no-arg constructor.
 *
 *  Bug: doesn't seem to flag constructors with default access (package private)
 *       or protected access.
 *
 *  Bug: doesn't seem to check that class implements prescribed interfaces, e.g,
 *       AGAPITerm in autocomplete has "public class AGAPITerm implements Comparable<AGAPITerm>"
 *       but, while APIChecker checks for compareTo(), it doesn't check that
 *       it implememnts the given interface.
 *
 *  This class taken straight from Princeton's COS226 course.
 *  (Courtesy of Kevin Wayne at Princeton)
 *************************************************************************/

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.TreeMap;
import java.util.TreeSet;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

public class APIChecker {
    private static final String BULLET = "  *  ";
    private String name;
    private String refName;
    private Class<?> c;
    private Class<?> refC;
    private TreeMap<String, Integer> nameToIndex;
    private TreeMap<Integer, String> indexToName;

    /**
     * Create an instance of the APIChecker.
     * 
     * @param c         the class being checked
     * @param refC      the class used as the reference solution
     * @param name      the string name of the class being checked
     * @param refName   the string name of the reference solution
     */
    public APIChecker(Class<?> c, Class<?> refC, String name, String refName) {
        this.c = c;
        this.refC = refC;
        this.name = name;
        this.refName = refName;
        nameToIndex = new TreeMap<String, Integer>();
        indexToName = new TreeMap<Integer, String>();
        nameToIndex.put(refName, 0);
        indexToName.put(0, name);
    }

    public static boolean hasMethod(Class<?> c, String name, Class<?>[] params,
            Class<?> returnType, int modifiers, int nonmodifiers) {
        if (c == null || name == null)
            return false;

        try {
            Method m;
            if (params == null)
                m = c.getDeclaredMethod(name);
            else
                m = c.getDeclaredMethod(name, params);
            if (!m.getReturnType().equals(returnType))
                return false;

            int mod = m.getModifiers();
            if ((mod & modifiers) != modifiers)
                return false;
            if ((mod | ~nonmodifiers) != ~nonmodifiers)
                return false;

            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static boolean hasConstructor(Class<?> c, Class<?>... params) {
        if (c == null)
            return false;
        try {
            Constructor<?> cons;
            if (params == null)
                cons = c.getDeclaredConstructor();
            else
                cons = c.getDeclaredConstructor(params);
            if (Modifier.isPrivate(cons.getModifiers()))
                return false;
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Check whether the class's type parameters match the reference solution's.
     * To match, type parameters must have the same names and the same bounds.
     * Print errors to the standard output stream.
     * 
     * @param print     the boolean that describes whether to print errors
     * @return          whether the type parameters are equal
     */
    public boolean checkTypeParameters(boolean print) {
        TypeVariable[] tv = c.getTypeParameters();
        TypeVariable[] refTv = refC.getTypeParameters();
        int i;

        boolean diff = (tv.length != refTv.length);

        if (!diff)
            for (i = 0; i < tv.length; i++) {
                if (!tv[i].getName().equals(refTv[i].getName()) || !checkTypesUnordered(tv[i].getBounds(), refTv[i].getBounds())) {
                    diff = true;
                    break;
                }
            }

        if (diff && print) {
            System.out.println("Wrong type paramters: ");
            System.out.println(BULLET + "Expected: " + fixTypeParameters(refTv, true));
            System.out.println(BULLET + "Received: " + fixTypeParameters(tv, false));
            System.out.println();
        }
        return !diff;
    }

    /**
     * Check whether the class's superclass match the reference solution's.
     * Print errors to the standard output stream.
     * 
     * @param print     the boolean that describes whether to print errors
     * @return          whether the superclasses are equal
     */
    public boolean checkSuperclass(boolean print) {
        Class<?> superc = c.getSuperclass();
        Class<?> refSuperc = refC.getSuperclass();
        if (!superc.equals(refSuperc)) {
            if (print) {
                System.out.println("Superclasses do not match up:");
                System.out.println(BULLET + refSuperc +  " expected, " + superc + " found.");
                System.out.println();
            }
            return false;
        }
        return true;
    }

    /**
     * Check whether the class's package match the reference solution's.
     * Print errors to the standard output stream.
     * 
     * @param print     the boolean that describes whether to print errors
     * @return          whether the packages are equal
     */
    public boolean checkPackage(boolean print) {
        Package p = c.getPackage();
        Package refP = refC.getPackage();
        if (p == null)
            if (refP == null)
                return true;
        if (!p.equals(refP)) {
            if (print) {
                System.out.println("Packages do not match up:");
                System.out.println(BULLET + refP + " expected, " + p + " found.");
                System.out.println();
            }
            return false;
        }
        return true;
    }

    /**
     * Check whether the class's modifiers match the reference solution's.
     * Print errors to the standard output stream.
     * 
     * @param print     the boolean that describes whether to print errors
     * @return          whether the modifiers are equal
     */
    public boolean checkModifiers(boolean print) {
        // check modifiers
        String mod = Modifier.toString(c.getModifiers());
        String refMod = Modifier.toString(refC.getModifiers());
        if (!mod.equals(refMod)) {
            if (print) {
                mod = mod.replace(" interface", "");
                refMod = refMod.replace(" interface", "");
                mod = mod.replace("interface", "");
                refMod = refMod.replace("interface", "");
                System.out.println("The class has the wrong modifiers:");
                System.out.println(BULLET + mod + " " + c + " should be " + refMod + " " + refC.toString().replace(" " + refName, " " + name));
                System.out.println();
            }
            return false;
        }
        return true;
    }

    /**
     * Check whether the class's non-private fields match the reference solution's.
     * Print errors to the standard output stream.
     * 
     * @param print     the boolean that describes whether to print errors
     * @return          whether the fields are equal
     */
    public boolean checkFields(boolean print) {
        Field[] f = c.getDeclaredFields();
        Field[] refF = refC.getDeclaredFields();

        TreeSet<String> fieldSet = new TreeSet<String>();
        TreeSet<String> refFieldSet = new TreeSet<String>();
        for (int i = 0; i < f.length; i++) {
            if (!Modifier.isPrivate(f[i].getModifiers()) && !f[i].isSynthetic()) {
                String s = Modifier.toString(f[i].getModifiers());
                if (f[i].getModifiers() != 0)
                    s += " ";
                s += fixType(f[i].getGenericType(), false, false) + " " + f[i].getName();
                fieldSet.add(s);
            }
        }
        for (int i = 0; i < refF.length; i++) {
            if (!Modifier.isPrivate(refF[i].getModifiers()) && !refF[i].isSynthetic()) {
                String s = Modifier.toString(refF[i].getModifiers());
                if (refF[i].getModifiers() != 0)
                    s += " ";
                s += fixType(refF[i].getGenericType(), true, false) + " " + refF[i].getName();
                refFieldSet.add(s);
            }
        }

        boolean same = (fieldSet.size() == refFieldSet.size());

        if (!print && !same)
            return false;

        String m = "The following fields are missing:";
        String e = "The following fields should be made private:";
        TreeSet<String> matches = findDifferences(fieldSet, refFieldSet, m, e, print);
        return (same && (matches.size() == fieldSet.size())); 
    }

    /**
     * Check whether the class's implemented interfaces match the reference solution's.
     * Print errors to the standard output stream.
     * 
     * @param print     the boolean that describes whether to print errors
     * @return          whether the interfaces are equal
     */
    public boolean checkInterfaces(boolean print) {
        Type[] inter = c.getGenericInterfaces();
        Type[] refInter = refC.getGenericInterfaces();

        if (!print && (inter.length != refInter.length))
            return false;

        TreeSet<String> interfaces = new TreeSet<String>();
        TreeSet<String> refInterfaces = new TreeSet<String>();

        for (int i = 0; i < inter.length; i++)
            interfaces.add(fixType(inter[i], false, false));
        for (int i = 0; i < refInter.length; i++)
            refInterfaces.add(fixType(refInter[i], true, false));

        String m = "The following interfaces are missing:";
        String e = "The following interfaces should not be implemented:";
        TreeSet<String> matches = findDifferences(interfaces, refInterfaces, m, e, print);
        return ((matches.size() == inter.length) && (inter.length == refInter.length));
    }

    /**
     * Check whether the class's non-private, declared constructors match the reference solution's.
     * Print errors to the standard output stream.
     * 
     * @param print     the boolean that describes whether to print errors
     * @return          whether the constructors are equal
     */
    public boolean checkConstructors(boolean print) {
        Constructor<?>[] cons = c.getConstructors();
        Constructor<?>[] refCons = refC.getConstructors();

        if (!print && (cons.length != refCons.length))
            return false;

        TreeSet<String> c1 = new TreeSet<String>();
        TreeSet<String> c2 = new TreeSet<String>();
        for (int i = 0; i < cons.length; i++)
            if (!cons[i].isSynthetic())
                c1.add(fixConstructor(cons[i], false));
        for (int i = 0; i < refCons.length; i++)
            if (!refCons[i].isSynthetic())
                c2.add(fixConstructor(refCons[i], true));
        String m = "The following constructors are missing:";
        String e = "The following constructors should be removed:";
        TreeSet<String> matches = findDifferences(c1, c2, m, e, print);
        return ((matches.size() == cons.length) && (cons.length == refCons.length));
    }

    /**
     * Check whether the class's non-private, declared methods match the reference solution's.
     * Print errors to the standard output stream.
     * 
     * @param print     the boolean that describes whether to print errors
     * @return          whether the methods are equal
     */
    public boolean checkMethods(boolean print) {
        Method[] methods = c.getDeclaredMethods();
        Method[] refMethods = refC.getDeclaredMethods();
        TreeSet<String> m1 = getPublicMethods(methods, false);
        TreeSet<String> m2 = getPublicMethods(refMethods, true);

        m1.remove("public T put(Integer,T)");

        if (!print && (m1.size() != m2.size()))
            return false;

        String m = "The following methods are missing:";
        String e = "The following methods should be removed or made private:";
        TreeSet<String> matches = findDifferences(m1, m2, m, e, print);
        return ((matches.size() == m1.size()) && (m1.size() == m2.size()));
    }

    /**
     * Check whether the class's non-private nested classes match the reference solution's.
     * Print errors to the standard output stream.
     * If the names of the nested classes match up, check all of the class' attributes.
     * 
     * @param print     the boolean that describes whether to print errors
     * @return          whether the nested classes are equal
     */
    public boolean checkDeclaredClasses(boolean print) {
        Class<?>[] classes = c.getDeclaredClasses();
        Class<?>[] refClasses = refC.getDeclaredClasses();

        if (classes.length == 0)
            if (refClasses.length == 0)
                return true;

        TreeSet<String> classNames = new TreeSet<String>();
        TreeSet<String> refClassNames = new TreeSet<String>();
        TreeMap<String, Class<?>> nameToClass = new TreeMap<String, Class<?>>();
        TreeMap<String, Class<?>> refNameToClass = new TreeMap<String, Class<?>>();
        for (int i = 0; i < classes.length; i++) {
            if (!Modifier.isPrivate(classes[i].getModifiers()) && !classes[i].isSynthetic()) {
                String n = classes[i].getName().replace(name + "$", "");
                if (!Character.isDigit(n.charAt(0))) {
                    classNames.add(n);
                    nameToClass.put(n, classes[i]);
                }
            }
        }
        for (int i = 0; i < refClasses.length; i++) {
            if (!Modifier.isPrivate(refClasses[i].getModifiers()) && !refClasses[i].isSynthetic()) {
                String n = refClasses[i].getName().replace(refName + "$", "");
                if (!Character.isDigit(n.charAt(0))) {
                    refClassNames.add(n);
                    refNameToClass.put(n, refClasses[i]);
                }
            }
        }

        if (!print && (classNames.size() != refClassNames.size()))
            return false;

        String m = "The following nested classes are missing:";
        String e = "The following nested classes should be made private:";
        TreeSet<String> matches = findDifferences(classNames, refClassNames, m, e, print);

        if (!print && ((matches.size() != classNames.size()) || (matches.size() != refClassNames.size())))
            return false;

        boolean same = true;

        // recursively check each nested class, if the class names match up
        for (String s : matches) {
            Class<?> c = nameToClass.get(s);
            Class<?> refC = refNameToClass.get(s);
            APIChecker checker = new APIChecker(c, refC, name + "$" + s, refName + "$" + s);
            checker.nameToIndex = new TreeMap<String, Integer>(nameToIndex);
            checker.indexToName = new TreeMap<Integer, String>(indexToName);
            int length = nameToIndex.size();
            checker.nameToIndex.put(refName + "$" + s, length);
            checker.indexToName.put(length, name + "$" + s);
            same = same && checkAll(checker, print);
            if (!print && !same)
                return false;
        }
        return same;
    }

    private boolean checkTypesUnordered(Type[] types1, Type[] types2) {
        if (types1.length != types2.length)
            return false;

        boolean[] marked = new boolean[types1.length];
        int i, j;

        for (i = 0; i < types1.length; i++) {
            for (j = 0; j < types2.length; j++) {
                if (!marked[j] && fixType(types1[j], false, true).equals(fixType(types2[i], true, true))) {
                    marked[j] = true;
                    break;
                }
            }
            if (j == types2.length)
                return false;
        }
        return true;
    }

    private String getName(String s) {
        if (nameToIndex.containsKey(s))
            return indexToName.get(nameToIndex.get(s));
        return null;
    }

    private String fixTypeParameters(TypeVariable[] t, boolean ref) {
        String s;
        if (t.length == 0)
            return "(none)";
        else
            s = "<";

        for (int i = 0; i < t.length; i++) {
            s += fixType(t[i], ref, true);
            if (i < t.length - 1)
                s += ",";
        }
        s += ">";
        return s;
    }

    private String fixName(String s, boolean ref) {
        s = s.replace("class ", "");
        s = s.replace("interface ", "");
        s = s.replace("java.lang.", "");
        int len = s.length();
        if (ref) {
            if (getName(s) != null)
                s = getName(s);
            else if (len > 5 && s.substring(0, 5).equals("AGAPI")) {
                s = s.substring(5, len);
            }
            else if (s.contains("$")) {
                int i = s.indexOf("$");
                String front = s.substring(0, i);
                String back = s.substring(i);
                s = fixName(front, ref) + back;
            }
        }
        return s;
    }

    /*
     * return a string representation of Type t. ref tells whether the type
     * comes from the reference solution or the student's class. includeBounds
     * tells whether the string should include the bounds of type variables.
     */
    private String fixType(Type t, boolean ref, boolean includeBounds) {
        String name = t.toString();
        name = fixName(name, ref);

        if (name.charAt(0) == '[') {
            int i = 1;
            while (name.charAt(i) == '[')
                i++;
            char c = name.charAt(i);
            String s;
            switch (c) {
                case 'Z': s = "boolean"; break;
                case 'B': s = "byte";    break;
                case 'C': s = "char";    break;
                case 'D': s = "double";  break;
                case 'F': s = "float";   break;
                case 'I': s = "int";     break;
                case 'J': s = "long";    break;
                case 'S': s = "short";   break;
                case 'L': s = name.substring(i + 1, name.length() - 1); break;
                default: s = "";         break;
            }
            s = fixName(s, ref);
            for (int j = 0; j < i; j++)
                s += "[]";
            return s;
        }

        if (t instanceof ParameterizedType) {
            String temp;
            ParameterizedType type = (ParameterizedType) t;
            Type front = type.getRawType();
            Type[] args = type.getActualTypeArguments();
            String s = fixType(front, ref, includeBounds);
            if (args.length > 0) {
                s += "<";
                int i;
                for (i = 0; i < args.length - 1; i++) {
                    temp = fixType(args[i], ref, false);
                    s += temp + ",";
                }
                temp = fixType(args[i], ref, false);
                s += temp + ">";
            }
            return s;
        }

        if (t instanceof TypeVariable) {
            TypeVariable type = (TypeVariable) t;
            String s = type.getName();
            Type[] bounds = type.getBounds();
            if (includeBounds)
                if (bounds.length != 0)
                    if (!(bounds.length == 1 && bounds[0].toString().replace("class ", "").equals("java.lang.Object"))) {
                        s += " extends ";
                        for (int i = 0; i < bounds.length; i++) {
                            s += fixType(bounds[i], ref, includeBounds);
                            if (i < bounds.length - 1)
                                s += ", ";
                        }
                    }
            return s;
        }

        if (t instanceof GenericArrayType) {
            Type type = ((GenericArrayType) t).getGenericComponentType();
            String sType = fixType(type, ref, includeBounds);
            return sType + "[]";
        }

        if (t instanceof WildcardType) {
            String s = "?";

            WildcardType type = (WildcardType) t;
            Type[] upperBounds = type.getUpperBounds();
            Type[] lowerBounds = type.getLowerBounds();

            if (lowerBounds.length == 0) {
                if (!upperBounds[0].toString().replace("class ", "").equals("java.lang.Object"))
                    s += " extends " + fixType(upperBounds[0], ref, includeBounds);
            }
            else
                s += " super " + fixType(lowerBounds[0], ref, includeBounds);

            return s;
        }

        return name;
    }

    /*
     * return a string representation of cons
     */
    private String fixConstructor(Constructor cons, boolean ref) {
        Type[] parameterTypes = cons.getGenericParameterTypes();
        TypeVariable[] tv = cons.getTypeParameters();
        String s = Modifier.toString(cons.getModifiers());
        if (cons.getModifiers() != 0)
            s += " ";

        if (tv.length != 0)
            s += fixTypeParameters(tv, ref) + " ";

        s += name + "(";

        for (int i = 0; i < parameterTypes.length; i++) {
            s += fixType(parameterTypes[i], ref, false);
            if (i < parameterTypes.length - 1)
                s += ",";
        }
        s += ")";
        return s;
    }

    /*
     * return a string representation of Method m
     */
    private String fixMethod(Method m, boolean ref) {
        Type[] parameterTypes = m.getGenericParameterTypes();
        TypeVariable[] tv = m.getTypeParameters();
        String s = Modifier.toString(m.getModifiers());
        if (m.getModifiers() != 0)
            s += " ";

        if (tv.length != 0)
            s += fixTypeParameters(tv, ref) + " ";

        s += fixType(m.getGenericReturnType(), ref, false) + " ";
        s += m.getName() + "(";

        for (int i = 0; i < parameterTypes.length; i++) {
            s += fixType(parameterTypes[i], ref, false);
            if (i < parameterTypes.length - 1)
                s += ",";
        }
        s += ")";
        return s;
    }

    private TreeSet<String> getPublicMethods(Method[] m, boolean ref) {
        int i;
        int count = 0;
        TreeSet<String> publicMethods = new TreeSet<String>();

        for (i = 0; i < m.length; i++) {
            if (!Modifier.isPrivate(m[i].getModifiers()) && !m[i].isSynthetic()) {
                count++;
                publicMethods.add(fixMethod(m[i], ref));
            }
        }

        return publicMethods;
    }

    /* 
     * print out the differences between the sets and return the set of matching items
     * m is printed before the missing items, and e before the extra ones
     */
    private TreeSet<String> findDifferences(TreeSet<String> items, TreeSet<String> refItems, String m, String e, boolean print) {
        TreeSet<String> extra = new TreeSet<String>();
        TreeSet<String> missing = new TreeSet<String>();
        TreeSet<String> intersection = new TreeSet<String>();
        extra.addAll(items);
        missing.addAll(refItems);
        intersection.addAll(items);

        extra.removeAll(refItems);
        missing.removeAll(items);
        intersection.retainAll(refItems);

        if (print) {
            if (!missing.isEmpty()) {
                System.out.println(m);
                for (String s : missing)
                    System.out.println(BULLET + s);
                System.out.println();
            }
            if (!extra.isEmpty()) {
                boolean b = false;
                for (String s : extra) {
                    
                    /*if (!s.equals("public static void main(String[])")
                            && !s.equals("public " + name + "()")) {*/
                        if (!b) {
                            System.out.println(e);
                            b = true;
                        }
                        System.out.println(BULLET + s);
                    
                }
                if (b)
                    System.out.println();
            }
        }

        return intersection;
    }

    /**
     * Check all attributes of the class stored in checker.
     * Print errors to the standard output stream.
     * 
     * @param checker   the checker with the class to be checked
     * @param print     the boolean that describes whether to print errors
     * @return          whether the APIs are equal
     */
    public static boolean checkAll(APIChecker checker, boolean print) {
        if (print) {
            System.out.println("Testing " + checker.name + ".java");
        }
        
        boolean same;

        same = checker.checkTypeParameters(print);
        same = checker.checkSuperclass(print) && same;
        same = checker.checkPackage(print) && same;
        same = checker.checkModifiers(print) && same;
        same = checker.checkFields(print) && same;
        same = checker.checkInterfaces(print) && same;
        same = checker.checkConstructors(print) && same;

        same = checker.checkMethods(print) && same;
        same = checker.checkDeclaredClasses(print) && same;
        
        return same;
    }

    /** */
    private static String createAPITestName(String classname) {
        int lastDotDex = classname.lastIndexOf('.');
        if (lastDotDex == -1) {
            return "AGAPI" + classname;
        }
        String packageNames = classname.substring(0, lastDotDex);
        String basename = classname.substring(lastDotDex + 1, classname.length());
        String newName = packageNames + ".AGAPI" + basename;
        return newName;
    }

    /**
     * For each class whose name is stored in args, check whether it has the same API
     * as the class whose name is the first class' name prefixed with "AGAPI".
     * Print errors to the standard output stream.
     */
    public static void main(String[] args) {
        boolean allPassed = true;
        for (int i = 0; i < args.length; i++) {
            String name = args[i];
            boolean b = false;
            try {
                URL runningDirURL = new URL("file://" + System.getProperty("user.dir"));
                URLClassLoader runningDirClassLoader = new URLClassLoader(
                        new URL[]{runningDirURL});

                Class<?> c = Class.forName(name, true, runningDirClassLoader);
                b = true;

                String apiTestName = createAPITestName(name);

                Class<?> refC = Class.forName(apiTestName, true, runningDirClassLoader);

                APIChecker checker = new APIChecker(c, refC, name, apiTestName);
                if (!checkAll(checker, true)) {

                    allPassed = false;
                }
            }
            catch (MalformedURLException|ClassNotFoundException|NoClassDefFoundError x) {
                System.out.println(x);
                System.out.print(name + ": ");
                if (b)
                    System.out.print("AGAPI");
                System.out.print(args[i]);
                System.out.println(".class could not be found.");
            }
        }
        if (allPassed == true) {
            System.out.println("All API checks passed.");
        } else {
            System.out.println("One or more API checks failed.");
        }
    }
}
