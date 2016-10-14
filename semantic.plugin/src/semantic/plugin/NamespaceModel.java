package semantic.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;

import ru.ispras.antlr.v4.editing.core.runtime.CommonProblem;
import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.ProblemHelper;
import ru.ispras.masiw.plugin.aadl.metamodel.Feature;
import ru.ispras.masiw.plugin.aadl.metamodel.Flow;
import ru.ispras.masiw.plugin.aadl.metamodel.Mode;
import ru.ispras.masiw.plugin.aadl.metamodel.ModeTransition;
import ru.ispras.masiw.plugin.aadl.metamodel.NamedElement;
import ru.ispras.masiw.plugin.aadl.metamodel.Prototype;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AADLDeclaration;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AllClassifiersAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.ClassifierAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageSection;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PropertyAssociation;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.impl.PropertySetImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.AbstractTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentImplementationImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.DataTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.model.domain.AADLSpecificationDomain;

public class NamespaceModel {
	public static ProblemHelper helper;
	
	// names of packages and property sets in specification
	public static Set<AADLIdentifier> globalPackageNamespace; 
	public static Set<AADLIdentifier> globalPrSetNamespace; 
	
	// names of package classifiers
	public static Map<AADLIdentifier, Set<AADLIdentifier>> localPublicNamespaces;
	public static Map<AADLIdentifier, Set<AADLIdentifier>> localPrivateNamespaces;
	
	// names of package imports
	public static Map<AADLIdentifier, Set<AADLIdentifier>> localPublicImports;
	public static Map<AADLIdentifier, Set<AADLIdentifier>> localPrivateImports;
	
	// names of package aliases
	public static Map<AADLIdentifier, Set<AADLIdentifier>> localPublicPackageAliases;
	public static Map<AADLIdentifier, Set<AADLIdentifier>> localPrivatePackageAliases;
	
	// names of classifier aliases
	public static Map<AADLIdentifier, Set<AADLIdentifier>> localPublicClassifierAliases;
	public static Map<AADLIdentifier, Set<AADLIdentifier>> localPrivateClassifierAliases;
	
	// names of "all" aliases 
	public static Map<AADLIdentifier, Set<AADLIdentifier>> localPublicAllAliases;
	public static Map<AADLIdentifier, Set<AADLIdentifier>> localPrivateAllAliases;
	
	// names of defining identifiers of classifier
	public static Map<AADLIdentifier, Map<AADLIdentifier, Set<AADLIdentifier>>> localClassifierNamespaces;
	
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
	
	// function to check if category type and referenced component type of alias_declaration match each other
	private static boolean typeMatch(String categoryType, String classifierType) {
		if (classifierType.startsWith(categoryType)) {
			return true;
		}
		return false;
	}
	
