package ru.ispras.masiw.plugin.aadl.semantic.support.namespace;

import java.util.HashSet;
import java.util.Set;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.semantic.core.AllRunner;
import ru.ispras.masiw.plugin.aadl.semantic.core.NamespaceModel;
import ru.ispras.masiw.plugin.aadl.semantic.support.Usefull;

public class ComponentTypeFunctions implements Usefull{

	public static void namespaceIntersection(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		PackageImpl curPackage = (PackageImpl) p;
		ComponentTypeImpl component = (ComponentTypeImpl) c;
		
		Set<AADLIdentifier> intersection = new HashSet<AADLIdentifier>(NamespaceModel.localClassifierNamespacesWithBindings
				.get(curPackage.getIdentifier()).get(component.getIdentifier()).keySet());
		
		if (component.getAncestor() != null) {
			if (component.getAncestor().eIsProxy()) {
				AllRunner.raiseCommonProblem("P43N3", component);
			} else {
				intersection.retainAll(NamespaceModel.localClassifierNamespacesWithBindings
					.get(curPackage.getIdentifier()).get(component.getAncestor().getPrototypeOrClassifier().getIdentifier()).keySet());
				if (!intersection.isEmpty()) {
					//iterate over all identifiers in the intersection
					for (AADLIdentifier obj: intersection) {
						AllRunner.raiseCommonProblem("P43N2", NamespaceModel.localClassifierNamespacesWithBindings
								.get(curPackage.getIdentifier()).get(component.getIdentifier()).get(obj));
					}
					//AllRunner.raiseCommonProblem("P43N2", component);
				}
			}
		}
	}
	
	public static void extensionTypeMatch(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		//PackageImpl curPackage = (PackageImpl) p;
		ComponentTypeImpl component = (ComponentTypeImpl) c;
		
		if (component.getAncestor() != null) {
			if (!AllRunner.typeMatch(component.getClass().getSimpleName(), 
					component.getAncestor().getPrototypeOrClassifier().getClass().getSimpleName()) &&
					//!component.getClass().getSimpleName().contains("Abstract") &&
					!component.getAncestor().getPrototypeOrClassifier().getClass().getSimpleName().contains("Abstract")) {
				AllRunner.raiseCommonProblem("P44L4", component);
			}
		}
	}
	
	private static boolean hasRequireModes(ComponentTypeImpl component) {
		if (component.getModes().size() > 0) {
			for (int i = 0; i < component.getModes().size(); i++) {
				if (component.getModes().get(i).isRequires())
					return true;
			}
		}
		return false;
	}
	
	private static boolean hasOtherModes(ComponentTypeImpl component) {
		if (component.getModes().size() > 0) {
			for (int i = 0; i < component.getModes().size(); i++) {
				if (!component.getModes().get(i).isRequires())
					return true;
			}
		}
		return false;
	}
	
	public static void modesCheck(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		ComponentTypeImpl component = (ComponentTypeImpl) c;
		if (hasRequireModes(component) && hasOtherModes(component)) {
			AllRunner.raiseCommonProblem("P43L6", component);
		}
	}
	
	@Override
	public void doWork(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		// TODO Auto-generated method stub
		
	}

}
