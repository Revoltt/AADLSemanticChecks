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
import ru.ispras.masiw.plugin.aadl.metamodel.NamedElement;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AADLDeclaration;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.ClassifierAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PropertyAssociation;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.impl.PropertySetImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.AbstractTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.BusTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.DataTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.DeviceTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.MemoryTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ProcessTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ProcessorTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ProvidesSubprogramAccessImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.SubprogramGroupTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.SubprogramTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ThreadGroupTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ThreadTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.VirtualBusTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.VirtualProcessorTypeImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.model.domain.AADLSpecificationDomain;

public class Check {
	public static ProblemHelper helper;
	// namespace for checking existence of packages and property sets
	public static Set<AADLIdentifier> globalPackageNamespace; 
	public static Set<AADLIdentifier> globalPrSetNamespace; 
	// for each package identifier a local namespace of it's public and private classifiers
	public static Map<AADLIdentifier, Set<AADLIdentifier>> localPublicNamespaces;
	public static Map<AADLIdentifier, Set<AADLIdentifier>> localPrivateNamespaces;
	
	// function to check if category type and referenced component type of alias_declaration match each other
	private static boolean typeMatch(String categoryType, String classifierType) {
		if (classifierType.startsWith(categoryType)) {
			return true;
		}
		return false;
	}
		
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
			
	public static void declarativeChecksDummy(ProblemHelper h, AADLSpecificationDomain model) {
		TreeIterator<EObject> x = model.getAADLPackagesResource().getAllContents();
		ArrayList<EObject> l = new ArrayList<EObject>();
		helper = h;
		while (x.hasNext()) {
			l.add(x.next()); // list of packages
		}
		// check whether there are a package and a property set with the same identifier
		globalPackageNamespace = new HashSet<AADLIdentifier>();
		globalPrSetNamespace = new HashSet<AADLIdentifier>();
		localPublicNamespaces = new HashMap<AADLIdentifier, Set<AADLIdentifier>>();
		localPrivateNamespaces = new HashMap<AADLIdentifier, Set<AADLIdentifier>>();
		
		for (int i = 0; i < l.size(); i++) {
			if ((l.get(i)).getClass().equals(PackageImpl.class)) {
				// local namespace is created for every package
				PackageImpl curPackage = (PackageImpl) l.get(i);
				
				if (globalPackageNamespace.contains(curPackage.getIdentifier())) {
					// duplicate package name
					raiseCommonProblem("P41N1", curPackage);
				} else {
					globalPackageNamespace.add(curPackage.getIdentifier());
					localPublicNamespaces.put(curPackage.getIdentifier(), new HashSet<AADLIdentifier>());
					if (curPackage.getPublicSection() != null) {
						EList<AADLDeclaration> declarations = curPackage.getPublicSection().getDeclarations();
						for (int j = 0; j < declarations.size(); j++) {
							if (!localPublicNamespaces.get(curPackage.getIdentifier()).contains(declarations.get(j).getIdentifier())) {
								localPublicNamespaces.get(curPackage.getIdentifier()).add(declarations.get(j).getIdentifier());
							} else {
								// duplicate identifier error
								if (ComponentTypeImpl.class.isAssignableFrom(declarations.get(j).getClass())) {
									// duplicate component type
									raiseCommonProblem("P43N1", declarations.get(j));
								}
							}
						}
						//localPublicNamespaces.get(curPackage.getIdentifier()).addAll(curPackage.getPublicSection().getDeclarationsMap().keySet());
					}
					
					localPrivateNamespaces.put(curPackage.getIdentifier(), new HashSet<AADLIdentifier>());
					if (curPackage.getPrivateSection() != null) {
						EList<AADLDeclaration> declarations = curPackage.getPrivateSection().getDeclarations();
						for (int j = 0; j < declarations.size(); j++) {
							if (!localPrivateNamespaces.get(curPackage.getIdentifier()).contains(declarations.get(j).getIdentifier())) {
								localPrivateNamespaces.get(curPackage.getIdentifier()).add(declarations.get(j).getIdentifier());
							} else {
								// duplicate identifier error
								if (ComponentTypeImpl.class.isAssignableFrom(declarations.get(j).getClass())) {
									// duplicate component type
									raiseCommonProblem("P43N1", declarations.get(j));
								}
							}
						}
						//localPrivateNamespaces.get(curPackage.getIdentifier()).addAll(curPackage.getPrivateSection().getDeclarationsMap().keySet());
					}
				}
			} else if ((l.get(i)).getClass().equals(PropertySetImpl.class)) {
				// local namespace is created for every property set
				PropertySetImpl curPrSet = (PropertySetImpl) l.get(i);
				
				if (globalPrSetNamespace.contains(curPrSet.getIdentifier())) {
					raiseCommonProblem("P41N1", curPrSet);
				} else {
					globalPrSetNamespace.add(curPrSet.getIdentifier());
				}
			}
		}
		
		// by now global namespace and all package local namespaces are created
		// local namespaces do not contain alias identifiers
		for (int i = 0; i < l.size(); i++) {
			if ((l.get(i)).getClass().equals(PackageImpl.class)) {
				// DFS is called for every package
				PackageImpl curPackage = (PackageImpl) l.get(i);
				DFS((EObject) curPackage);
			} else if ((l.get(i)).getClass().equals(PropertySetImpl.class)) {
				// DFS is called for every property set
				PropertySetImpl curPrSet = (PropertySetImpl) l.get(i);
				DFS((EObject) curPrSet);
			}
		}
	}
	
