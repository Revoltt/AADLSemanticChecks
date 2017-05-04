package ru.ispras.masiw.plugin.aadl.semantic.support.other;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.metamodel.ComponentClassifier;
import ru.ispras.masiw.plugin.aadl.metamodel.Subcomponent;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentImplementationImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.semantic.core.AllRunner;
import ru.ispras.masiw.plugin.aadl.semantic.support.Usefull;

public class SubcomponentFunctions implements Usefull{

	public static void componentNameExists(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
//		PackageImpl curpackage = (PackageImpl) p;
//		ComponentImplementationImpl component = (ComponentImplementationImpl) c;
		Subcomponent subcomponent = (Subcomponent) subc;
		
		if (subcomponent.getComponent().eIsProxy()) {
			AllRunner.raiseCommonProblem("P45N3", subcomponent);
		}
	}
	
	public static void subcomponentTypeMatch(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
//		PackageImpl curpackage = (PackageImpl) p;
//		ComponentImplementationImpl component = (ComponentImplementationImpl) c;
		Subcomponent subcomponent = (Subcomponent) subc;
		
		if (!AllRunner.typeMatch(subcomponent.getClass().getSimpleName(), 
				subcomponent.getComponent().getClass().getSimpleName()) &&
				!subcomponent.getComponent().getClass().getSimpleName().contains("Abstract")) {
			AllRunner.raiseCommonProblem("P45L1", subcomponent);
		}
	}
	
	@Override
	public void doWork(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		// TODO Auto-generated method stub
		
	}

}
