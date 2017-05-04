package ru.ispras.masiw.plugin.aadl.semantic.support.namespace;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.metamodel.NamedElement;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AADLDeclaration;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.semantic.core.AllRunner;
import ru.ispras.masiw.plugin.aadl.semantic.core.NamespaceModel;
import ru.ispras.masiw.plugin.aadl.semantic.support.Usefull;

public class SubcomponentFunctions implements Usefull{
	public static void uniqueSubcomponentName(IExtendedEObject p, IExtendedEObject c, IExtendedEObject subc, AADLIdentifier unused)
	{
		PackageImpl curPackage = (PackageImpl) p;
		AADLDeclaration component = (AADLDeclaration) c;
		NamedElement subcomponent = (NamedElement) subc;
		if (NamespaceModel.localClassifierNamespacesWithBindings.get(curPackage.getIdentifier()).get(component.getIdentifier()).containsKey(subcomponent.getIdentifier())) {
			AllRunner.raiseCommonProblem("P44N3", subcomponent);
		} else {
			NamespaceModel.localClassifierNamespacesWithBindings.get(curPackage.getIdentifier()).get(component.getIdentifier()).put(subcomponent.getIdentifier(), subcomponent);
		}
	}

	@Override
	public void doWork(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		// TODO Auto-generated method stub
		
	}
}
