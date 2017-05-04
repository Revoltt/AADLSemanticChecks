package ru.ispras.masiw.plugin.aadl.semantic.updated;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AADLDeclaration;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AllClassifiersAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.ClassifierAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageSection;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;

public class PackageDeclaration {
	PackageNamespace parent;
	PackageSection src;
	boolean isPublic;
	
	Map<AADLIdentifier, IExtendedEObject> imports;
	
	Map<AADLIdentifier, IExtendedEObject> packageAliases;
	
	Map<AADLIdentifier, IExtendedEObject> classifierAliases;
	
	Map<AADLIdentifier, IExtendedEObject> allAliases;
	
	Map<AADLIdentifier, ComponentNamespace> componentNamespaces;
	
	PackageDeclaration(PackageSection p, PackageNamespace par, boolean pub) {
		imports = new HashMap<AADLIdentifier, IExtendedEObject>();
		packageAliases = new HashMap<AADLIdentifier, IExtendedEObject>();
		classifierAliases = new HashMap<AADLIdentifier, IExtendedEObject>();
		allAliases = new HashMap<AADLIdentifier, IExtendedEObject>();
		componentNamespaces = new HashMap<AADLIdentifier, ComponentNamespace>();
		
		parent = par;
		isPublic = pub;
		src = p;
		
		EList<AADLIdentifier> importsList = src.getImports();
		for (int i = 0; i < importsList.size(); i++) {
			if (imports.containsKey(importsList.get(i))) {
				// TODO warning - import is duplicated
			}
			imports.put(importsList.get(i), null);
			// TODO smth with property sets
		}
		
		EList<PackageAlias> packageAliasList = src.getPackageAliases();
		for (int i = 0; i < packageAliasList.size(); i++) {
			if (!imports.containsKey(packageAliasList.get(i).getPackage().getIdentifier())) {
				// TODO error - package referenced in alias is not imported
			}
			if (packageAliases.containsKey(packageAliasList.get(i).getIdentifier())) {
				// TODO error - package alias name conflicts with other package alias
			}
			if (parent.src.getIdentifier().equals(packageAliasList.get(i).getIdentifier())) {
				// TODO error - package alias name conflicts with package name where it's listed
			}
			if (imports.containsKey(packageAliasList.get(i).getIdentifier())) {
				// TODO error - package alias name conflicts with name of some imported package
			}
			packageAliases.put(packageAliasList.get(i).getIdentifier(), packageAliasList.get(i));
		}
		
		EList<ClassifierAlias> classifierAliasList = src.getClassifierAliases();
		for (int i = 0; i < classifierAliasList.size(); i++) {
			if (classifierAliases.containsKey(classifierAliasList.get(i).getIdentifier())) {
				// TODO error - classifier alias name conflicts with other classifier alias
			}
			if (packageAliases.containsKey(classifierAliasList.get(i).getIdentifier())) {
				// TODO error - classifier alias name conflicts with some package alias
			}
			if (parent.src.getIdentifier().equals(classifierAliasList.get(i).getIdentifier())) {
				// TODO error - classifier alias name conflicts with package name where it's listed
			}
			if (imports.containsKey(classifierAliasList.get(i).getIdentifier())) {
				// TODO error - classifer alias name conflicts with name of some imported package
			}
			classifierAliases.put(classifierAliasList.get(i).getIdentifier(), classifierAliasList.get(i));
		}
		
		EList<AllClassifiersAlias> allAliasList = src.getAllClassifiersAliases();
		for (int i = 0; i < allAliasList.size(); i++) {
			if (!imports.containsKey(allAliasList.get(i).getPackage().getIdentifier())) {
				// TODO error - package referenced in alias is not imported
			}
			allAliases.put(allAliasList.get(i).getIdentifier(), allAliasList.get(i));
		}
		
		EList<AADLDeclaration> componentList = src.getDeclarations();
		for (int i = 0; i < componentList.size(); i++) {
			if (classifierAliases.containsKey(componentList.get(i).getIdentifier())) {
				// TODO error - component name conflicts with some classifier alias
			}
			if (packageAliases.containsKey(componentList.get(i).getIdentifier())) {
				// TODO error - component name conflicts with some package alias
			}
			if (parent.src.getIdentifier().equals(componentList.get(i).getIdentifier())) {
				// TODO error - component name conflicts with package name where it's listed
			}
			if (imports.containsKey(componentList.get(i).getIdentifier())) {
				// TODO error - component name conflicts with name of some imported package
			}
			if (componentNamespaces.containsKey(componentList.get(i).getIdentifier())) {
				// TODO error - component name conflicts with name of other component
			}
			
			componentNamespaces.put(componentList.get(i).getIdentifier(), new ComponentNamespace(componentList.get(i)));
		}
	}
}
