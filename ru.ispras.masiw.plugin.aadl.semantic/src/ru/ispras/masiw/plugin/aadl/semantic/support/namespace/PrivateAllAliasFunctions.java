package ru.ispras.masiw.plugin.aadl.semantic.support.namespace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AllClassifiersAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.semantic.core.AllRunner;
import ru.ispras.masiw.plugin.aadl.semantic.core.NamespaceModel;
import ru.ispras.masiw.plugin.aadl.semantic.support.Usefull;

public class PrivateAllAliasFunctions implements Usefull{
	public static void namespaceIntersection(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		
		AllClassifiersAlias alias = (AllClassifiersAlias) c;
		PackageImpl curPackage = (PackageImpl) p;
		
		// check if namespaces of package and alias package intersect
		if (alias.getPackage().eIsProxy()) {
			// package is not found
			AllRunner.raiseCommonProblem("P42N11global", alias);
		} else if (!NamespaceModel.localPublicImports.get(curPackage.getIdentifier()).containsKey(alias.getPackage().getIdentifier()) &&
				!NamespaceModel.localPrivateImports.get(curPackage.getIdentifier()).containsKey(alias.getPackage().getIdentifier())) {
			// package is not in import
			AllRunner.raiseCommonProblem("P42N11import", alias);
		} else {
			Map<AADLIdentifier, IExtendedEObject> intersection = new HashMap<AADLIdentifier, IExtendedEObject>(
					NamespaceModel.localPublicNamespaces.get(alias.getPackage().getIdentifier()));
			Set<AADLIdentifier> temp = new HashSet<AADLIdentifier>(
					NamespaceModel.localPrivateNamespaces.get(curPackage.getIdentifier()).keySet());
			temp.addAll(NamespaceModel.localPublicNamespaces.get(curPackage.getIdentifier()).keySet());
			intersection.keySet().retainAll(temp);
			if (!intersection.keySet().isEmpty()) {
				AllRunner.raiseCommonProblem("P42N16", alias);
			}
		}
	}
	
	@Override
	public void doWork(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		// TODO Auto-generated method stub
		
	}

}
