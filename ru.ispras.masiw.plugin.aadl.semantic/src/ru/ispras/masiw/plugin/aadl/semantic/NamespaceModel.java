package ru.ispras.masiw.plugin.aadl.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import ru.ispras.antlr.v4.editing.core.runtime.CommonProblem;
import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.ProblemHelper;
import ru.ispras.masiw.plugin.aadl.metamodel.Classifier;
import ru.ispras.masiw.plugin.aadl.metamodel.Connection;
import ru.ispras.masiw.plugin.aadl.metamodel.Feature;
import ru.ispras.masiw.plugin.aadl.metamodel.Flow;
import ru.ispras.masiw.plugin.aadl.metamodel.Mode;
import ru.ispras.masiw.plugin.aadl.metamodel.ModeTransition;
import ru.ispras.masiw.plugin.aadl.metamodel.NamedElement;
import ru.ispras.masiw.plugin.aadl.metamodel.Prototype;
import ru.ispras.masiw.plugin.aadl.metamodel.Subcomponent;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AADLDeclaration;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AllClassifiersAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.ClassifierAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.ClassifierWithBindings;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageDeclaration;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageSection;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PropertyAssociation;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.SubprogramCallSequence;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.impl.PropertySetImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.AbstractTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ClassifierImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentImplementationImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.DataTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.NamedElementImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.model.domain.AADLSpecificationDomain;

