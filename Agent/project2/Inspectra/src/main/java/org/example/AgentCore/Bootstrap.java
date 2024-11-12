package org.example.AgentCore;

import org.example.Loader.AgentCache;
import org.example.Loader.CustomClassLoader;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;


public class Bootstrap {

	public static ClassLoader classLoader;
	Bootstrap() {
	}
	public static void back(Instrumentation inst, CustomClassLoader loader, AgentCache agentCache) {
		classLoader = loader;
		System.out.println("Worker classloader is " + Bootstrap.class.getClassLoader());
		ClassFileTransformer memoryTransformer = new MemoryTransformer(agentCache);
		agentCache.getTransformers().add(memoryTransformer);

		inst.addTransformer(memoryTransformer, true);
	}
	public static void print(String str){
		System.out.println("Bootstrap context loader: " + Thread.currentThread().getContextClassLoader());
		System.out.println("HELLO WORD! THIS IS CLASS WORKER. Hello: "+str);

	}
	public static void test(Instrumentation inst,  AgentCache agentCache) {
		System.out.println("Method test executed!");
//         classLoader = loader;
		System.out.println("Worker classloader is " + Bootstrap.class.getClassLoader());
//		ClassFileTransformer memoryTransformer = new MemoryTransformer(agentCache);
//		agentCache.getTransformers().add(memoryTransformer);
//
//		inst.addTransformer(memoryTransformer, true);
	}


}
