package ru.ispras.masiw.plugin.aadl.semantic.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.model.domain.AADLSpecificationDomain;


import ru.ispras.masiw.plugin.aadl.semantic.support.Usefull;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.PackageFunctions;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.PrivateComponentFunctions;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.PrivateImportFunctions;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.PrivatePackageAliasFunctions;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.PublicComponentFunctions;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.PublicImportFunctions;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.PublicPackageAliasFunctions;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.PublicClassifierAliasFunctions;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.PublicAllAliasFunctions;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.PrivateAllAliasFunctions;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.PrivateClassifierAliasFunctions;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.ComponentImplementationFunctions;
import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.ComponentTypeFunctions;

import ru.ispras.masiw.plugin.aadl.semantic.support.namespace.SubcomponentFunctions;

public class NamespaceModel {
	// names of packages and property sets in specification
	public static Map<AADLIdentifier, IExtendedEObject> globalPackageNamespace; 
	public static Map<AADLIdentifier, IExtendedEObject> globalPrSetNamespace; 
	
	// names of package classifiers
	public static Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>> localPublicNamespaces;
	public static Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>> localPrivateNamespaces;
	
	// names of package imports
	public static Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>> localPublicImports;
	public static Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>> localPrivateImports;
	
	// names of package aliases
	public static Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>> localPublicPackageAliases;
	public static Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>> localPrivatePackageAliases;
	
	// names of classifier aliases
	public static Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>> localPublicClassifierAliases;
	public static Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>> localPrivateClassifierAliases;
	
	// names of "all" aliases 
	public static Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>> localPublicAllAliases;
	public static Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>> localPrivateAllAliases;
	
	// names of defining identifiers of classifier
	public static Map<AADLIdentifier, Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>> localClassifierNamespacesWithBindings;
	
