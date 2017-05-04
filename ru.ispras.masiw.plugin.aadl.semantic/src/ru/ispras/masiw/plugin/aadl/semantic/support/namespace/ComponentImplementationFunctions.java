package ru.ispras.masiw.plugin.aadl.semantic.support.namespace;

import java.util.HashSet;
import java.util.Set;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.metamodel.ComponentClassifier;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentImplementationImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.semantic.core.AllRunner;
import ru.ispras.masiw.plugin.aadl.semantic.core.NamespaceModel;
import ru.ispras.masiw.plugin.aadl.semantic.support.Usefull;

public class ComponentImplementationFunctions implements Usefull{

	public static void namespaceIntersection(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		PackageImpl curPackage = (PackageImpl) p;
		ComponentImplementationImpl component = (ComponentImplementationImpl) c;
		
		Set<AADLIdentifier> intersection = new HashSet<AADLIdentifier>(NamespaceModel.localClassifierNamespacesWithBindings
				.get(curPackage.getIdentifier()).get(component.getIdentifier()).keySet());
		
		if (component.getComponentType().eIsProxy()) {
			AllRunner.raiseCommonProblem("P44N1", component);
		} else {
			intersection.retainAll(NamespaceModel.localClassifierNamespacesWithBindings
					.get(curPackage.getIdentifier()).get(component.getComponentType().getIdentifier()).keySet());
			if (!intersection.isEmpty()) {
				//iterate over all identifiers in the intersection
				for (AADLIdentifier obj: intersection) {
					AllRunner.raiseCommonProblem("P44N4", NamespaceModel.localClassifierNamespacesWithBindings
							.get(curPackage.getIdentifier()).get(component.getIdentifier()).get(obj));
				}
				//AllRunner.raiseCommonProblem("P44N4", component);
			}
			
			// check if implementation type matches with classifier type
			if (!AllRunner.typeMatch(component.getClass().getSimpleName(), 
					component.getComponentType().getClass().getSimpleName())) {
				AllRunner.raiseCommonProblem("P44L3", component);
			}
		}
	}
	
	public static void allNamespaceIntersection(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		PackageImpl curPackage = (PackageImpl) p;
		ComponentClassifier component = (ComponentClassifier) c;
		
		Set<AADLIdentifier> componentNames = new HashSet<AADLIdentifier>(NamespaceModel.localClassifierNamespacesWithBindings
				.get(curPackage.getIdentifier()).get(component.getIdentifier()).keySet());
		
		// cycle over implementation extensions
		while (component.getAncestor() != null) {
			HashSet<AADLIdentifier> intersection = new HashSet<AADLIdentifier>(componentNames);
			intersection.retainAll(NamespaceModel.localClassifierNamespacesWithBindings
					.get(curPackage.getIdentifier()).get(component.getAncestor()
							.getPrototypeOrClassifier().getIdentifier()).keySet());
			
			// TODO cycle over classifier type extensions
			
			if (!intersection.isEmpty()) {
				//iterate over all identifiers in the intersection
				for (AADLIdentifier obj: intersection) {
					AllRunner.raiseCommonProblem("P44N4", NamespaceModel.localClassifierNamespacesWithBindings
							.get(curPackage.getIdentifier()).get(component.getIdentifier()).get(obj));
				}
			} else {
				component = (ComponentClassifier) component.getAncestor().getPrototypeOrClassifier();
			}
		}
		
	}
	
	public static void extensionTypeMatch(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		//PackageImpl curPackage = (PackageImpl) p;
		ComponentImplementationImpl component = (ComponentImplementationImpl) c;
		
		if (component.getAncestor() != null) {
			if (!AllRunner.typeMatch(component.getClass().getSimpleName(), 
					component.getAncestor().getPrototypeOrClassifier().getClass().getSimpleName()) &&
					//!component.getClass().getSimpleName().contains("Abstract") &&
					!component.getAncestor().getPrototypeOrClassifier().getClass().getSimpleName().contains("Abstract")) {
				AllRunner.raiseCommonProblem("P44L4", component);
			}
		}
	}
	
	private static boolean typeHasRequireModes(ComponentImplementationImpl component) {
		if (component.getComponentType().getModes().size() > 0) {
			for (int i = 0; i < component.getComponentType().getModes().size(); i++) {
				if (component.getComponentType().getModes().get(i).isRequires())
					return true;
			}
		}
		return false;
	}
	
	private static boolean typeHasOtherModes(ComponentImplementationImpl component) {
		if (component.getComponentType().getModes().size() > 0) {
			for (int i = 0; i < component.getComponentType().getModes().size(); i++) {
				if (!component.getComponentType().getModes().get(i).isRequires())
					return true;
			}
		}
		return false;
	}
	
	public static void requireModesCheck(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		//PackageImpl curPackage = (PackageImpl) p;
		ComponentImplementationImpl component = (ComponentImplementationImpl) c;
		
		if (component.getComponentType().getModes().size() > 0)
			if ((component.getModes().size() > 0) && typeHasRequireModes(component)) {
				// if component type has require modes, component implementation must not have modes
				AllRunner.raiseCommonProblem("P44L6", component.getModes().get(0));
			} else if ((component.getModes().size() > 0) && typeHasOtherModes(component)) {
				// if component type has modes, component implementation must not have modes
				AllRunner.raiseCommonProblem("P44L7", component.getModes().get(0));
			}
	}
	
	
	@Override
	public void doWork(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		// TODO Auto-generated method stub
		
	}

}
