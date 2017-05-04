package ru.ispras.masiw.plugin.aadl.semantic.support.namespace;

import java.util.HashMap;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AADLDeclaration;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentImplementationImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.semantic.core.AllRunner;
import ru.ispras.masiw.plugin.aadl.semantic.core.NamespaceModel;
import ru.ispras.masiw.plugin.aadl.semantic.support.Usefull;

public class PrivateComponentFunctions implements Usefull{

	public static void privateUniqueComponentName(IExtendedEObject p, IExtendedEObject c, IExtendedEObject unused1, AADLIdentifier unused2) {
		PackageImpl curPackage = (PackageImpl) p;
		AADLDeclaration component = (AADLDeclaration) c;
		if (NamespaceModel.localPrivateNamespaces.get(curPackage.getIdentifier()).containsKey(component.getIdentifier())) {
			// identifier in the package section is not unique
			if (ComponentTypeImpl.class.isAssignableFrom(component.getClass())) {
				// component type name duplicate
				AllRunner.raiseCommonProblem("P43N1", component);
			} else if (ComponentImplementationImpl.class.isAssignableFrom(component.getClass())) {
				// component implementation name duplicate
				AllRunner.raiseCommonProblem("P44N2", component);
			}
		} else {
			NamespaceModel.localPrivateNamespaces.get(curPackage.getIdentifier()).put(component.getIdentifier(), component);
			NamespaceModel.localClassifierNamespacesWithBindings.get(curPackage.getIdentifier()).put(component.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
		}
	}
	
	@Override
	public void doWork(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		// TODO Auto-generated method stub
		
	}

}
