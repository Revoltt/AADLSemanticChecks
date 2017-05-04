package ru.ispras.masiw.plugin.aadl.semantic.support.namespace;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageSection;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.semantic.core.AllRunner;
import ru.ispras.masiw.plugin.aadl.semantic.core.NamespaceModel;
import ru.ispras.masiw.plugin.aadl.semantic.support.Usefull;

public class PublicImportFunctions implements Usefull{

	public static void publicDuplicateImport(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier importId) {
		PackageImpl curPackage = (PackageImpl) p;
		PackageSection curPackageSection = (PackageSection) c;
		if (!(NamespaceModel.globalPackageNamespace.containsKey(importId) || 
				NamespaceModel.globalPrSetNamespace.containsKey(importId))) {
			// import is not present in global namespace
			AllRunner.raiseCommonProblem("P42N7", curPackageSection);
		} else {
			// TODO add reference to imported package or propertyset
			NamespaceModel.localPublicImports.get(curPackage.getIdentifier()).put(importId, 
					NamespaceModel.globalPackageNamespace.get(importId));
		}
	}
	
	@Override
	public void doWork(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier importId) {
		// TODO Auto-generated method stub
		
	}

}
