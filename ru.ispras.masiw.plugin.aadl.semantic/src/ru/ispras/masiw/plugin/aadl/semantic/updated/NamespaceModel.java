package ru.ispras.masiw.plugin.aadl.semantic.updated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;

import ru.ispras.antlr.v4.editing.core.runtime.CommonProblem;
import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.ProblemHelper;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.impl.PropertySetImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.model.domain.AADLSpecificationDomain;

public class NamespaceModel {
	public static ProblemHelper helper;
	
	public static Map<AADLIdentifier, PackageNamespace> globalPackageNamespace;
	public static Map<AADLIdentifier, PackageNamespace> globalPrSetNamespace;
	
	
	//function for raising problems
	private static void raiseCommonProblem(String id, IExtendedEObject x) {
		try {
			CommonProblem problem = helper.createProblem(id);
			x.getProblems().add(problem);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// main function for creating namespace model
	public static void createModel(ProblemHelper h, AADLSpecificationDomain model) {
		globalPackageNamespace = new HashMap<AADLIdentifier, PackageNamespace>();
		globalPrSetNamespace = new HashMap<AADLIdentifier, PackageNamespace>();
		
		TreeIterator<EObject> x = model.getAADLPackagesResource().getAllContents();
		ArrayList<EObject> l = new ArrayList<EObject>();
		helper = h;
		while (x.hasNext()) {
			l.add(x.next()); // list of packages
		}
		
		// create global namespace of packages and property sets
		for (int i = 0; i < l.size(); i++) {
			if ((l.get(i)).getClass().equals(PackageImpl.class)) {
				PackageImpl curPackage = (PackageImpl) l.get(i);
				
				if (globalPackageNamespace.containsKey(curPackage.getIdentifier()) || globalPrSetNamespace.containsKey(curPackage.getIdentifier())) {
					// TODO duplicate package name
					raiseCommonProblem("P41N1", curPackage);
				} else {
					globalPackageNamespace.put(curPackage.getIdentifier(), new PackageNamespace(curPackage));
				}
			}
//			} else if ((l.get(i)).getClass().equals(PropertySetImpl.class)) {
//				PropertySetImpl curPrSet = (PropertySetImpl) l.get(i);
//				
//				if (globalPackageNamespace.containsKey(curPrSet.getIdentifier()) || globalPrSetNamespace.containsKey(curPrSet.getIdentifier())) {
//					// duplicate property set name
//					raiseCommonProblem("P41N1", curPrSet);
//				} else {
//					// TODO property set
//					//globalPrSetNamespace.put(curPrSet.getIdentifier(), new PackageNamespace(curPrSet));
//				}
//			}
		}
	}
	
	public static void checkImports() {
		
	}
	
	
}