	private static void DFS(EObject p) {
		if (p.getClass().equals(PackageImpl.class)) {
			// for Package declarations
			
			// count amount of sections
			PackageImpl curPackage = ((PackageImpl) p);
			int publicDeclarations = 0, privateDeclarations = 0; // amounts of public and private package declarations
			for (int i = 0; i < curPackage.getPackageDeclarations().size(); i++) {
				if (curPackage.getPackageDeclarations().get(i).getPrivateSection() != null) {
					privateDeclarations = privateDeclarations + 1;
				}
				if (curPackage.getPackageDeclarations().get(i).getPublicSection() != null) {
					publicDeclarations = publicDeclarations + 1;
				}
			}
			
			// only one public and one private package declaration is allowed
			if ((publicDeclarations > 1) || (privateDeclarations > 1)) {
				raiseCommonProblem("P42N1", curPackage.getPackageDeclarations().get(0));
			} else {
				// public section
				if (curPackage.getPublicSection() != null) {
					// check that imports should contain existing packages and/or property sets
					// TODO check if this works for property sets as good as for packages
					EList<AADLIdentifier> imports = curPackage.getPublicSection().getImports();
					// set of package identifiers imported by this package
					Set<AADLIdentifier> importsSet = new HashSet<AADLIdentifier>(); 
					for (int i = 0; i < imports.size(); i++) {
						importsSet.add(imports.get(i));
						if (!globalPackageNamespace.contains(imports.get(i)) && !globalPrSetNamespace.contains(imports.get(i))) {
							raiseCommonProblem("P42N7", curPackage.getPublicSection());
						}
					}
					
					// check that aliases should rename existing objects
					EList<ClassifierAlias> classifierAliases = curPackage.getPublicSection().getClassifierAliases();
					EList<PackageAlias> packageAliases = curPackage.getPublicSection().getPackageAliases();
					
					for (int i = 0; i < packageAliases.size(); i++) {
						if (packageAliases.get(i).eIsProxy()) {
							// the package is not found in namespace - there is proxy instead
							String reference = packageAliases.get(i).eProxyURI().fragment();
							
							// package is referenced
							if (!globalPackageNamespace.contains(new AADLIdentifier(reference))) {
								// package name referenced is not found in global namespace
								raiseCommonProblem("P42N11g", packageAliases.get(i));
							} else if (!importsSet.contains(new AADLIdentifier(reference))) {
								// package referenced is not imported
								raiseCommonProblem("P42N11i", packageAliases.get(i));
							}
						} else {
							if (importsSet.contains(packageAliases.get(i).getIdentifier())) {
								// alias conflicts with imported package names
								raiseCommonProblem("P42N14p", packageAliases.get(i));
							}
							if (curPackage.getIdentifier().equals(packageAliases.get(i).getIdentifier())) {
								// alias conflicts with package name where it is defined
								raiseCommonProblem("P42N14d", packageAliases.get(i));
							}
						}
					}
					
					for (int i = 0; i < classifierAliases.size(); i++) {	
						if (classifierAliases.get(i).getClassifier().eIsProxy()) {
							// the object is not found in namespace - there is proxy instead
							String reference = classifierAliases.get(i).getClassifier().eProxyURI().fragment();
							// classifier of feature group type is referenced
							if (!reference.contains("::")) {
								raiseCommonProblem("P42N12o", classifierAliases.get(i));
							} else {
								String packageName = reference.substring(0, reference.lastIndexOf("::"));
								String identifier = reference.substring(reference.lastIndexOf("::") + 2);
								//System.out.println(packageName + " " + identifier);
								if (!globalPackageNamespace.contains(new AADLIdentifier(packageName))) {
									// package name referenced is not found in global namespace
									raiseCommonProblem("P42N11g", classifierAliases.get(i));
								} else if (!importsSet.contains(new AADLIdentifier(packageName))) {
									// package referenced is not imported
									raiseCommonProblem("P42N11i", classifierAliases.get(i));
								} else if (!localPublicNamespaces.get(new AADLIdentifier(packageName))
										.contains(new AADLIdentifier(identifier))) {
									// classifier referenced is not found in package
									raiseCommonProblem("P42N12", classifierAliases.get(i));
								}
							}
						} else {
							// check that alias name is not used in package local namespace
							if (localPublicNamespaces.get(curPackage.getIdentifier()).contains(classifierAliases.get(i).getIdentifier())) {
								// alias identifier is not unique by some other way
								raiseCommonProblem("P42N14o", classifierAliases.get(i));
							}
							// add identifier of alias
							localPublicNamespaces.get(curPackage.getIdentifier()).add(classifierAliases.get(i).getIdentifier());
							
							if (!typeMatch(classifierAliases.get(i).getCategory().getName(), classifierAliases.get(i).getClassifier().getClass().getSimpleName())) {
								// types do not match
								raiseCommonProblem("P42L4", classifierAliases.get(i));
							}
						}
					}
					// go through all component declarations
					EList<AADLDeclaration> publicDeclarationsList = curPackage.getPublicSection().getDeclarations();
					for (int i = 0; i < publicDeclarationsList.size(); i++) {
						DFS((EObject) publicDeclarationsList.get(i));
					}
				}
				
				// private section
				if (curPackage.getPrivateSection() != null) {
					// check that imports should contain existing packages and/or property sets
					// TODO check if this works for property sets as good as for packages
					EList<AADLIdentifier> imports = curPackage.getPrivateSection().getImports();
					for (int i = 0; i < imports.size(); i++) {
						if (!globalPackageNamespace.contains(imports.get(i)) && !globalPrSetNamespace.contains(imports.get(i))) {
							raiseCommonProblem("P42N7", curPackage.getPrivateSection());
						}
					}
					
					//TODO alias checking not implemented
					
					// go through all component declarations
					EList<AADLDeclaration> privateDeclararionsList = curPackage.getPrivateSection().getDeclarations();
					for (int i = 0; i < privateDeclararionsList.size(); i++) {
						DFS((EObject) privateDeclararionsList.get(i));
					}
				}
			}
		} else if (ComponentTypeImpl.class.isAssignableFrom(p.getClass())) {
			//TODO general component type checks (p43) Are there any??
			if (p.getClass().equals(DataTypeImpl.class)) {
				// for Data type components
				DataTypeImpl componentTypeImpl = ((DataTypeImpl) p);
				
				if (componentTypeImpl.getAncestor() != null) {
					// component which is extended should exist
					// TODO check if referenced package is in import declaration
					NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
					if (ancestor.eIsProxy()) {
						// the ancestor is not found in namespace - there is proxy instead
						raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
					} else if (!(ancestor.getClass().equals(DataTypeImpl.class)
							|| ancestor.getClass().equals(AbstractTypeImpl.class))) {
						raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
					} else if ((((DataTypeImpl) ancestor).getModes() != null) && (componentTypeImpl.getModes() != null) &&
							(((DataTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
						raiseCommonProblem("P43L6", componentTypeImpl);
					}
				}
				// go trough all properties
				EList<PropertyAssociation> properties = componentTypeImpl.getProperties();
				for (int i = 0; i < properties.size(); i++) {
					DFS((EObject) properties.get(i));
				}
				
				// features: provides subprogram access allowed, others supposed not
				EList<Feature> features = componentTypeImpl.getFeatures();
				for (int i = 0; i < features.size(); i++) {
					if (!features.get(i).getClass().equals(ProvidesSubprogramAccessImpl.class)) {
						raiseCommonProblem("P51L1", features.get(i));
					}
					DFS((EObject) features.get(i));
				}
				
				// modes are not allowed
				EList<Mode> modes = componentTypeImpl.getModes();
				if (modes.size() != 0) {
					raiseCommonProblem("P51L2m", modes.get(0));
				}
				
				// flows are not allowed
				// TODO are end-to-end flows possible?
				EList<Flow> flows = componentTypeImpl.getFlows();
				if (flows.size() != 0) {
					raiseCommonProblem("P51L2f", flows.get(0));
				}
			} else if (p.getClass().equals(SubprogramTypeImpl.class)) {
				SubprogramTypeImpl componentTypeImpl = ((SubprogramTypeImpl) p);
				
				if (componentTypeImpl.getAncestor() != null) {
					// component which is extended should exist
					// TODO check if referenced package is in import declaration
					NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
					if (componentTypeImpl.getAncestor().getPrototypeOrClassifier().eIsProxy()) {
						// the ancestor is not found in namespace - there is proxy instead
						raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
					} else if (!(componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(SubprogramTypeImpl.class)
							|| componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(AbstractTypeImpl.class))) {
						raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
					} else if ((((SubprogramTypeImpl) ancestor).getModes() != null) && (componentTypeImpl.getModes() != null) &&
							(((SubprogramTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
						raiseCommonProblem("P43L6", componentTypeImpl);
					}
				}
			} else if (p.getClass().equals(SubprogramGroupTypeImpl.class)) {
				SubprogramGroupTypeImpl componentTypeImpl = ((SubprogramGroupTypeImpl) p);
				
				if (componentTypeImpl.getAncestor() != null) {
					// component which is extended should exist
					// TODO check if referenced package is in import declaration
					NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
					if (componentTypeImpl.getAncestor().getPrototypeOrClassifier().eIsProxy()) {
						// the ancestor is not found in namespace - there is proxy instead
						raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
					} else if (!(componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(SubprogramGroupTypeImpl.class)
							|| componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(AbstractTypeImpl.class))) {
						raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
					} else if ((((SubprogramGroupTypeImpl) ancestor).getModes() != null) && (componentTypeImpl.getModes() != null) &&
							(((SubprogramGroupTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
						raiseCommonProblem("P43L6", componentTypeImpl);
					}
				}
			} else if (p.getClass().equals(ThreadTypeImpl.class)) {
				ThreadTypeImpl componentTypeImpl = ((ThreadTypeImpl) p);
				
				if (componentTypeImpl.getAncestor() != null) {
					// component which is extended should exist
					// TODO check if referenced package is in import declaration
					NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
					if (componentTypeImpl.getAncestor().getPrototypeOrClassifier().eIsProxy()) {
						// the ancestor is not found in namespace - there is proxy instead
						raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
					} else if (!(componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(ThreadTypeImpl.class)
							|| componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(AbstractTypeImpl.class))) {
						raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
					} else if ((((ThreadTypeImpl) ancestor).getModes() != null) && (componentTypeImpl.getModes() != null) &&
							(((ThreadTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
						raiseCommonProblem("P43L6", componentTypeImpl);
					}
				}
			} else if (p.getClass().equals(ThreadGroupTypeImpl.class)) {
				ThreadGroupTypeImpl componentTypeImpl = ((ThreadGroupTypeImpl) p);
				
				if (componentTypeImpl.getAncestor() != null) {
					// component which is extended should exist
					// TODO check if referenced package is in import declaration
					NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
					if (componentTypeImpl.getAncestor().getPrototypeOrClassifier().eIsProxy()) {
						// the ancestor is not found in namespace - there is proxy instead
						raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
					} else if (!(componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(ThreadGroupTypeImpl.class)
							|| componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(AbstractTypeImpl.class))) {
						raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
					} else if ((((ThreadGroupTypeImpl) ancestor).getModes() != null) && (componentTypeImpl.getModes() != null) &&
							(((ThreadGroupTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
						raiseCommonProblem("P43L6", componentTypeImpl);
					}
				}
			} else if (p.getClass().equals(ProcessTypeImpl.class)) {
				ProcessTypeImpl componentTypeImpl = ((ProcessTypeImpl) p);
				
				if (componentTypeImpl.getAncestor() != null) {
					// component which is extended should exist
					// TODO check if referenced package is in import declaration
					NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
					if (componentTypeImpl.getAncestor().getPrototypeOrClassifier().eIsProxy()) {
						// the ancestor is not found in namespace - there is proxy instead
						raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
					} else if (!(componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(ProcessTypeImpl.class)
							|| componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(AbstractTypeImpl.class))) {
						raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
					} else if ((((ProcessTypeImpl) ancestor).getModes() != null) && (componentTypeImpl.getModes() != null) &&
							(((ProcessTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
						raiseCommonProblem("P43L6", componentTypeImpl);
					}
				}
			} else if (p.getClass().equals(MemoryTypeImpl.class)) {
				MemoryTypeImpl componentTypeImpl = ((MemoryTypeImpl) p);
				
				if (componentTypeImpl.getAncestor() != null) {
					// component which is extended should exist
					// TODO check if referenced package is in import declaration
					NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
					if (componentTypeImpl.getAncestor().getPrototypeOrClassifier().eIsProxy()) {
						// the ancestor is not found in namespace - there is proxy instead
						raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
					} else if (!(componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(MemoryTypeImpl.class)
							|| componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(AbstractTypeImpl.class))) {
						raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
					} else if ((((MemoryTypeImpl) ancestor).getModes() != null) && (componentTypeImpl.getModes() != null) &&
							(((MemoryTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
						raiseCommonProblem("P43L6", componentTypeImpl);
					}
				}
			} else if (p.getClass().equals(ProcessorTypeImpl.class)) {
				ProcessorTypeImpl componentTypeImpl = ((ProcessorTypeImpl) p);
				
				if (componentTypeImpl.getAncestor() != null) {
					// component which is extended should exist
					// TODO check if referenced package is in import declaration
					NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
					if (componentTypeImpl.getAncestor().getPrototypeOrClassifier().eIsProxy()) {
						// the ancestor is not found in namespace - there is proxy instead
						raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
					} else if (!(componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(ProcessorTypeImpl.class)
							|| componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(AbstractTypeImpl.class))) {
						raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
					} else if ((((ProcessorTypeImpl) ancestor).getModes() != null) && (componentTypeImpl.getModes() != null) &&
							(((ProcessorTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
						raiseCommonProblem("P43L6", componentTypeImpl);
					}
				}
			} else if (p.getClass().equals(BusTypeImpl.class)) {
				BusTypeImpl componentTypeImpl = ((BusTypeImpl) p);
				
				if (componentTypeImpl.getAncestor() != null) {
					// component which is extended should exist
					// TODO check if referenced package is in import declaration
					NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
					if (componentTypeImpl.getAncestor().getPrototypeOrClassifier().eIsProxy()) {
						// the ancestor is not found in namespace - there is proxy instead
						raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
					} else if (!(componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(BusTypeImpl.class)
							|| componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(AbstractTypeImpl.class))) {
						raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
					} else if ((((BusTypeImpl) ancestor).getModes() != null) && (componentTypeImpl.getModes() != null) &&
							(((BusTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
						raiseCommonProblem("P43L6", componentTypeImpl);
					}
				}
			} else if (p.getClass().equals(DeviceTypeImpl.class)) {
				DeviceTypeImpl componentTypeImpl = ((DeviceTypeImpl) p);
				
				if (componentTypeImpl.getAncestor() != null) {
					// component which is extended should exist
					// TODO check if referenced package is in import declaration
					NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
					if (componentTypeImpl.getAncestor().getPrototypeOrClassifier().eIsProxy()) {
						// the ancestor is not found in namespace - there is proxy instead
						raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
					} else if (!(componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(DeviceTypeImpl.class)
							|| componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(AbstractTypeImpl.class))) {
						raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
					} else if ((((DeviceTypeImpl) ancestor).getModes() != null) && (componentTypeImpl.getModes() != null) &&
							(((DeviceTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
						raiseCommonProblem("P43L6", componentTypeImpl);
					}
				}
			} else if (p.getClass().equals(VirtualProcessorTypeImpl.class)) {
				VirtualProcessorTypeImpl componentTypeImpl = ((VirtualProcessorTypeImpl) p);
				
				if (componentTypeImpl.getAncestor() != null) {
					// component which is extended should exist
					// TODO check if referenced package is in import declaration
					NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
					if (componentTypeImpl.getAncestor().getPrototypeOrClassifier().eIsProxy()) {
						// the ancestor is not found in namespace - there is proxy instead
						raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
					} else if (!(componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(VirtualProcessorTypeImpl.class)
							|| componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(AbstractTypeImpl.class))) {
						raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
					} else if ((((VirtualProcessorTypeImpl) ancestor).getModes() != null) && (componentTypeImpl.getModes() != null) &&
							(((VirtualProcessorTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
						raiseCommonProblem("P43L6", componentTypeImpl);
					}
				}
			} else if (p.getClass().equals(VirtualBusTypeImpl.class)) {
				VirtualBusTypeImpl componentTypeImpl = ((VirtualBusTypeImpl) p);
				
				if (componentTypeImpl.getAncestor() != null) {
					// component which is extended should exist
					// TODO check if referenced package is in import declaration
					NamedElement ancestor = componentTypeImpl.getAncestor().getPrototypeOrClassifier();
					if (componentTypeImpl.getAncestor().getPrototypeOrClassifier().eIsProxy()) {
						// the ancestor is not found in namespace - there is proxy instead
						raiseCommonProblem("P43N3", componentTypeImpl.getAncestor());
					} else if (!(componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(VirtualBusTypeImpl.class)
							|| componentTypeImpl.getAncestor().getPrototypeOrClassifier().getClass().equals(AbstractTypeImpl.class))) {
						raiseCommonProblem("P43L3", componentTypeImpl.getAncestor());
					} else if ((((VirtualBusTypeImpl) ancestor).getModes() != null) && (componentTypeImpl.getModes() != null) &&
							(((VirtualBusTypeImpl) ancestor).getModes().get(0).isRequires() ^ componentTypeImpl.getModes().get(0).isRequires())) {
						raiseCommonProblem("P43L6", componentTypeImpl);
					}
				}
			}
			// TODO: if branches for different components, for different component containments
		}
	}
}
