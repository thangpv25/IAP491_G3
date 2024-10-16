import boomerang.*;
import boomerang.results.BackwardBoomerangResults;
import boomerang.scene.ControlFlowGraph;
import boomerang.scene.DataFlowScope;
import boomerang.scene.SootDataFlowScope;
import boomerang.scene.Statement;
import boomerang.scene.jimple.JimpleMethod;
import boomerang.util.AccessPath;
import soot.*;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;

import java.util.*;

public class TaintAnalysis {

    private Set<SootMethod> taintSources = new HashSet<>();
    private Set<SootMethod> taintSinks = new HashSet<>();
    private Set<SootMethod> sanitizationMethods = new HashSet<>();
    private ControlFlow controlFlowManager;

    public TaintAnalysis() {
        controlFlowManager = ControlFlow.getInstance();
        initializeSourcesAndSinks();
    }

    private void initializeSourcesAndSinks() {
        // Sources
        taintSources.add(Scene.v().getMethod("<org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping: void registerMapping(org.springframework.web.servlet.mvc.method.RequestMappingInfo, java.lang.Object, java.lang.reflect.Method)>"));
        taintSources.add(Scene.v().getMethod("<org.springframework.web.servlet.HandlerMapping: void registerHandler(java.lang.String, java.lang.Object)>"));
        taintSources.add(Scene.v().getMethod("<java.lang.reflect.Field: java.lang.Object get(java.lang.Object)>"));
        taintSources.add(Scene.v().getMethod("<org.springframework.context.ApplicationEventPublisher: void addApplicationEventListener(org.springframework.context.ApplicationListener)>"));
        taintSources.add(Scene.v().getMethod("<javax.servlet.ServletContext: void addServletMappingDecoded(java.lang.String, java.lang.String)>"));

        // Sinks
        taintSinks.add(Scene.v().getMethod("<java.lang.ProcessBuilder: java.lang.Process start()>"));
        taintSinks.add(Scene.v().getMethod("<java.lang.Runtime: java.lang.Process exec(java.lang.String)>"));
        taintSinks.add(Scene.v().getMethod("<java.lang.Runtime: java.lang.Process exec(java.lang.String[])>"));
        taintSinks.add(Scene.v().getMethod("<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object, java.lang.Object[])>"));

        // Sanitization
        sanitizationMethods.add(Scene.v().getMethod("<org.apache.commons.lang3.StringEscapeUtils: java.lang.String escapeHtml4(java.lang.String)>"));
    }

    public void analyze(SootClass sootClass) {
        for (SootMethod method : sootClass.getMethods()) {
            if (method.isConcrete()) {
                analyzeMethod(method);
            }
        }
    }

    private void analyzeMethod(SootMethod method) {
        ExceptionalUnitGraph cfg = new ExceptionalUnitGraph(method.retrieveActiveBody());
        TaintFlowAnalysis taintFlowAnalysis = new TaintFlowAnalysis(cfg);
        taintFlowAnalysis.doAnalysis();
    }

    private class TaintFlowAnalysis extends BackwardFlowAnalysis<Unit, Set<AccessPath>> {

        private Set<AccessPath> taintedVars = new HashSet<>();
        private Map<Stmt, Set<AccessPath>> taintedResults = new HashMap<>();

        public TaintFlowAnalysis(ExceptionalUnitGraph graph) {
            super(graph);
        }

        @Override
        protected void flowThrough(Set<AccessPath> in, Unit unit, Set<AccessPath> out) {
            Stmt stmt = (Stmt) unit;
            Set<AccessPath> newOut = new HashSet<>(in);

            if (stmt.containsInvokeExpr()) {
                SootMethod invokedMethod = stmt.getInvokeExpr().getMethod();

                if (taintSources.contains(invokedMethod)) {
                    Set<AccessPath> taintedData = controlFlowManager.getAliases(stmt, invokedMethod, stmt.getInvokeExpr().getArg(0));
                    newOut.addAll(taintedData);
                } else if (taintSinks.contains(invokedMethod)) {
                    // Report taint at sink
                    reportTaintedData(stmt, invokedMethod, newOut);
                } else if (sanitizationMethods.contains(invokedMethod)) {
                    newOut.clear(); // Assume sanitization clears taint
                }
            } else if (stmt instanceof JAssignStmt) {
                JAssignStmt assignStmt = (JAssignStmt) stmt;
                Value leftOp = assignStmt.getLeftOp();
                Value rightOp = assignStmt.getRightOp();

                Set<AccessPath> aliases = controlFlowManager.getAliases(assignStmt, stmt.getMethod(), rightOp);
                newOut.addAll(aliases);

                if (newOut.contains(new AccessPath(leftOp))) {
                    taintedVars.add(new AccessPath(leftOp));
                }
            }

            out.addAll(newOut);
        }

        @Override
        protected Set<AccessPath> newInitialFlow() {
            return new HashSet<>();
        }

        @Override
        protected Set<AccessPath> entryInitialFlow() {
            return new HashSet<>(taintedVars);
        }

        @Override
        protected void merge(Set<AccessPath> in1, Set<AccessPath> in2, Set<AccessPath> out) {
            out.addAll(in1);
            out.addAll(in2);
        }

        @Override
        protected void copy(Set<AccessPath> source, Set<AccessPath> dest) {
            dest.clear();
            dest.addAll(source);
        }

        private void reportTaintedData(Stmt stmt, SootMethod sink, Set<AccessPath> taintedVars) {
            System.out.println("Potential malicious data detected:");
            System.out.println("Class Path: " + sink.getDeclaringClass().getJavaStyleName());
            System.out.println("Class Name: " + sink.getDeclaringClass().getName());
            System.out.println("Method: " + sink.getSignature());
            System.out.println("Tainted Data: " + taintedVars);
        }

    }

    public static void main(String[] args) {
        String jimpleFile = "test.jimple";
        SootClass sootClass = Scene.v().loadClassAndSupport(jimpleFile);
        Scene.v().loadNecessaryClasses();
        TaintAnalysis ta = new TaintAnalysis();
        ta.analyze(sootClass);
    }
}
