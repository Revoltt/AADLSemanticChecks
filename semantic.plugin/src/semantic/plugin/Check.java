package semantic.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;

import ru.ispras.antlr.v4.editing.core.runtime.CommonProblem;
import ru.ispras.masiw.plugin.aadl.ProblemHelper;
import ru.ispras.masiw.plugin.aadl.metamodel.Feature;
import ru.ispras.masiw.plugin.aadl.metamodel.Flow;
import ru.ispras.masiw.plugin.aadl.metamodel.Mode;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AADLDeclaration;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PropertyAssociation;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.impl.PropertySetImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.DataTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ProvidesSubprogramAccessImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.model.domain.AADLSpecificationDomain;

public class Check {
	public static ProblemHelper helper;
	// namespace for checking existence of packages and property sets
	public static Map<String, Integer> globalNamespace; 
	// for each package/property set identifier a local namespace of it's components
	// to check visibility of components between packages should be filled
	
	public static void declarativeChecksDummy(ProblemHelper h, AADLSpecificationDomain model) {
		TreeIterator<EObject> x = model.getAADLPackagesResource().getAllContents();
		ArrayList<EObject> l = new ArrayList<EObject>();
		helper = h;
		while (x.hasNext()) {
			l.add(x.next()); // list of packages
		}
		// check whether there are a package and a property set with the same identifier
		globalNamespace = new HashMap<String, Integer>();
		
		for (int i = 0; i < l.size(); i++) {
			if ((l.get(i)).getClass().equals(PackageImpl.class)) {
				// DFS is called for every package
				PackageImpl curPackage = (PackageImpl) l.get(i);
				
				if (!globalNamespace.containsKey(curPackage.getIdentifier().toString())) {
					globalNamespace.put(curPackage.getIdentifier().toString(), (Integer) 1);
				}
				
				DFS((EObject) curPackage);
			} else if ((l.get(i)).getClass().equals(PropertySetImpl.class)) {
				// DFS is called for every property set
				PropertySetImpl curPrSet = (PropertySetImpl) l.get(i);
				
				if (globalNamespace.containsKey(curPrSet.getIdentifier().toString())) {
					try {
						CommonProblem problem = helper.createProblem("P41N1");
						curPrSet.getProblems().add(problem);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					globalNamespace.put(curPrSet.getIdentifier().toString(), (Integer) 1);
				}
				DFS((EObject) curPrSet);
			}
		}
	}
	
	public static void DFS(EObject p) {
		//System.out.println(p);
		
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
				try {
					CommonProblem problem = helper.createProblem("P42N1");
					curPackage.getPackageDeclarations().get(0).getProblems().add(problem);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// public section
				if (curPackage.getPublicSection() != null) {
					// check that imports should contain existing packages and/or property sets
					// TODO check if this works for property sets as good as for packages
					EList<AADLIdentifier> imports = curPackage.getPublicSection().getImports();
					for (int i = 0; i < imports.size(); i++) {
						if (!globalNamespace.containsKey(imports.get(i))) {
							try {
								CommonProblem problem = helper.createProblem("P42N7");
								curPackage.getPublicSection().getProblems().add(problem);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
					// go through all component declarations
					EList<AADLDeclaration> publicDecl = curPackage.getPublicSection().getDeclarations();
					for (int i = 0; i < publicDecl.size(); i++) {
						DFS((EObject) publicDecl.get(i));
					}
				}
				
				// private section
				if (curPackage.getPrivateSection() != null) {
					// go through all component declarations
					EList<AADLDeclaration> privateDecl = curPackage.getPrivateSection().getDeclarations();
					for (int i = 0; i < privateDecl.size(); i++) {
						DFS((EObject) privateDecl.get(i));
					}
				}
			}
		} else if (p.getClass().equals(DataTypeImpl.class)) {
			// for Data type components
			DataTypeImpl dataImpl = ((DataTypeImpl) p);
			
			// go trough all properties
			EList<PropertyAssociation> properties = dataImpl.getProperties();
			for (int i = 0; i < properties.size(); i++) {
				DFS((EObject) properties.get(i));
			}
			
			// features: provides subprogram access allowed, others supposed not
			EList<Feature> features = dataImpl.getFeatures();
			for (int i = 0; i < features.size(); i++) {
				if (!features.get(i).getClass().equals(ProvidesSubprogramAccessImpl.class)) {
					try {
						CommonProblem problem = helper.createProblem("P51L1");
						features.get(i).getProblems().add(problem);
						System.out.println("Incorrect feature");
					} catch (Exception e) {
						//TODO Does getProblems().add(problem) try to write in restricted space?
						e.printStackTrace();
					}
				}
				DFS((EObject) features.get(i));
			}
			
			// modes are not allowed
			EList<Mode> modes = dataImpl.getModes();
			if (modes.size() != 0) {
				try {
					CommonProblem problem = helper.createProblem("P51L2m");
					modes.get(0).getProblems().add(problem);
				} catch (Exception e) {
					//TODO Does getProblems().add(problem) try to write in restricted space?
					e.printStackTrace();
				}
			}
			
			// flows are not allowed
			// TODO are end-to-end flows possible?
			EList<Flow> flows = dataImpl.getFlows();
			if (flows.size() != 0) {
				try {
					CommonProblem problem = helper.createProblem("P51L2f");
					flows.get(0).getProblems().add(problem);
				} catch (Exception e) {
					//TODO Does getProblems().add(problem) try to write in restricted space?
					e.printStackTrace();
				}
			}
			
		}
		// TODO: if branches for different components, for different component containments
	}
}