public class NamespaceModel {
	public static ProblemHelper helper;
	
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
	//public static Map<AADLIdentifier, Map<AADLIdentifier, Set<AADLIdentifier>>> localClassifierNamespaces;
	public static Map<AADLIdentifier, Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>> localClassifierNamespacesWithBindings;
	
	
	//function for raising problems
	private static void raiseCommonProblem(String id, IExtendedEObject x) {
		try {
			CommonProblem problem = helper.createProblem(id);
			x.getProblems().add(problem);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// function for getting inherited namespace of classifier
	private static Set<AADLIdentifier> getAncestorNamespaces(ClassifierImpl ctype) {
		//Set<AADLIdentifier> localNamespace = (localClassifierNamespaces.get(p.getIdentifier())).get(ctype);
		Set<AADLIdentifier> inheritedNamespace = new HashSet<AADLIdentifier>();
		ClassifierImpl cur = ctype;
		while (cur.getAncestor() != null) {
			cur = (ClassifierImpl) cur.getAncestor().getPrototypeOrClassifier();
			//inheritedNamespace.addAll((localClassifierNamespaces.get(p.getIdentifier())).get(cur.getIdentifier()));
			inheritedNamespace.addAll((localClassifierNamespacesWithBindings.get(((NamedElement) cur.eContainer().eContainer()).getIdentifier())).get(cur.getIdentifier()).keySet());
			
		}
		return inheritedNamespace;
	}
	
	// function to check if category type and referenced component type of alias_declaration match each other
	private static boolean typeMatch(String s1, String s2) { // was categoryType classifierType
		if (s1.endsWith("Impl")) {
			s1 = s1.substring(0, s1.length() - "Impl".length());
		}
		
		if (s2.endsWith("Impl")) {
			s2 = s2.substring(0, s2.length() - "Impl".length());
		}
		
		if (s1.endsWith("Implementation")) {
			s1 = s1.substring(0, s1.length() - "Implementation".length());
		}
		
		if (s2.endsWith("Implementation")) {
			s2 = s2.substring(0, s2.length() - "Implementation".length());
		}
		
		if (s1.endsWith("Type")) {
			s1 = s1.substring(0, s1.length() - "Type".length());
		}
		
		if (s2.endsWith("Type")) {
			s2 = s2.substring(0, s2.length() - "Type".length());
		}
	
		return s1.equals(s2);
		
//		if (classifierType.startsWith(categoryType)) {
//			return true;
//		}
//		return false;
	}
	
	// main function for creating namespace model
	public static void createModel(ProblemHelper h, AADLSpecificationDomain model) {
		TreeIterator<EObject> x = model.getAADLPackagesResource().getAllContents();
		ArrayList<EObject> l = new ArrayList<EObject>();
		helper = h;
		while (x.hasNext()) {
			l.add(x.next()); // list of packages
		}
		
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
		
		//localClassifierNamespaces = new HashMap<AADLIdentifier, Map<AADLIdentifier, Set<AADLIdentifier>>>();
		localClassifierNamespacesWithBindings = new HashMap<AADLIdentifier, Map<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>>();
		
		
		// create global namespace of packages and property sets
		for (int i = 0; i < l.size(); i++) {
			if ((l.get(i)).getClass().equals(PackageImpl.class)) {
				PackageImpl curPackage = (PackageImpl) l.get(i);
				
				if (globalPackageNamespace.containsKey(curPackage.getIdentifier()) || globalPrSetNamespace.containsKey(curPackage.getIdentifier())) {
					// duplicate package name
					raiseCommonProblem("P41N1", curPackage);
				} else {
					globalPackageNamespace.put(curPackage.getIdentifier(), curPackage);
				}
			} else if ((l.get(i)).getClass().equals(PropertySetImpl.class)) {
				PropertySetImpl curPrSet = (PropertySetImpl) l.get(i);
				
				if (globalPackageNamespace.containsKey(curPrSet.getIdentifier()) || globalPrSetNamespace.containsKey(curPrSet.getIdentifier())) {
					// duplicate property set name
					raiseCommonProblem("P41N1", curPrSet);
				} else {
					globalPrSetNamespace.put(curPrSet.getIdentifier(), curPrSet);
				}
			}
		}
		
		for (int i = 0; i < l.size(); i++) {
			if ((l.get(i)).getClass().equals(PackageImpl.class)) {
				PackageImpl curPackage = (PackageImpl) l.get(i);
				packageNamespaceCreate(curPackage);
			} else if ((l.get(i)).getClass().equals(PropertySetImpl.class)) {
				PropertySetImpl curPrSet = (PropertySetImpl) l.get(i);
			}
		}
		
		for (int i = 0; i < l.size(); i++) {
			if ((l.get(i)).getClass().equals(PackageImpl.class)) {
				PackageImpl curPackage = (PackageImpl) l.get(i);
				packageNamespaceCheck(curPackage);
			} else if ((l.get(i)).getClass().equals(PropertySetImpl.class)) {
				PropertySetImpl curPrSet = (PropertySetImpl) l.get(i);
			}
		}
	}
	
	public static void packageNamespaceCheck(PackageImpl p) {
		PackageSection publicPackageSection = null;
		PackageSection privatePackageSection = null;
		
		int publicDeclarations = 0, privateDeclarations = 0; // amounts of public and private package declarations
		for (int i = 0; i < p.getPackageDeclarations().size(); i++) {
			if (p.getPackageDeclarations().get(i).getPrivateSection() != null) {
				privateDeclarations = privateDeclarations + 1;
				privatePackageSection = p.getPackageDeclarations().get(i).getPrivateSection();
			}
			if (p.getPackageDeclarations().get(i).getPublicSection() != null) {
				publicDeclarations = publicDeclarations + 1;
				publicPackageSection = p.getPackageDeclarations().get(i).getPublicSection();
			}
		}
		
		if ((publicDeclarations > 1) || (privateDeclarations > 1)) {
			// only one public and one private package declaration is allowed
			// this type of error should be already raised earlier
			
			raiseCommonProblem("P42N1", p.getPackageDeclarations().get(0));
		} else {
			if (publicPackageSection != null) {
				// get imports of public section
				EList<AADLIdentifier> imports = publicPackageSection.getImports();
				for (int i = 0; i < imports.size(); i++) {
					if (!(globalPackageNamespace.containsKey(imports.get(i)) || globalPrSetNamespace.containsKey(imports.get(i)))) {
						// import is not present in global namespace
						raiseCommonProblem("P42N7", publicPackageSection);
					} else {
						// TODO add reference to imported package or propertyset
						localPublicImports.get(p.getIdentifier()).put(imports.get(i), null);
					}
				}
				// get package aliases of public section
				EList<PackageAlias> packageAliases = publicPackageSection.getPackageAliases();
				for (int i = 0; i < packageAliases.size(); i++) {
					if (packageAliases.get(i).getPackage().eIsProxy()) {
						// the package is not found in namespace - there is proxy instead
						String reference = packageAliases.get(i).getPackage().eProxyURI().fragment();
						// package is referenced
						if (!globalPackageNamespace.containsKey(new AADLIdentifier(reference))) {
							// package name referenced is not found in global namespace
							raiseCommonProblem("P42N11global", packageAliases.get(i));
						}
					} else {
						// package is not imported
						if (!localPublicImports.get(p.getIdentifier()).containsKey(packageAliases.get(i).getPackage().getIdentifier())) {
							// package referenced is not imported
							raiseCommonProblem("P42N11import", packageAliases.get(i));
						}
						// package is present in global namespace
						if (localPublicImports.get(p.getIdentifier()).containsKey(packageAliases.get(i).getIdentifier())) {
							// alias name is the same as some imported package name
							raiseCommonProblem("P42N14package", packageAliases.get(i));
						}
						if (p.getIdentifier().equals(packageAliases.get(i).getIdentifier())) {
							// alias name is the same as package name where it is defined
							raiseCommonProblem("P42N14defining", packageAliases.get(i));
						}
						if (localPublicNamespaces.get(p.getIdentifier()).containsKey(packageAliases.get(i).getIdentifier()) || 
								localPublicClassifierAliases.get(p.getIdentifier()).containsKey(packageAliases.get(i).getIdentifier()) ||
								localPublicPackageAliases.get(p.getIdentifier()).containsKey(packageAliases.get(i).getIdentifier())) {
							// alias identifier is not unique by some other way
							raiseCommonProblem("P42N14other", packageAliases.get(i));
						}
						// add identifier of package
						localPublicPackageAliases.get(p.getIdentifier()).put(packageAliases.get(i).getIdentifier(), packageAliases.get(i));
					}
				}
				// get classifier aliases of public section
				EList<ClassifierAlias> classifierAliases = publicPackageSection.getClassifierAliases();
				for (int i = 0; i < classifierAliases.size(); i++) {	
					if (classifierAliases.get(i).getClassifier().eIsProxy()) {
						// the object is not found in namespace - there is proxy instead
						String reference = classifierAliases.get(i).getClassifier().eProxyURI().fragment();
						// classifier or feature group type is referenced
						// TODO check if feature group alias is classifierAlias
						if (!reference.contains("::")) {
							// no package is referenced
							raiseCommonProblem("P42N12other", classifierAliases.get(i));
						} else {
							String packageName = reference.substring(0, reference.lastIndexOf("::"));
							String identifier = reference.substring(reference.lastIndexOf("::") + 2);
							if (!globalPackageNamespace.containsKey(new AADLIdentifier(packageName))) {
								// package name referenced is not found in global package namespace
								raiseCommonProblem("P42N11global", classifierAliases.get(i));
							} else if (!localPublicImports.get(p.getIdentifier()).containsKey(new AADLIdentifier(packageName))) {
								// package referenced is not imported
								raiseCommonProblem("P42N11import", classifierAliases.get(i));
							} else if (!localPublicNamespaces.get(new AADLIdentifier(packageName)).containsKey(new AADLIdentifier(identifier))) {
								// classifier referenced is not found in package
								raiseCommonProblem("P42N12", classifierAliases.get(i));
							}
						}
					} else {
						// check that alias name is not used in package local namespace
						AADLIdentifier id = classifierAliases.get(i).getIdentifier();
						if (id == null)
							id = classifierAliases.get(i).getClassifier().getIdentifier();
						if (localPublicNamespaces.get(p.getIdentifier()).containsKey(id) || 
								localPublicClassifierAliases.get(p.getIdentifier()).containsKey(id) ||
								localPublicPackageAliases.get(p.getIdentifier()).containsKey(id)) {
							if (classifierAliases.get(i).getIdentifier() != null) {
								// alias identifier is not unique by some other way
								raiseCommonProblem("P42N14other", classifierAliases.get(i));
							} else {
								// alias identifier is empty
								raiseCommonProblem("P42N15", classifierAliases.get(i));
							}
						}
						// add identifier of alias
						localPublicClassifierAliases.get(p.getIdentifier()).put(classifierAliases.get(i).getIdentifier(), classifierAliases.get(i));
						
						if (!typeMatch(classifierAliases.get(i).getCategory().getName(), classifierAliases.get(i).getClassifier().getClass().getSimpleName())) {
							// types of classifiers do not match
							raiseCommonProblem("P42L4", classifierAliases.get(i));
						}
					}
				}
				// get "all" aliases of public section
				EList<AllClassifiersAlias> allAliases = publicPackageSection.getAllClassifiersAliases();
				for (int i = 0; i < allAliases.size(); i++) {
					// check if namespaces of package and alias package intersect
					if (allAliases.get(i).getPackage().eIsProxy()) {
						// package is not found
						raiseCommonProblem("P42N11global", allAliases.get(i));
					} else if (!localPublicImports.get(p.getIdentifier()).containsKey(allAliases.get(i).getPackage().getIdentifier())) {
						// package is not in import
						raiseCommonProblem("P42N11import", allAliases.get(i));
					} else {
						Map<AADLIdentifier, IExtendedEObject> intersection = new HashMap<AADLIdentifier, IExtendedEObject>(localPublicNamespaces.get(allAliases.get(i).getPackage().getIdentifier()));
						intersection.keySet().retainAll(localPublicNamespaces.get(p.getIdentifier()).keySet());
						if (!intersection.keySet().isEmpty()) {
							raiseCommonProblem("P42N16", allAliases.get(i));
						}
					}
				}
				
				for (int i = 0; i < publicPackageSection.getDeclarations().size(); i++) {
					// check classifier rules
					classifierCheck(publicPackageSection.getDeclarations().get(i), p);
				}
			}
			if (privatePackageSection != null) {
				// get imports of private section
				EList<AADLIdentifier> imports = privatePackageSection.getImports();
				for (int i = 0; i < imports.size(); i++) {
					if (!(globalPackageNamespace.containsKey(imports.get(i)) || globalPrSetNamespace.containsKey(imports.get(i)))) {
						// import is not present in global namespace
						raiseCommonProblem("P42N7", privatePackageSection);
					} else {
						// TODO add reference to imported package or propertyset
						localPrivateImports.get(p.getIdentifier()).put(imports.get(i), null);
					}
				}
				
				// get package aliases of private section
				EList<PackageAlias> packageAliases = privatePackageSection.getPackageAliases();
				for (int i = 0; i < packageAliases.size(); i++) {
					if (packageAliases.get(i).getPackage().eIsProxy()) {
						// the package is not found in namespace - there is proxy instead
						String reference = packageAliases.get(i).getPackage().eProxyURI().fragment();
						// package is referenced
						if (!globalPackageNamespace.containsKey(new AADLIdentifier(reference))) {
							// package name referenced is not found in global namespace
							raiseCommonProblem("P42N11global", packageAliases.get(i));
						}
					} else {
						// package is not imported
						if (!localPrivateImports.get(p.getIdentifier()).containsKey(packageAliases.get(i).getPackage().getIdentifier())) {
							// package referenced is not imported
							raiseCommonProblem("P42N11import", packageAliases.get(i));
						}
						// package is present in global namespace
						if (localPrivateImports.get(p.getIdentifier()).containsKey(packageAliases.get(i).getIdentifier())) {
							// alias name is the same as some imported package name
							raiseCommonProblem("P42N14package", packageAliases.get(i));
						}
						if (p.getIdentifier().equals(packageAliases.get(i).getIdentifier())) {
							// alias name is the same as package name where it is defined
							raiseCommonProblem("P42N14defining", packageAliases.get(i));
						}
						if (localPrivateNamespaces.get(p.getIdentifier()).containsKey(packageAliases.get(i).getIdentifier()) || 
								localPrivateClassifierAliases.get(p.getIdentifier()).containsKey(packageAliases.get(i).getIdentifier()) ||
								localPrivatePackageAliases.get(p.getIdentifier()).containsKey(packageAliases.get(i).getIdentifier())) {
							// alias identifier is not unique by some other way
							raiseCommonProblem("P42N14other", packageAliases.get(i));
						}
						// add identifier of package
						localPrivatePackageAliases.get(p.getIdentifier()).put(packageAliases.get(i).getIdentifier(), packageAliases.get(i));
					}
				}
				
				// get classifier aliases of private section
				EList<ClassifierAlias> classifierAliases = privatePackageSection.getClassifierAliases();
				for (int i = 0; i < classifierAliases.size(); i++) {	
					if (classifierAliases.get(i).getClassifier().eIsProxy()) {
						// the object is not found in namespace - there is proxy instead
						String reference = classifierAliases.get(i).getClassifier().eProxyURI().fragment();
						// classifier or feature group type is referenced
						// TODO check if feature group alias is classifierAlias
						if (!reference.contains("::")) {
							// no package is referenced
							raiseCommonProblem("P42N12other", classifierAliases.get(i));
						} else {
							String packageName = reference.substring(0, reference.lastIndexOf("::"));
							String identifier = reference.substring(reference.lastIndexOf("::") + 2);
							if (!globalPackageNamespace.containsKey(new AADLIdentifier(packageName))) {
								// package name referenced is not found in global namespace
								raiseCommonProblem("P42N11global", classifierAliases.get(i));
							} else if (!localPrivateImports.get(p.getIdentifier()).containsKey(new AADLIdentifier(packageName))) {
								// package referenced is not imported
								raiseCommonProblem("P42N11import", classifierAliases.get(i));
							} else if (!localPrivateNamespaces.get(new AADLIdentifier(packageName)).containsKey(new AADLIdentifier(identifier))) {
								// classifier referenced is not found in package
								raiseCommonProblem("P42N12", classifierAliases.get(i));
							}
						}
					} else {
						// check that alias name is not used in package local namespace
						AADLIdentifier id = classifierAliases.get(i).getIdentifier();
						if (id == null)
							id = classifierAliases.get(i).getClassifier().getIdentifier();
						if (localPrivateNamespaces.get(p.getIdentifier()).containsKey(id) || 
								localPrivateClassifierAliases.get(p.getIdentifier()).containsKey(id) ||
								localPrivatePackageAliases.get(p.getIdentifier()).containsKey(id)) {
							if (classifierAliases.get(i).getIdentifier() != null) {
								// alias identifier is not unique by some other way
								raiseCommonProblem("P42N14other", classifierAliases.get(i));
							} else {
								// alias identifier is empty
								raiseCommonProblem("P42N15", classifierAliases.get(i));
							}
						}
						// add identifier of alias
						localPrivateClassifierAliases.get(p.getIdentifier()).put(classifierAliases.get(i).getIdentifier(), classifierAliases.get(i));
						
						if (!typeMatch(classifierAliases.get(i).getCategory().getName(), classifierAliases.get(i).getClassifier().getClass().getSimpleName())) {
							// types of classifiers do not match
							raiseCommonProblem("P42L4", classifierAliases.get(i));
						}
					}
				}
				
				// get "all" aliases of private section
				EList<AllClassifiersAlias> allAliases = publicPackageSection.getAllClassifiersAliases();
				for (int i = 0; i < allAliases.size(); i++) {
					// check if namespaces of package and alias package intersect
					if (allAliases.get(i).getPackage().eIsProxy()) {
						// package is not found
						raiseCommonProblem("P42N11global", allAliases.get(i));
					} else if (!localPublicImports.get(p.getIdentifier()).containsKey(allAliases.get(i).getPackage().getIdentifier())) {
						// package is not in import
						raiseCommonProblem("P42N11import", allAliases.get(i));
					} else {
						Map<AADLIdentifier, IExtendedEObject> x = localPublicNamespaces.get(allAliases.get(i).getPackage().getIdentifier());
						x.keySet().retainAll(localPrivateNamespaces.get(p.getIdentifier()).keySet());
						if (!x.isEmpty()) {
							raiseCommonProblem("P42N16", allAliases.get(i));
						}
					}
				}
				
				for (int i = 0; i < privatePackageSection.getDeclarations().size(); i++) {
					// check classifier rules
					classifierCheck(privatePackageSection.getDeclarations().get(i), p);
				}
			}
		}
	}
	
	public static void classifierCheck(AADLDeclaration c, PackageImpl p) {
		if (ComponentTypeImpl.class.isAssignableFrom(c.getClass())) {
			ComponentTypeImpl componentTypeImpl = (ComponentTypeImpl) c;
			if (componentTypeImpl.getAncestor() != null) {
				// component which is extended should exist
				// TODO check if referenced package is in import declaration
				
				//??? does not work: EcoreUtil.resolveAll(componentTypeImpl.getAncestor());
				NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
				
				if (ancestor.eIsProxy()) {
					// the ancestor is not found in namespace - there is proxy instead
					// TODO or ancestor is in another package and alias is used instead of it's real name
					raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
				} else if (!(ancestor.getClass().equals(componentTypeImpl.getClass()) || ancestor.getClass().equals(AbstractTypeImpl.class))) {
					// classes classifier and it's ancestor should match
					raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
				} else if ((((ComponentTypeImpl) ancestor).getModes().size() > 0) && (componentTypeImpl.getModes().size() > 0) &&
						(((ComponentTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
					// both modes and requires modes subclauses are not allowed
					raiseCommonProblem("P43L6", componentTypeImpl);
				} else {
					// check intersection of component type local namespace with namespaces of it's ancestors
					Set<AADLIdentifier> inheritedNamespace = getAncestorNamespaces(componentTypeImpl);
					//Set<AADLIdentifier> intersection = new HashSet<AADLIdentifier>(localClassifierNamespaces.get(p.getIdentifier()).get(componentTypeImpl.getIdentifier()));
					Set<AADLIdentifier> intersection = new HashSet<AADLIdentifier>(localClassifierNamespacesWithBindings.get(p.getIdentifier()).get(componentTypeImpl.getIdentifier()).keySet());
					
					intersection.retainAll(inheritedNamespace);
					if (!intersection.isEmpty()) {
						raiseCommonProblem("P43N4", componentTypeImpl);
					}
				}
				// TODO prototype bindings
			}
			// TODO refinements
		} else if (ComponentImplementationImpl.class.isAssignableFrom(c.getClass())) {
			ComponentImplementationImpl componentImplementationImpl = (ComponentImplementationImpl) c;
			// check if there is intersection with local namespace of corresponding component type
			//Set<AADLIdentifier> intersection = new HashSet<AADLIdentifier>(localClassifierNamespaces.get(p.getIdentifier()).get(componentImplementationImpl.getIdentifier()));
			Set<AADLIdentifier> intersection = new HashSet<AADLIdentifier>(localClassifierNamespacesWithBindings.get(p.getIdentifier()).get(componentImplementationImpl.getIdentifier()).keySet());
			
			intersection.retainAll(localClassifierNamespacesWithBindings.get(p.getIdentifier()).get(componentImplementationImpl.getComponentType().getIdentifier()).keySet());
			if (!intersection.isEmpty()) {
				raiseCommonProblem("P44N4", c);
			}
			
			// check if implementation type matches with classifier type
			if (!typeMatch(componentImplementationImpl.getClass().getSimpleName(), componentImplementationImpl.getComponentType().getClass().getSimpleName())) {
				raiseCommonProblem("P44L3", c);
			}
			
			if (componentImplementationImpl.getAncestor() != null) {
				// component implementation which is extended should exist
				// TODO check if referenced package is in import declaration
				NamedElement ancestor = componentImplementationImpl.getAncestor().getPrototypeOrClassifier();
				if (ancestor.eIsProxy()) {
					// the ancestor is not found in namespace - there is proxy instead
					// TODO problem not is list
				} else if (!(ancestor.getClass().equals(componentImplementationImpl.getClass()) || ancestor.getClass().equals(AbstractTypeImpl.class))) {
					// classes of classifier and it's ancestor should match
					raiseCommonProblem("P44L4", componentImplementationImpl.getAncestor());
				}
				// TODO prototype bindings, refinements
			}
			
			// both public and private section contain component implementation with the same name
			if (localPublicNamespaces.get(p.getIdentifier()).containsKey(componentImplementationImpl.getIdentifier()) && 
					localPrivateNamespaces.get(p.getIdentifier()).containsKey(componentImplementationImpl.getIdentifier())) {
				// we are processing public section
				if (componentImplementationImpl.eContainer().equals(
						((PackageDeclaration) componentImplementationImpl.eContainer().eContainer()).getPublicSection())) {
					// if component implementation contains anything but properties and modes
					// that is subcomponents, subprogram calls, prototypes, connections, flows - then error
					if ((componentImplementationImpl.getSubcomponents().size() > 0) ||
							(componentImplementationImpl.getCalls().size() > 0) ||
							(componentImplementationImpl.getPrototypes().size() > 0) ||
							(componentImplementationImpl.getConnections().size() > 0) ||
							(componentImplementationImpl.getFlows().size() > 0)) {
						raiseCommonProblem("P42L3", c);
					}
				}
			}
			
			// check modes of component type
			EList<Mode> typeModes = componentImplementationImpl.getComponentType().getModes();
			for (int i = 0; i < typeModes.size(); i++) {
				if (typeModes.get(i).isRequires()) {
					// if component type has requires modes then component implementation cannot contain ANY modes subclause
					// TODO check also ancestors' requires modes
					if (componentImplementationImpl.getModes().size() > 0) {
						raiseCommonProblem("P44L6", c);
					}
				} else {
					// component type has modes then component implementation cannot have mode declarations
					// TODO check also ancestors' modes
					EList<Mode> modes = componentImplementationImpl.getModes();
					for (int j = 0; j < modes.size(); j++) {
						if (!modes.get(i).isRequires()) {
							raiseCommonProblem("P44L7", c);
						}
					}
				}
			}
		}
	//	if (p.getClass().equals(DataTypeImpl.class)) {
	//		// for Data type components
	//		DataTypeImpl componentTypeImpl = ((DataTypeImpl) p);	
	//	}
	}
	
	public static void packageNamespaceCreate(PackageImpl p) {
		localPublicNamespaces.put(p.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
		localPrivateNamespaces.put(p.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
		
		localPublicImports.put(p.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
		localPrivateImports.put(p.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
		
		localPublicPackageAliases.put(p.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
		localPrivatePackageAliases.put(p.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
		
		localPublicClassifierAliases.put(p.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
		localPrivateClassifierAliases.put(p.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
		
		localPublicAllAliases.put(p.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
		localPrivateAllAliases.put(p.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>());
		
		
		PackageSection publicPackageSection = null;
		PackageSection privatePackageSection = null;
		
		int publicDeclarations = 0, privateDeclarations = 0; // amounts of public and private package declarations
		for (int i = 0; i < p.getPackageDeclarations().size(); i++) {
			if (p.getPackageDeclarations().get(i).getPrivateSection() != null) {
				privateDeclarations = privateDeclarations + 1;
				privatePackageSection = p.getPackageDeclarations().get(i).getPrivateSection();
			}
			if (p.getPackageDeclarations().get(i).getPublicSection() != null) {
				publicDeclarations = publicDeclarations + 1;
				publicPackageSection = p.getPackageDeclarations().get(i).getPublicSection();
			}
		}
		
		if ((publicDeclarations > 1) || (privateDeclarations > 1)) {
			// only one public and one private package declaration is allowed
			raiseCommonProblem("P42N1", p.getPackageDeclarations().get(0));
		} else {
			//localClassifierNamespaces.put(p.getIdentifier(), new HashMap<AADLIdentifier, Set<AADLIdentifier>>());
			localClassifierNamespacesWithBindings.put(p.getIdentifier(), new HashMap<AADLIdentifier, Map<AADLIdentifier, IExtendedEObject>>());
			
			if (publicPackageSection != null) {
				// add all classifier identifiers to local public namespace
				EList<AADLDeclaration> declarationsList = publicPackageSection.getDeclarations();
				for (int i = 0; i < declarationsList.size(); i++) {
					
					if (localPublicNamespaces.get(p.getIdentifier()).containsKey(declarationsList.get(i).getIdentifier())) {
						// identifier in the package section is not unique
						if (ComponentTypeImpl.class.isAssignableFrom(declarationsList.get(i).getClass())) {
							// component type name duplicate
							raiseCommonProblem("P43N1", declarationsList.get(i));
						} else if (ComponentImplementationImpl.class.isAssignableFrom(declarationsList.get(i).getClass())) {
							// component implementation name duplicate
							raiseCommonProblem("P44N2", declarationsList.get(i));
						}
					} else {
						localPublicNamespaces.get(p.getIdentifier()).put(declarationsList.get(i).getIdentifier(), declarationsList.get(i));
						classifierNamespaceCreate(declarationsList.get(i), p);
					}
				}
			}
			
			if (privatePackageSection != null) {
				// add all classifier identifiers to local private namespace
				EList<AADLDeclaration> declarationsList = privatePackageSection.getDeclarations();
				for (int i = 0; i < declarationsList.size(); i++) {
					if (localPrivateNamespaces.get(p.getIdentifier()).containsKey(declarationsList.get(i).getIdentifier())) {
						// identifier in the package section is not unique
						if (ComponentTypeImpl.class.isAssignableFrom(declarationsList.get(i).getClass())) {
							// component type name duplicate
							raiseCommonProblem("P43N1", declarationsList.get(i));
						} else if (ComponentImplementationImpl.class.isAssignableFrom(declarationsList.get(i).getClass())) {
							// component implementation name duplicate
							raiseCommonProblem("P44N2", declarationsList.get(i));
						}
					} else {
						localPrivateNamespaces.get(p.getIdentifier()).put(declarationsList.get(i).getIdentifier(), declarationsList.get(i));
						classifierNamespaceCreate(declarationsList.get(i), p);
					}
				}
			}
		}
	}
	
	public static void classifierNamespaceCreate(AADLDeclaration c, PackageImpl p) {
		// element can be either component type or component implementation
		// c.getClass() is concrete component/component implementation type, for example, DataTypeImpl, and we need to check it
		if (ComponentTypeImpl.class.isAssignableFrom(c.getClass())) {
			ComponentTypeImpl componentTypeImpl = (ComponentTypeImpl) c;
			
			// create namespace for a classifier of specified package
			Set<AADLIdentifier> tempSet = new HashSet<AADLIdentifier>();
			Map<AADLIdentifier, IExtendedEObject> tempMap = new HashMap<AADLIdentifier, IExtendedEObject>();
			
			// add all modes names
			EList<Mode> modes = componentTypeImpl.getModes();
			for (int i = 0; i < modes.size(); i++) {
				if (tempSet.contains(modes.get(i).getIdentifier())) {
					raiseCommonProblem("P43N2", modes.get(i));
				} else {
					tempSet.add(modes.get(i).getIdentifier());
					tempMap.put(modes.get(i).getIdentifier(), modes.get(i));
				}
			}
			// add all prototype names
			EList<Prototype> prototypes = componentTypeImpl.getPrototypes();
			for (int i = 0; i < prototypes.size(); i++) {
				if (tempSet.contains(prototypes.get(i).getIdentifier())) {
					raiseCommonProblem("P43N2", prototypes.get(i));
				} else {
					tempSet.add(prototypes.get(i).getIdentifier());
					tempMap.put(prototypes.get(i).getIdentifier(), prototypes.get(i));
				}
			}
			// add all feature names
			EList<Feature> features = componentTypeImpl.getFeatures();
			for (int i = 0; i < features.size(); i++) {
				if (tempSet.contains(features.get(i).getIdentifier())) {
					raiseCommonProblem("P43N2", features.get(i));
				} else {
					tempSet.add(features.get(i).getIdentifier());
					tempMap.put(features.get(i).getIdentifier(), features.get(i));
				}
			}
			// add all mode transitions names
			EList<ModeTransition> modetransitions = componentTypeImpl.getModeTransitions();
			for (int i = 0; i < modetransitions.size(); i++) {
				if (tempSet.contains(modetransitions.get(i).getIdentifier())) {
					raiseCommonProblem("P43N2", modetransitions.get(i));
				} else {
					tempSet.add(modetransitions.get(i).getIdentifier());
					tempMap.put(modetransitions.get(i).getIdentifier(), modetransitions.get(i));
				}
			}
			// add all flow names
			EList<Flow> flows = componentTypeImpl.getFlows();
			for (int i = 0; i < flows.size(); i++) {
				if (tempSet.contains(flows.get(i).getIdentifier())) {
					raiseCommonProblem("P43N2", flows.get(i));
				} else {
					tempSet.add(flows.get(i).getIdentifier());
					tempMap.put(flows.get(i).getIdentifier(), flows.get(i));
				}
			}
			//localClassifierNamespaces.get(p.getIdentifier()).put(c.getIdentifier(), new HashSet<AADLIdentifier>(tempSet));
			localClassifierNamespacesWithBindings.get(p.getIdentifier()).put(c.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>(tempMap));
		} else if (ComponentImplementationImpl.class.isAssignableFrom(c.getClass())) {
			ComponentImplementationImpl componentImplementationImpl = (ComponentImplementationImpl) c;
			if (componentImplementationImpl.getComponentType().eIsProxy()) {
				// component type to which implementation refers does not exist
				raiseCommonProblem("P44N1", componentImplementationImpl);
			} else {
				// create namespace for a classifier of specified package
				Set<AADLIdentifier> tempSet = new HashSet<AADLIdentifier>();
				Map<AADLIdentifier, IExtendedEObject> tempMap = new HashMap<AADLIdentifier, IExtendedEObject>();
				
				// add all modes names
				EList<Mode> modes = componentImplementationImpl.getModes();
				for (int i = 0; i < modes.size(); i++) {
					if (tempSet.contains(modes.get(i).getIdentifier())) {
						raiseCommonProblem("P44N3", modes.get(i));
					} else {
						tempSet.add(modes.get(i).getIdentifier());
						tempMap.put(modes.get(i).getIdentifier(), modes.get(i));
					}
				}
				// add all subcomponents names
				EList<Subcomponent> subcomponents = componentImplementationImpl.getSubcomponents();
				for (int i = 0; i < subcomponents.size(); i++) {
					if (tempSet.contains(subcomponents.get(i).getIdentifier())) {
						raiseCommonProblem("P44N3", subcomponents.get(i));
					} else {
						tempSet.add(subcomponents.get(i).getIdentifier());
						tempMap.put(subcomponents.get(i).getIdentifier(), subcomponents.get(i));
					}
				}
				// add all subprogram calls
				EList<SubprogramCallSequence> calls = componentImplementationImpl.getCalls();
				for (int i = 0; i < calls.size(); i++) {
					if (tempSet.contains(calls.get(i).getIdentifier())) {
						raiseCommonProblem("P44N3", calls.get(i));
					} else {
						tempSet.add(calls.get(i).getIdentifier());
						tempMap.put(calls.get(i).getIdentifier(), calls.get(i));
					}
				}
				// add all connections
				EList<Connection> connections = componentImplementationImpl.getConnections();
				for (int i = 0; i < connections.size(); i++) {
					if (tempSet.contains(connections.get(i).getIdentifier())) {
						raiseCommonProblem("P44N3", connections.get(i));
					} else {
						tempSet.add(connections.get(i).getIdentifier());
						tempMap.put(connections.get(i).getIdentifier(), connections.get(i));
					}
				}
				// add all prototype names
				EList<Prototype> prototypes = componentImplementationImpl.getPrototypes();
				for (int i = 0; i < prototypes.size(); i++) {
					if (tempSet.contains(prototypes.get(i).getIdentifier())) {
						raiseCommonProblem("P44N3", prototypes.get(i));
					} else {
						tempSet.add(prototypes.get(i).getIdentifier());
						tempMap.put(prototypes.get(i).getIdentifier(), prototypes.get(i));
					}
				}
				// add all mode transitions names
				EList<ModeTransition> modetransitions = componentImplementationImpl.getModeTransitions();
				for (int i = 0; i < modetransitions.size(); i++) {
					if (tempSet.contains(modetransitions.get(i).getIdentifier())) {
						raiseCommonProblem("P44N3", modetransitions.get(i));
					} else {
						tempSet.add(modetransitions.get(i).getIdentifier());
						tempMap.put(modetransitions.get(i).getIdentifier(), modetransitions.get(i));
					}
				}
				// add all flow names
				EList<Flow> flows = componentImplementationImpl.getFlows();
				for (int i = 0; i < flows.size(); i++) {
					if (tempSet.contains(flows.get(i).getIdentifier())) {
						raiseCommonProblem("P44N3", flows.get(i));
					} else {
						tempSet.add(flows.get(i).getIdentifier());
						tempMap.put(flows.get(i).getIdentifier(), flows.get(i));
					}
				}
				//localClassifierNamespaces.get(p.getIdentifier()).put(c.getIdentifier(), new HashSet<AADLIdentifier>(tempSet));
				localClassifierNamespacesWithBindings.get(p.getIdentifier()).put(c.getIdentifier(), new HashMap<AADLIdentifier, IExtendedEObject>(tempMap));
			}
		}
	}
}
