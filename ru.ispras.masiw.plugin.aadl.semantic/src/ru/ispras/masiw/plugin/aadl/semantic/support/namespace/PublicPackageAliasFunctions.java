package ru.ispras.masiw.plugin.aadl.semantic.support.namespace;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.semantic.core.AllRunner;
import ru.ispras.masiw.plugin.aadl.semantic.core.NamespaceModel;
import ru.ispras.masiw.plugin.aadl.semantic.support.Usefull;

public class PublicPackageAliasFunctions implements Usefull{

	public static void uniquePackageAlias(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject unused1, AADLIdentifier unused2) {
		PackageAlias alias = (PackageAlias) c;
		PackageImpl curPackage = (PackageImpl) p;
		if (alias.getPackage().eIsProxy()) {
			// the package is not found in namespace - there is proxy instead
			String reference = alias.getPackage().eProxyURI().fragment();
			// package is referenced
			if (!NamespaceModel.globalPackageNamespace.containsKey(new AADLIdentifier(reference))) {
				// package name referenced is not found in global namespace
				AllRunner.raiseCommonProblem("P42N11global", alias);
			}
		} else {
			// package is not imported
			if (!NamespaceModel.localPublicImports.get(curPackage.getIdentifier())
					.containsKey(alias.getPackage().getIdentifier())) {
				// package referenced is not imported
				AllRunner.raiseCommonProblem("P42N11import", alias);
			}
			// package is present in global namespace
			if (NamespaceModel.localPublicImports.get(curPackage.getIdentifier())
					.containsKey(alias.getIdentifier())) {
				// alias name is the same as some imported package name
				AllRunner.raiseCommonProblem("P42N14package", alias);
			}
			if (curPackage.getIdentifier().equals(alias.getIdentifier())) {
				// alias name is the same as package name where it is defined
				AllRunner.raiseCommonProblem("P42N14defining", alias);
			}
			if (NamespaceModel.localPublicNamespaces.get(curPackage.getIdentifier()).containsKey(alias.getIdentifier()) || 
					NamespaceModel.localPublicClassifierAliases.get(curPackage.getIdentifier()).containsKey(alias.getIdentifier()) ||
					NamespaceModel.localPublicPackageAliases.get(curPackage.getIdentifier()).containsKey(alias.getIdentifier())) {
				// alias identifier is not unique by some other way
				AllRunner.raiseCommonProblem("P42N14other", alias);
			}
			// add identifier of package
			NamespaceModel.localPublicPackageAliases.get(curPackage.getIdentifier()).put(alias.getIdentifier(), alias);
		}
	}
	
	@Override
	public void doWork(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		// TODO Auto-generated method stub
		
	}

}
