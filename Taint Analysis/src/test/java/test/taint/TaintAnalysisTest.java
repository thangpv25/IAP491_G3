package test.taint;

import analysis.IFDSTaintAnalysisProblem;
import analysis.data.DFF;
import heros.InterproceduralCFG;
import org.junit.Test;
import soot.*;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import test.base.IFDSTestSetUp;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class TaintAnalysisTest extends IFDSTestSetUp {

    @Override
    protected Transformer createAnalysisTransformer() {
        List<SootMethodRef> sources = new ArrayList<>();
        List<SootMethodRef> sinks = new ArrayList<>();
        SootClass sourceClass = new SootClass("target.taint.internal.SourceClass");
        SootMethodRef source1 = new SootMethodRefImpl(sourceClass, "anInstanceSource", Collections.emptyList(), RefType.v("java.lang.String"), false);
        SootMethodRef source2 = new SootMethodRefImpl(sourceClass, "aStaticSource", Collections.emptyList(), RefType.v("java.lang.String"), true);

        SootClass sinkClass = new SootClass("target.taint.internal.SinkClass");
        SootMethodRef sink1 = new SootMethodRefImpl(sinkClass, "anInstanceSink", Collections.emptyList(), RefType.v("java.lang.String"), false);
        SootMethodRef sink2 = new SootMethodRefImpl(sinkClass, "aStaticSink", Collections.emptyList(), RefType.v("java.lang.String"), true);

        SootClass handlerMethodMappingClass = Scene.v().getSootClass("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping");
        SootMethodRef registerMapping = new SootMethodRefImpl(handlerMethodMappingClass, "registerMapping", Collections.emptyList(), VoidType.v(), false);
        sources.add(registerMapping);

        SootClass servletRequestClass = Scene.v().getSootClass("javax.servlet.http.HttpServletRequest");
        SootMethodRef getParameter = new SootMethodRefImpl(servletRequestClass, "getParameter", Collections.singletonList(RefType.v("java.lang.String")), RefType.v("java.lang.String"), false);
        sources.add(getParameter);

        // AbstractUrlHandlerMapping: registerHandler
        SootClass urlHandlerMappingClass = Scene.v().getSootClass("org.springframework.web.servlet.handler.AbstractUrlHandlerMapping");
        SootMethodRef registerHandler = new SootMethodRefImpl(urlHandlerMappingClass, "registerHandler", Collections.emptyList(), VoidType.v(), false);
        sources.add(registerHandler);

        // Field: get
        SootClass fieldClass = Scene.v().getSootClass("java.lang.reflect.Field");
        SootMethodRef fieldGet = new SootMethodRefImpl(fieldClass, "get", Collections.singletonList(RefType.v("java.lang.Object")), RefType.v("java.lang.Object"), false);
        sources.add(fieldGet);

        // FilterDef: setFilterName
        SootClass filterDefClass = Scene.v().getSootClass("org.apache.catalina.deploy.FilterDef");
        SootMethodRef setFilterName = new SootMethodRefImpl(filterDefClass, "setFilterName", Collections.singletonList(RefType.v("java.lang.String")), VoidType.v(), false);
        sources.add(setFilterName);

        // StandardContext: addApplicationEventListener, addServletMappingDecoded
        SootClass standardContextClass = Scene.v().getSootClass("org.apache.catalina.core.StandardContext");
        SootMethodRef addApplicationEventListener = new SootMethodRefImpl(standardContextClass, "addApplicationEventListener", Arrays.asList(RefType.v("java.util.EventListener")), VoidType.v(), false);
        SootMethodRef addServletMappingDecoded = new SootMethodRefImpl(standardContextClass, "addServletMappingDecoded", Arrays.asList(RefType.v("java.lang.String"), RefType.v("java.lang.String")), VoidType.v(), false);
        sources.add(addApplicationEventListener);
        sources.add(addServletMappingDecoded);

        //sources.add(source1);
        //sources.add(source2);
        sinks.add(sink1);
        sinks.add(sink2);

        SootClass processBuilderClass = Scene.v().getSootClass("java.lang.ProcessBuilder");
        SootMethodRef processBuilderStart = new SootMethodRefImpl(processBuilderClass, "start", Collections.emptyList(), RefType.v("java.lang.Process"), true);
        sinks.add(processBuilderStart);

        SootClass runtimeClass = Scene.v().getSootClass("java.lang.Runtime");
        SootMethodRef runtimeExecString = new SootMethodRefImpl(runtimeClass, "exec", Collections.singletonList(RefType.v("java.lang.String")), RefType.v("java.lang.Process"), true);
        sinks.add(runtimeExecString);
        SootMethodRef runtimeExecStringArray = new SootMethodRefImpl(runtimeClass, "exec", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)), RefType.v("java.lang.Process"), true);
        sinks.add(runtimeExecStringArray);

        SootClass methodClass = Scene.v().getSootClass("java.lang.reflect.Method");
        SootMethodRef methodInvoke = new SootMethodRefImpl(methodClass, "invoke", Arrays.asList(RefType.v("java.lang.Object"), ArrayType.v(RefType.v("java.lang.Object"), 1)), RefType.v("java.lang.Object"), true);
        sinks.add(methodInvoke);

        return new SceneTransformer() {
            @Override
            protected void internalTransform(String phaseName, Map<String, String> options) {
                JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(false);
                IFDSTaintAnalysisProblem problem = new IFDSTaintAnalysisProblem(icfg, sources, sinks);
                @SuppressWarnings({"rawtypes", "unchecked"})
                JimpleIFDSSolver<?, ?> solver = new JimpleIFDSSolver<>(problem);
                solver.solve();
                IFDSTestSetUp.solver = solver;
            }
        };
    }


    private Set<String> getResult(Object analysis) {
        List<SootMethod> entryPoints = getEntryPointMethods(); // Get all entry points
        Set<String> result = new HashSet<>();

        for (SootMethod m : entryPoints) {
            // Skip methods that don't have an active body or are empty
            if (m == null || m.getActiveBody() == null || m.getActiveBody().getUnits().isEmpty()) {
                continue;
            }

            Map<DFF, Integer> res = null;
            if (analysis instanceof JimpleIFDSSolver) {
                JimpleIFDSSolver solver = (JimpleIFDSSolver) analysis;
                res = (Map<DFF, Integer>) solver.resultsAt(m.getActiveBody().getUnits().getLast());
            }

            // Add results from this method to the main result set
            if (res != null) {
                for (Map.Entry<DFF, Integer> e : res.entrySet()) {
                    result.add(e.getKey().toString());
                }
            }
        }

        // Print whether the set is empty or contains data
        if (result.isEmpty()) {
            System.out.println("Class is not malicious");
        } else {
            System.out.println("Class is malicious");
        }
        System.out.print(result + "\n");
        return result;
    }

    /*
    private void checkResults(Set<String> defaultIDEResult, Set<String> expected) {
        // first remove intermediate vars
        Supplier<Predicate<String>> pred = () -> p -> !(p.startsWith("$stack") || p.startsWith("varReplacer"));
        defaultIDEResult = defaultIDEResult.stream().filter(pred.get()).collect(Collectors.toSet());
        assertTrue(defaultIDEResult.size() == expected.size());
        assertTrue(msg(defaultIDEResult, expected), defaultIDEResult.containsAll(expected));
    }


    private String msg(Set<String> actual, Set<String> expected) {
        StringBuilder str = new StringBuilder(System.lineSeparator());
        str.append("actual:").append(System.lineSeparator());
        str.append(actual.stream().collect(Collectors.joining("-"))).append(System.lineSeparator());
        str.append("expected:").append(System.lineSeparator());
        str.append(expected.stream().collect(Collectors.joining("-"))).append(System.lineSeparator());
        return str.toString();
    }
     */

    @Test
    public void Evil() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.Evil.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void Hello() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.HelloWorld.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void InvisibleShell() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.InvisibleShell.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void InjectToController() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.InjectToController.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void FRain() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.FRain.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    //@Test
    //public void Field() {
    //    JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.IFRain.class.getName());
    //    Set<String> defaultIDEResult = getResult(analysis);
    //}

    @Test
    public void AddFilter() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.AddFilter.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void TestInterceptor() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.TestInterceptor.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void SRain() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.SRain.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void AddServlet() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.AddServlet.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void LRain() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.LRain.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void AddListener() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.AddListener.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void FilterBasedWithoutRequestVariant() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.FilterBasedWithoutRequestVariant.FilterBasedWithoutRequestVariant.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    //@Test
    //public void Branching5() {
    //    JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.Branching5.class.getName());
    //    Set<String> defaultIDEResult = getResult(analysis);
    //}


    @Test
    public void Loop() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.Loop.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void BasicIOExample() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.BasicIOExample.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void filterMem() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.FilterSetupServlet.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void AddTomcatListener() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.AddTomcatListener.AddTomcatListener.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void AddController() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.AddController.AddController.class.getName());
       Set<String> defaultIDEResult = getResult(analysis);
    }

    @Test
    public void AddInterceptor() {
        JimpleIFDSSolver<?, ? extends InterproceduralCFG<Unit, SootMethod>> analysis = executeStaticAnalysis(target.taint.AddInterceptor.AddInterceptor.class.getName());
        Set<String> defaultIDEResult = getResult(analysis);
    }
}