	// main function for creating namespace model
	public static void createModel(AADLSpecificationDomain model) {
		
		globalPackageNamespace = new HashMap<AADLIdentifier, IExtendedEObject>();
		globalPrSetNamespace = new HashMap<AADLIdentifier, IExtendedEObject>();
		
		localPublicNamespaces = new HashMap<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>();
		localPrivateNamespaces = new HashMap<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>();
		
		localPublicImports = new HashMap<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>();
		localPrivateImports = new HashMap<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>();
		
		localPublicPackageAliases = new HashMap<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>();
		localPrivatePackageAliases = new HashMap<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>();
		
		localPublicClassifierAliases = new HashMap<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>();
		localPrivateClassifierAliases = new HashMap<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>();
		
		localPublicAllAliases = new HashMap<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>();
		localPrivateAllAliases = new HashMap<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>();
		
		localClassifierNamespacesWithBindings = new HashMap<AADLIdentifier, Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>>();
		
		
//---------------------------------------------------------------------------------------
// First Inspector run adds package, component/component implementation and subcomponent names to namespace
		HashMap<String, ArrayList<Usefull>> funcs = new HashMap<String, ArrayList<Usefull>>();
		
		ArrayList<Usefull> packageFunctions = new ArrayList<Usefull>();
		packageFunctions.add(PackageFunctions::uniquePackageName);
		packageFunctions.add(PackageFunctions::enoughPackageDeclarations);
		packageFunctions.add(PackageFunctions::uniquePrSetName);
		funcs.put("prePackageInspect", packageFunctions);
		
		ArrayList<Usefull> publicComponentFunctions = new ArrayList<Usefull>();
		publicComponentFunctions.add(PublicComponentFunctions::publicUniqueComponentName);
		funcs.put("publicPreComponentInspect", publicComponentFunctions);
		
		ArrayList<Usefull> privateComponentFunctions = new ArrayList<Usefull>();
		privateComponentFunctions.add(PrivateComponentFunctions::privateUniqueComponentName);
		funcs.put("privatePreComponentInspect", privateComponentFunctions);
		
		ArrayList<Usefull> namespaceSubcomponentFunctions = new ArrayList<Usefull>();
		namespaceSubcomponentFunctions.add(SubcomponentFunctions::uniqueSubcomponentName);
		funcs.put("modes", namespaceSubcomponentFunctions);
		funcs.put("modetransitions", namespaceSubcomponentFunctions);
		funcs.put("subcomponents", namespaceSubcomponentFunctions);
		funcs.put("properties", namespaceSubcomponentFunctions);
		funcs.put("prototypes", namespaceSubcomponentFunctions);
		funcs.put("connections", namespaceSubcomponentFunctions);
		funcs.put("features", namespaceSubcomponentFunctions);
		funcs.put("flows", namespaceSubcomponentFunctions);
		funcs.put("annexes", namespaceSubcomponentFunctions);
		funcs.put("calls", namespaceSubcomponentFunctions);
		
		funcs.put("modesImpl", namespaceSubcomponentFunctions);
		funcs.put("modetransitionsImpl", namespaceSubcomponentFunctions);
		funcs.put("subcomponentsImpl", namespaceSubcomponentFunctions);
		funcs.put("propertiesImpl", namespaceSubcomponentFunctions);
		funcs.put("prototypesImpl", namespaceSubcomponentFunctions);
		funcs.put("connectionsImpl", namespaceSubcomponentFunctions);
		funcs.put("featuresImpl", namespaceSubcomponentFunctions);
		funcs.put("flowsImpl", namespaceSubcomponentFunctions);
		funcs.put("annexesImpl", namespaceSubcomponentFunctions);
		funcs.put("callsImpl", namespaceSubcomponentFunctions);
		
		Inspector.inspectModel(model, funcs);

//------------------------------------------------------------------------------------------
// Second run adds aliases checking: imports, package/classifier/all aliases
		funcs = new HashMap<String, ArrayList<Usefull>>();
		
		ArrayList<Usefull> publicImportFunctions = new ArrayList<Usefull>();
		publicImportFunctions.add(PublicImportFunctions::publicDuplicateImport);
		funcs.put("publicPackageImport", publicImportFunctions);
		
		ArrayList<Usefull> privateImportFunctions = new ArrayList<Usefull>();
		privateImportFunctions.add(PrivateImportFunctions::privateDuplicateImport);
		funcs.put("privatePackageImport", publicImportFunctions);
		
		ArrayList<Usefull> publicPackageAliasFunctions = new ArrayList<Usefull>();
		publicPackageAliasFunctions.add(PublicPackageAliasFunctions::uniquePackageAlias);
		funcs.put("publicPackageAlias", publicPackageAliasFunctions);
		
		ArrayList<Usefull> privatePackageAliasFunctions = new ArrayList<Usefull>();
		privatePackageAliasFunctions.add(PrivatePackageAliasFunctions::uniquePackageAlias);
		funcs.put("privatePackageAlias", privatePackageAliasFunctions);
		
		ArrayList<Usefull> publicClassifierAliasFunctions = new ArrayList<Usefull>();
		publicClassifierAliasFunctions.add(PublicClassifierAliasFunctions::uniqueClassifierAlias);
		funcs.put("publicClassifierAlias", publicClassifierAliasFunctions);
		
		ArrayList<Usefull> privateClassifierAliasFunctions = new ArrayList<Usefull>();
		privateClassifierAliasFunctions.add(PrivateClassifierAliasFunctions::uniqueClassifierAlias);
		funcs.put("privateClassifierAlias", privateClassifierAliasFunctions);
		
		ArrayList<Usefull> publicAllAliasFunctions = new ArrayList<Usefull>();
		publicAllAliasFunctions.add(PublicAllAliasFunctions::namespaceIntersection);
		funcs.put("publicAllAlias", publicAllAliasFunctions);
		
		ArrayList<Usefull> privateAllAliasFunctions = new ArrayList<Usefull>();
		privateAllAliasFunctions.add(PrivateAllAliasFunctions::namespaceIntersection);
		funcs.put("privateAllAlias", privateAllAliasFunctions);
		
		ArrayList<Usefull> componentTypeFunctions = new ArrayList<Usefull>();
		componentTypeFunctions.add(ComponentTypeFunctions::namespaceIntersection);
		componentTypeFunctions.add(ComponentTypeFunctions::extensionTypeMatch);
		componentTypeFunctions.add(ComponentTypeFunctions::modesCheck);
		funcs.put("preComponentType", componentTypeFunctions);
		
		ArrayList<Usefull> componentImplementationFunctions = new ArrayList<Usefull>();
		componentImplementationFunctions.add(ComponentImplementationFunctions::namespaceIntersection);
		componentImplementationFunctions.add(ComponentImplementationFunctions::extensionTypeMatch);
		componentImplementationFunctions.add(ComponentImplementationFunctions::requireModesCheck);
		funcs.put("preComponentImpl", componentImplementationFunctions);
		
		Inspector.inspectModel(model, funcs);
	}
}