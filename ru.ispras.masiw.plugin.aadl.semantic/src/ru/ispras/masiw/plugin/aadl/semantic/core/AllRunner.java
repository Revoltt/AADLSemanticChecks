package ru.ispras.masiw.plugin.aadl.semantic.core;

import ru.ispras.antlr.v4.editing.core.runtime.CommonProblem;
import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.ProblemHelper;
import ru.ispras.masiw.plugin.aadl.model.domain.AADLSpecificationDomain;

public class AllRunner {
	public static ProblemHelper helper;

	//function for raising problems
	public static void raiseCommonProblem(String id, IExtendedEObject x) {
		try {
			CommonProblem problem = helper.createProblem(id);
			x.getProblems().add(problem);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	// function to check if category type and referenced component type of alias_declaration match each other
	public static boolean typeMatch(String s1, String s2) { // was categoryType classifierType
		if (s1.endsWith("Impl")) {
			s1 = s1.substring(0, s1.length() - "Impl".length());
		}
		
		if (s2.endsWith("Impl")) {
			s2 = s2.substring(0, s2.length() - "Impl".length());
		}
		
		if (s1.endsWith("Implementation")) {
			s1 = s1.substring(0, s1.length() - "Implementation".length());
		}
		
		if (s2.endsWith("Implementation")) {
			s2 = s2.substring(0, s2.length() - "Implementation".length());
		}
		
		if (s1.endsWith("Type")) {
			s1 = s1.substring(0, s1.length() - "Type".length());
		}
		
		if (s2.endsWith("Type")) {
			s2 = s2.substring(0, s2.length() - "Type".length());
		}
	
		return s1.equals(s2);
		
	}

	public static void check(ProblemHelper h, AADLSpecificationDomain model) {
		helper = h;
		NamespaceModel.createModel(model);
	}
}
