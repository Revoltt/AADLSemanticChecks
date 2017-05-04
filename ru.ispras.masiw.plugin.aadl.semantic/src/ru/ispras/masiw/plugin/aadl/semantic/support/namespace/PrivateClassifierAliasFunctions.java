package ru.ispras.masiw.plugin.aadl.semantic.support.namespace;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.ClassifierAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.impl.PackageDeclarationImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.semantic.core.AllRunner;
import ru.ispras.masiw.plugin.aadl.semantic.core.NamespaceModel;
import ru.ispras.masiw.plugin.aadl.semantic.support.Usefull;

public class PrivateClassifierAliasFunctions implements Usefull{

	public static void uniqueClassifierAlias(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		ClassifierAlias alias = (ClassifierAlias) c;
		PackageImpl curPackage = (PackageImpl) p;
		
		if (alias.getClassifier().eIsProxy()) {
			// the object is not found in namespace - there is proxy instead
			String reference = alias.getClassifier().eProxyURI().fragment();
			// classifier or feature group type is referenced
			// TODO check if feature group alias is classifierAlias
			if (!reference.contains("::")) {
				// no package is referenced
				AllRunner.raiseCommonProblem("P42N12other", alias);
			} else {
				String packageName = reference.substring(0, reference.lastIndexOf("::"));
				String identifier = reference.substring(reference.lastIndexOf("::") + 2);
				if (!NamespaceModel.globalPackageNamespace.containsKey(new AADLIdentifier(packageName))) {
					// package name referenced is not found in global namespace
					AllRunner.raiseCommonProblem("P42N11global", alias);
				} else if (!NamespaceModel.localPrivateImports.get(curPackage.getIdentifier()).containsKey(new AADLIdentifier(packageName)) &&
						!NamespaceModel.localPublicImports.get(curPackage.getIdentifier()).containsKey(new AADLIdentifier(packageName))) {
					// package referenced is not imported
					AllRunner.raiseCommonProblem("P42N11import", alias);
				} else if (!NamespaceModel.localPublicNamespaces.get(new AADLIdentifier(packageName)).containsKey(new AADLIdentifier(identifier))) {
					// classifier referenced is not found in package
					AllRunner.raiseCommonProblem("P42N12", alias);
				}
			}
		} else {
			// check that alias name is not used in package local namespace
			AADLIdentifier id = alias.getIdentifier();
			if (id == null)
				id = alias.getClassifier().getIdentifier();
			if (NamespaceModel.localPrivateNamespaces.get(curPackage.getIdentifier()).containsKey(id) || 
					NamespaceModel.localPublicNamespaces.get(curPackage.getIdentifier()).containsKey(id) || 
					NamespaceModel.localPrivateClassifierAliases.get(curPackage.getIdentifier()).containsKey(id) ||
					NamespaceModel.localPublicClassifierAliases.get(curPackage.getIdentifier()).containsKey(id) ||
					NamespaceModel.localPrivatePackageAliases.get(curPackage.getIdentifier()).containsKey(id) ||
					NamespaceModel.localPublicPackageAliases.get(curPackage.getIdentifier()).containsKey(id)) {
				if (alias.getIdentifier() != null) {
					// alias identifier is not unique by some other way
					AllRunner.raiseCommonProblem("P42N14other", alias);
				} else {
					// alias identifier is empty
					AllRunner.raiseCommonProblem("P42N15", alias);
				}
			}  else if (!NamespaceModel.localPublicNamespaces.get(
					((PackageDeclarationImpl)(alias.getClassifier().eContainer()).eContainer()).getIdentifier())
					.containsKey(alias.getClassifier().getIdentifier())) {
				if (!AllRunner.typeMatch(alias.getCategory().getName(), alias.getClassifier().getClass().getSimpleName())) {
					// types of classifiers do not match
					AllRunner.raiseCommonProblem("P42L4", alias);
				} else {
					// classifier referenced is not found in public section of referenced package
					AllRunner.raiseCommonProblem("P42N12", alias);
				}
			} else {
				if (!AllRunner.typeMatch(alias.getCategory().getName(), alias.getClassifier().getClass().getSimpleName())) {
					// types of classifiers do not match
					AllRunner.raiseCommonProblem("P42L4", alias);
				}
				// add identifier of alias
				NamespaceModel.localPrivateClassifierAliases.get(curPackage.getIdentifier()).put(alias.getIdentifier(), alias);
			}
		}
	}
	@Override
	public void doWork(IExtendedEObject p, IExtendedEObject c,
			IExtendedEObject subc, AADLIdentifier additional) {
		// TODO Auto-generated method stub
		
	}

}