	// main function for creating namespace model
	public static void createModel(ProblemHelper h, AADLSpecificationDomain model) {
		TreeIterator<EObject> x = model.getAADLPackagesResource().getAllContents();
		ArrayList<EObject> l = new ArrayList<EObject>();
		helper = h;
		while (x.hasNext()) {
			l.add(x.next()); // list of packages
		}
		
		globalPackageNamespace = new HashSet<AADLIdentifier>();
		globalPrSetNamespace = new HashSet<AADLIdentifier>();
		
		localPublicNamespaces = new HashMap<AADLIdentifier, Set<AADLIdentifier>>();
		localPrivateNamespaces = new HashMap<AADLIdentifier, Set<AADLIdentifier>>();
		
		localPublicImports = new HashMap<AADLIdentifier, Set<AADLIdentifier>>();
		localPrivateImports = new HashMap<AADLIdentifier, Set<AADLIdentifier>>();
		
		localPublicPackageAliases = new HashMap<AADLIdentifier, Set<AADLIdentifier>>();
		localPrivatePackageAliases = new HashMap<AADLIdentifier, Set<AADLIdentifier>>();
		
		localPublicClassifierAliases = new HashMap<AADLIdentifier, Set<AADLIdentifier>>();
		localPrivateClassifierAliases = new HashMap<AADLIdentifier, Set<AADLIdentifier>>();
		
		localPublicAllAliases = new HashMap<AADLIdentifier, Set<AADLIdentifier>>();
		localPrivateAllAliases = new HashMap<AADLIdentifier, Set<AADLIdentifier>>();
		
		localClassifierNamespaces = new HashMap<AADLIdentifier, Map<AADLIdentifier, Set<AADLIdentifier>>>();
		
		
		// create global namespace of packages and property sets
		for (int i = 0; i < l.size(); i++) {
			if ((l.get(i)).getClass().equals(PackageImpl.class)) {
				PackageImpl curPackage = (PackageImpl) l.get(i);
				
				if (globalPackageNamespace.contains(curPackage.getIdentifier()) || globalPrSetNamespace.contains(curPackage.getIdentifier())) {
					// duplicate package name
					raiseCommonProblem("P41N1", curPackage);
				} else {
					globalPackageNamespace.add(curPackage.getIdentifier());
				}
			} else if ((l.get(i)).getClass().equals(PropertySetImpl.class)) {
				PropertySetImpl curPrSet = (PropertySetImpl) l.get(i);
				
				if (globalPackageNamespace.contains(curPrSet.getIdentifier()) || globalPrSetNamespace.contains(curPrSet.getIdentifier())) {
					// duplicate property set name
					raiseCommonProblem("P41N1", curPrSet);
				} else {
					globalPrSetNamespace.add(curPrSet.getIdentifier());
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
			//raiseCommonProblem("P42N1", p.getPackageDeclarations().get(0));
		} else {
			if (publicPackageSection != null) {
				// get imports of public section
				EList<AADLIdentifier> imports = publicPackageSection.getImports();
				for (int i = 0; i < imports.size(); i++) {
					if (!(globalPackageNamespace.contains(imports.get(i)) || globalPrSetNamespace.contains(imports.get(i)))) {
						// import is not present in global namespace
						raiseCommonProblem("P42N7", publicPackageSection);
					} else {
						localPublicImports.get(p.getIdentifier()).add(imports.get(i));
					}
				}
				// get package aliases of public section
				EList<PackageAlias> packageAliases = publicPackageSection.getPackageAliases();
				for (int i = 0; i < packageAliases.size(); i++) {
					if (packageAliases.get(i).eIsProxy()) {
						// the package is not found in namespace - there is proxy instead
						String reference = packageAliases.get(i).eProxyURI().fragment();
						// package is referenced
						if (!globalPackageNamespace.contains(new AADLIdentifier(reference))) {
							// package name referenced is not found in global namespace
							raiseCommonProblem("P42N11global", packageAliases.get(i));
						} else if (!localPublicImports.get(p.getIdentifier()).contains(new AADLIdentifier(reference))) {
							// package referenced is not imported
							raiseCommonProblem("P42N11import", packageAliases.get(i));
						}
					} else {
						// package is present in global namespace
						if (localPublicImports.get(p.getIdentifier()).contains(packageAliases.get(i).getIdentifier())) {
							// alias name is the same as some imported package name
							raiseCommonProblem("P42N14package", packageAliases.get(i));
						}
						if (p.getIdentifier().equals(packageAliases.get(i).getIdentifier())) {
							// alias name is the same as package name where it is defined
							raiseCommonProblem("P42N14defining", packageAliases.get(i));
						}
						if (localPublicNamespaces.get(p.getIdentifier()).contains(packageAliases.get(i).getIdentifier()) || 
								localPublicClassifierAliases.get(p.getIdentifier()).contains(packageAliases.get(i).getIdentifier()) ||
								localPublicPackageAliases.get(p.getIdentifier()).contains(packageAliases.get(i).getIdentifier())) {
							// alias identifier is not unique by some other way
							raiseCommonProblem("P42N14other", packageAliases.get(i));
						}
						// add identifier of package
						localPublicPackageAliases.get(p.getIdentifier()).add(packageAliases.get(i).getIdentifier());
					}
				}
				// get classifier aliases of public section
				EList<ClassifierAlias> classifierAliases = publicPackageSection.getClassifierAliases();
				for (int i = 0; i < classifierAliases.size(); i++) {	
					if (classifierAliases.get(i).getClassifier().eIsProxy()) {
						// the object is not found in namespace - there is proxy instead
						String reference = classifierAliases.get(i).getClassifier().eProxyURI().fragment();
						// classifier of feature group type is referenced
						// TODO check if feature group alias is classifierAlias
						if (!reference.contains("::")) {
							// no package is referenced
							raiseCommonProblem("P42N12other", classifierAliases.get(i));
						} else {
							String packageName = reference.substring(0, reference.lastIndexOf("::"));
							String identifier = reference.substring(reference.lastIndexOf("::") + 2);
							if (!globalPackageNamespace.contains(new AADLIdentifier(packageName))) {
								// package name referenced is not found in global namespace
								raiseCommonProblem("P42N11global", classifierAliases.get(i));
							} else if (!localPublicImports.get(p.getIdentifier()).contains(new AADLIdentifier(packageName))) {
								// package referenced is not imported
								raiseCommonProblem("P42N11import", classifierAliases.get(i));
							} else if (!localPublicNamespaces.get(new AADLIdentifier(packageName)).contains(new AADLIdentifier(identifier))) {
								// classifier referenced is not found in package
								raiseCommonProblem("P42N12", classifierAliases.get(i));
							}
						}
					} else {
						// check that alias name is not used in package local namespace
						AADLIdentifier id = classifierAliases.get(i).getIdentifier();
						if (id == null)
							id = classifierAliases.get(i).getClassifier().getIdentifier();
						if (localPublicNamespaces.get(p.getIdentifier()).contains(id) || 
								localPublicClassifierAliases.get(p.getIdentifier()).contains(id) ||
								localPublicPackageAliases.get(p.getIdentifier()).contains(id)) {
							if (classifierAliases.get(i).getIdentifier() != null) {
								// alias identifier is not unique by some other way
								raiseCommonProblem("P42N14other", classifierAliases.get(i));
							} else {
								// alias identifier is empty
								raiseCommonProblem("P42N15", classifierAliases.get(i));
							}
						}
						// add identifier of alias
						localPublicClassifierAliases.get(p.getIdentifier()).add(classifierAliases.get(i).getIdentifier());
						
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
					} else if (!localPublicImports.get(p.getIdentifier()).contains(allAliases.get(i).getPackage().getIdentifier())) {
						// package is not in import
						raiseCommonProblem("P42N11import", allAliases.get(i));
					} else {
						Set<AADLIdentifier> intersection = new HashSet<AADLIdentifier>(localPublicNamespaces.get(allAliases.get(i).getPackage().getIdentifier()));
						intersection.retainAll(localPublicNamespaces.get(p.getIdentifier()));
						if (!intersection.isEmpty()) {
							raiseCommonProblem("P42N16", allAliases.get(i));
						}
					}
				}
				
				for (int i = 0; i < publicPackageSection.getDeclarations().size(); i++) {
					// check classifier rules
					// TODO check classifier implementation rules
					classifierCheck(publicPackageSection.getDeclarations().get(i), p);
				}
			}
			if (privatePackageSection != null) {
				// get imports of private section
				EList<AADLIdentifier> imports = privatePackageSection.getImports();
				for (int i = 0; i < imports.size(); i++) {
					if (!(globalPackageNamespace.contains(imports.get(i)) || globalPrSetNamespace.contains(imports.get(i)))) {
						// import is not present in global namespace
						raiseCommonProblem("P42N7", privatePackageSection);
					} else {
						localPrivateImports.get(p.getIdentifier()).add(imports.get(i));
					}
				}
				// get package aliases of private section
				EList<PackageAlias> packageAliases = privatePackageSection.getPackageAliases();
				for (int i = 0; i < packageAliases.size(); i++) {
					if (packageAliases.get(i).eIsProxy()) {
						// the package is not found in namespace - there is proxy instead
						String reference = packageAliases.get(i).eProxyURI().fragment();
						// package is referenced
						if (!globalPackageNamespace.contains(new AADLIdentifier(reference))) {
							// package name referenced is not found in global namespace
							raiseCommonProblem("P42N11global", packageAliases.get(i));
						} else if (!localPrivateImports.get(p.getIdentifier()).contains(new AADLIdentifier(reference))) {
							// package referenced is not imported
							raiseCommonProblem("P42N11import", packageAliases.get(i));
						}
					} else {
						// package is present in global namespace
						if (localPrivateImports.get(p.getIdentifier()).contains(packageAliases.get(i).getIdentifier())) {
							// alias name is the same as some imported package name
							raiseCommonProblem("P42N14package", packageAliases.get(i));
						}
						if (p.getIdentifier().equals(packageAliases.get(i).getIdentifier())) {
							// alias name is the same as package name where it is defined
							raiseCommonProblem("P42N14defining", packageAliases.get(i));
						}
						if (localPrivateNamespaces.get(p.getIdentifier()).contains(packageAliases.get(i).getIdentifier()) || 
								localPrivateClassifierAliases.get(p.getIdentifier()).contains(packageAliases.get(i).getIdentifier()) ||
								localPrivatePackageAliases.get(p.getIdentifier()).contains(packageAliases.get(i).getIdentifier())) {
							// alias identifier is not unique by some other way
							raiseCommonProblem("P42N14other", packageAliases.get(i));
						}
						// add identifier of package
						localPrivatePackageAliases.get(p.getIdentifier()).add(packageAliases.get(i).getIdentifier());
					}
				}
				// get classifier aliases of private section
				EList<ClassifierAlias> classifierAliases = privatePackageSection.getClassifierAliases();
				for (int i = 0; i < classifierAliases.size(); i++) {	
					if (classifierAliases.get(i).getClassifier().eIsProxy()) {
						// the object is not found in namespace - there is proxy instead
						String reference = classifierAliases.get(i).getClassifier().eProxyURI().fragment();
						// classifier of feature group type is referenced
						// TODO check if feature group alias is classifierAlias
						if (!reference.contains("::")) {
							// no package is referenced
							raiseCommonProblem("P42N12other", classifierAliases.get(i));
						} else {
							String packageName = reference.substring(0, reference.lastIndexOf("::"));
							String identifier = reference.substring(reference.lastIndexOf("::") + 2);
							if (!globalPackageNamespace.contains(new AADLIdentifier(packageName))) {
								// package name referenced is not found in global namespace
								raiseCommonProblem("P42N11global", classifierAliases.get(i));
							} else if (!localPrivateImports.get(p.getIdentifier()).contains(new AADLIdentifier(packageName))) {
								// package referenced is not imported
								raiseCommonProblem("P42N11import", classifierAliases.get(i));
							} else if (!localPrivateNamespaces.get(new AADLIdentifier(packageName)).contains(new AADLIdentifier(identifier))) {
								// classifier referenced is not found in package
								raiseCommonProblem("P42N12", classifierAliases.get(i));
							}
						}
					} else {
						// check that alias name is not used in package local namespace
						AADLIdentifier id = classifierAliases.get(i).getIdentifier();
						if (id == null)
							id = classifierAliases.get(i).getClassifier().getIdentifier();
						if (localPrivateNamespaces.get(p.getIdentifier()).contains(id) || 
								localPrivateClassifierAliases.get(p.getIdentifier()).contains(id) ||
								localPrivatePackageAliases.get(p.getIdentifier()).contains(id)) {
							if (classifierAliases.get(i).getIdentifier() != null) {
								// alias identifier is not unique by some other way
								raiseCommonProblem("P42N14other", classifierAliases.get(i));
							} else {
								// alias identifier is empty
								raiseCommonProblem("P42N15", classifierAliases.get(i));
							}
						}
						// add identifier of alias
						localPrivateClassifierAliases.get(p.getIdentifier()).add(classifierAliases.get(i).getIdentifier());
						
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
					} else if (!localPublicImports.get(p.getIdentifier()).contains(allAliases.get(i).getPackage().getIdentifier())) {
						// package is not in import
						raiseCommonProblem("P42N11import", allAliases.get(i));
					} else {
						Set<AADLIdentifier> x = localPublicNamespaces.get(allAliases.get(i).getPackage().getIdentifier());
						x.retainAll(localPrivateNamespaces.get(p.getIdentifier()));
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
				NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
				if (ancestor.eIsProxy()) {
					// the ancestor is not found in namespace - there is proxy instead
					raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
				} else if (!(ancestor.getClass().equals(componentTypeImpl.getClass()) || ancestor.getClass().equals(AbstractTypeImpl.class))) {
					// classes classifier and it's ancestor should match
					raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
				} else if ((((ComponentTypeImpl) ancestor).getModes().size() > 0) && (componentTypeImpl.getModes().size() > 0) &&
						(((ComponentTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
					// both modes and requires modes subclauses are not allowed
					raiseCommonProblem("P43L6", componentTypeImpl);
				} 
				// TODO Prototype bindings, refinements
				// TODO check intersection with namespace of ancestor
			}
		} else if (ComponentImplementationImpl.class.isAssignableFrom(c.getClass())) {
			ComponentImplementationImpl componentImplementationImpl = (ComponentImplementationImpl) c;
			// check if there is intersection with local namespace of corresponding component type
			Set<AADLIdentifier> intersection = new HashSet<AADLIdentifier>(localClassifierNamespaces.get(p.getIdentifier()).get(componentImplementationImpl.getIdentifier()));
			intersection.retainAll(localClassifierNamespaces.get(p.getIdentifier()).get(componentImplementationImpl.getComponentType().getIdentifier()));
			if (!intersection.isEmpty()) {
				raiseCommonProblem("P44N4", c);
			}
			// TODO check intersection with namespace of ancestor
			// TODO check other classifier implementation rules
		}
	//	if (p.getClass().equals(DataTypeImpl.class)) {
	//		// for Data type components
	//		DataTypeImpl componentTypeImpl = ((DataTypeImpl) p);	
	//	}
	}
	
	public static void packageNamespaceCreate(PackageImpl p) {
		localPublicNamespaces.put(p.getIdentifier(), new HashSet<AADLIdentifier>());
		localPrivateNamespaces.put(p.getIdentifier(), new HashSet<AADLIdentifier>());
		
		localPublicImports.put(p.getIdentifier(), new HashSet<AADLIdentifier>());
		localPrivateImports.put(p.getIdentifier(), new HashSet<AADLIdentifier>());
		
		localPublicPackageAliases.put(p.getIdentifier(), new HashSet<AADLIdentifier>());
		localPrivatePackageAliases.put(p.getIdentifier(), new HashSet<AADLIdentifier>());
		
		localPublicClassifierAliases.put(p.getIdentifier(), new HashSet<AADLIdentifier>());
		localPrivateClassifierAliases.put(p.getIdentifier(), new HashSet<AADLIdentifier>());
		
		localPublicAllAliases.put(p.getIdentifier(), new HashSet<AADLIdentifier>());
		localPrivateAllAliases.put(p.getIdentifier(), new HashSet<AADLIdentifier>());
		
		
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
			if (publicPackageSection != null) {
				// add all classifier identifiers to local public namespace
				EList<AADLDeclaration> declarationsList = publicPackageSection.getDeclarations();
				localClassifierNamespaces.put(p.getIdentifier(), new HashMap<AADLIdentifier, Set<AADLIdentifier>>());
				for (int i = 0; i < declarationsList.size(); i++) {
					
					if (localPublicNamespaces.get(p.getIdentifier()).contains(declarationsList.get(i).getIdentifier())) {
						// identifier in the package section is not unique
						if (ComponentTypeImpl.class.isAssignableFrom(declarationsList.get(i).getClass())) {
							// component type name duplicate
							raiseCommonProblem("P43N1", declarationsList.get(i));
						} else if (ComponentImplementationImpl.class.isAssignableFrom(declarationsList.get(i).getClass())) {
							// component implementation name duplicate
							raiseCommonProblem("P44N2", declarationsList.get(i));
						}
					} else {
						localPublicNamespaces.get(p.getIdentifier()).add(declarationsList.get(i).getIdentifier());
						classifierNamespaceCreate(declarationsList.get(i), p);
					}
				}
			}
			
			if (privatePackageSection != null) {
				// add all classifier identifiers to local private namespace
				EList<AADLDeclaration> declarationsList = privatePackageSection.getDeclarations();
				localClassifierNamespaces.put(p.getIdentifier(), new HashMap<AADLIdentifier, Set<AADLIdentifier>>());
				for (int i = 0; i < declarationsList.size(); i++) {
					if (localPrivateNamespaces.get(p.getIdentifier()).contains(declarationsList.get(i).getIdentifier())) {
						// identifier in the package section is not unique
						if (ComponentTypeImpl.class.isAssignableFrom(declarationsList.get(i).getClass())) {
							// component type name duplicate
							raiseCommonProblem("P43N1", declarationsList.get(i));
						} else if (ComponentImplementationImpl.class.isAssignableFrom(declarationsList.get(i).getClass())) {
							// component implementation name duplicate
							raiseCommonProblem("P44N2", declarationsList.get(i));
						}
					} else {
						localPrivateNamespaces.get(p.getIdentifier()).add(declarationsList.get(i).getIdentifier());
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
			
			// add all modes names
			EList<Mode> modes = componentTypeImpl.getModes();
			for (int i = 0; i < modes.size(); i++) {
				if (tempSet.contains(modes.get(i).getIdentifier())) {
					raiseCommonProblem("P43N2", modes.get(i));
				} else {
					tempSet.add(modes.get(i).getIdentifier());
				}
			}
			// add all prototype names
			EList<Prototype> prototypes = componentTypeImpl.getPrototypes();
			for (int i = 0; i < prototypes.size(); i++) {
				if (tempSet.contains(prototypes.get(i).getIdentifier())) {
					raiseCommonProblem("P43N2", prototypes.get(i));
				} else {
					tempSet.add(prototypes.get(i).getIdentifier());
				}
			}
			// add all feature names
			EList<Feature> features = componentTypeImpl.getFeatures();
			for (int i = 0; i < features.size(); i++) {
				if (tempSet.contains(features.get(i).getIdentifier())) {
					raiseCommonProblem("P43N2", features.get(i));
				} else {
					tempSet.add(features.get(i).getIdentifier());
				}
			}
			// add all mode transitions names
			EList<ModeTransition> modetransitions = componentTypeImpl.getModeTransitions();
			for (int i = 0; i < modetransitions.size(); i++) {
				if (tempSet.contains(modetransitions.get(i).getIdentifier())) {
					raiseCommonProblem("P43N2", modetransitions.get(i));
				} else {
					tempSet.add(modetransitions.get(i).getIdentifier());
				}
			}
			// add all flow names
			EList<Flow> flows = componentTypeImpl.getFlows();
			for (int i = 0; i < flows.size(); i++) {
				if (tempSet.contains(flows.get(i).getIdentifier())) {
					raiseCommonProblem("P43N2", flows.get(i));
				} else {
					tempSet.add(flows.get(i).getIdentifier());
				}
			}
			localClassifierNamespaces.get(p.getIdentifier()).put(c.getIdentifier(), new HashSet<AADLIdentifier>(tempSet));
		} else if (ComponentImplementationImpl.class.isAssignableFrom(c.getClass())) {
			ComponentImplementationImpl componentImplementationImpl = (ComponentImplementationImpl) c;
			// componentType - what was implemented
			if (componentImplementationImpl.getComponentType().eIsProxy()) {
				// component type to which implementation refers does not exist
				raiseCommonProblem("P44N1", componentImplementationImpl);
			} else {
				// create namespace for a classifier of specified package
				Set<AADLIdentifier> tempSet = new HashSet<AADLIdentifier>();
				
				// add all modes names
				EList<Mode> modes = componentImplementationImpl.getModes();
				for (int i = 0; i < modes.size(); i++) {
					if (tempSet.contains(modes.get(i).getIdentifier())) {
						raiseCommonProblem("P44N3", modes.get(i));
					} else {
						tempSet.add(modes.get(i).getIdentifier());
					}
				}
				// add all prototype names
				EList<Prototype> prototypes = componentImplementationImpl.getPrototypes();
				for (int i = 0; i < prototypes.size(); i++) {
					if (tempSet.contains(prototypes.get(i).getIdentifier())) {
						raiseCommonProblem("P44N3", prototypes.get(i));
					} else {
						tempSet.add(prototypes.get(i).getIdentifier());
					}
				}
				
				// add all mode transitions names
				EList<ModeTransition> modetransitions = componentImplementationImpl.getModeTransitions();
				for (int i = 0; i < modetransitions.size(); i++) {
					if (tempSet.contains(modetransitions.get(i).getIdentifier())) {
						raiseCommonProblem("P44N3", modetransitions.get(i));
					} else {
						tempSet.add(modetransitions.get(i).getIdentifier());
					}
				}
				// add all flow names
				EList<Flow> flows = componentImplementationImpl.getFlows();
				for (int i = 0; i < flows.size(); i++) {
					if (tempSet.contains(flows.get(i).getIdentifier())) {
						raiseCommonProblem("P44N3", flows.get(i));
					} else {
						tempSet.add(flows.get(i).getIdentifier());
					}
				}
				localClassifierNamespaces.get(p.getIdentifier()).put(c.getIdentifier(), new HashSet<AADLIdentifier>(tempSet));
			}
		}
	}
}
