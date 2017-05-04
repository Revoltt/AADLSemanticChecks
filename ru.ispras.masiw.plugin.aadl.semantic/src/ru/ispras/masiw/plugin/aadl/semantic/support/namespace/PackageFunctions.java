package ru.ispras.masiw.plugin.aadl.semantic.support.namespace;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.impl.PropertySetImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.semantic.core.AllRunner;
import ru.ispras.masiw.plugin.aadl.semantic.core.NamespaceModel;
import ru.ispras.masiw.plugin.aadl.semantic.support.Usefull;

public class PackageFunctions implements Usefull{
	public static void uniquePackageName(IExtendedEObject p, IExtendedEObject unused1, IExtendedEObject unused2, AADLIdentifier unused3) {
		PackageImpl curPackage = (PackageImpl) p;
		if (NamespaceModel.globalPackageNamespace.containsKey(curPackage.getIdentifier()) || 
				NamespaceModel.globalPrSetNamespace.containsKey(curPackage.getIdentifier())) {
			// duplicate package name
			AllRunner.raiseCommonProblem("P41N1", curPackage);
		} else {
			NamespaceModel.globalPackageNamespace.put(curPackage.getIdentifier(), curPackage);
			NamespaceModel.localPublicNamespaces.put(curPackage.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
			NamespaceModel.localPrivateNamespaces.put(curPackage.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
			NamespaceModel.localClassifierNamespacesWithBindings.put(curPackage.getIdentifier(), 
					new HashMap<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>());
			
			NamespaceModel.localPublicImports.put(curPackage.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
			NamespaceModel.localPrivateImports.put(curPackage.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
			
			NamespaceModel.localPublicPackageAliases.put(curPackage.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
			NamespaceModel.localPrivatePackageAliases.put(curPackage.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
			
			NamespaceModel.localPublicClassifierAliases.put(curPackage.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
			NamespaceModel.localPrivateClassifierAliases.put(curPackage.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
			
			NamespaceModel.localPublicAllAliases.put(curPackage.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
			NamespaceModel.localPrivateAllAliases.put(curPackage.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
			
		}
	}
	
	public static void enoughPackageDeclarations(IExtendedEObject p, IExtendedEObject unused1, IExtendedEObject unused2, AADLIdentifier unused3) {
		PackageImpl curPackage = (PackageImpl) p;
		int publicDeclarations = 0, privateDeclarations = 0; // amounts of public and private package declarations
		for (int i = 0; i < curPackage.getPackageDeclarations().size(); i++) {
			if (curPackage.getPackageDeclarations().get(i).getPrivateSection() != null) {
				privateDeclarations = privateDeclarations + 1;
			}
			if (curPackage.getPackageDeclarations().get(i).getPublicSection() != null) {
				publicDeclarations = publicDeclarations + 1;
			}
		}
		
		if ((publicDeclarations > 1) || (privateDeclarations > 1)) {
			// only one public and one private package declaration is allowed
			AllRunner.raiseCommonProblem("P42N1", curPackage.getPackageDeclarations().get(0));
		}
	}
	
	public static void uniquePrSetName(IExtendedEObject p, IExtendedEObject unused1, IExtendedEObject unused2, AADLIdentifier unused3) {
		if (p.getClass().equals(PropertySetImpl.class)) {
			PropertySetImpl curPrSet = (PropertySetImpl) p;
		
			if (NamespaceModel.globalPackageNamespace.containsKey(curPrSet.getIdentifier()) ||
					NamespaceModel.globalPrSetNamespace.containsKey(curPrSet.getIdentifier())) {
				// duplicate property set name
				AllRunner.raiseCommonProblem("P41N1", curPrSet);
			} else {
				NamespaceModel.globalPrSetNamespace.put(curPrSet.getIdentifier(), curPrSet);
			}
		}
	}
	
	@Override
	public void doWork(IExtendedEObject p, IExtendedEObject c, IExtendedEObject subc, AADLIdentifier additional) {
	}
}
