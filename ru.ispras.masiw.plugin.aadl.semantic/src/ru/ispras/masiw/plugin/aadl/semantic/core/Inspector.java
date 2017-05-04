package ru.ispras.masiw.plugin.aadl.semantic.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;

import ru.ispras.masiw.plugin.aadl.metamodel.Connection;
import ru.ispras.masiw.plugin.aadl.metamodel.Feature;
import ru.ispras.masiw.plugin.aadl.metamodel.Flow;
import ru.ispras.masiw.plugin.aadl.metamodel.Mode;
import ru.ispras.masiw.plugin.aadl.metamodel.ModeTransition;
import ru.ispras.masiw.plugin.aadl.metamodel.Prototype;
import ru.ispras.masiw.plugin.aadl.metamodel.Subcomponent;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AADLDeclaration;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AllClassifiersAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.Annex;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.ClassifierAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageAlias;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageSection;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PropertyAssociation;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.SubprogramCallSequence;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.impl.PropertySetImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentImplementationImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentTypeImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;
import ru.ispras.masiw.plugin.aadl.model.domain.AADLSpecificationDomain;
import ru.ispras.masiw.plugin.aadl.semantic.support.Usefull;

public class Inspector {
	public static HashMap<String, ArrayList<Usefull>> funcs;
	public static void inspectModel(AADLSpecificationDomain model, HashMap<String, ArrayList<Usefull>> f) {
		funcs = f;
		TreeIterator<EObject> x = model.getAADLPackagesResource().getAllContents();
		// list of packages and property sets
		ArrayList<EObject> l = new ArrayList<EObject>();
		while (x.hasNext()) {
			l.add(x.next());
		}
		
		for (int i = 0; i < l.size(); i++) {
			if ((l.get(i)).getClass().equals(PackageImpl.class)) {
				
				PackageImpl curPackage = (PackageImpl) l.get(i);
				
				if (funcs.containsKey("prePackageInspect"))
					for (int j = 0; j < funcs.get("prePackageInspect").size(); j++) {
						funcs.get("prePackageInspect").get(j).doWork(curPackage, null, null, null);
					}
				
				inspectPackage(curPackage);
			} else if ((l.get(i)).getClass().equals(PropertySetImpl.class)) {
				PropertySetImpl curPrSet = (PropertySetImpl) l.get(i);
				
				if (funcs.containsKey("prePrSetInspect"))
					for (int j = 0; j < funcs.get("prePrSetInspect").size(); j++) {
						funcs.get("prePrSetInspect").get(j).doWork(curPrSet, null, null, null);
					}
				
				inspectPrSet(curPrSet);
			}	
		}
	}
	
	private static void inspectPackage(PackageImpl p) {
		PackageSection publicPackageSection = p.getPublicSection();
		PackageSection privatePackageSection = p.getPrivateSection();
		
		if (publicPackageSection != null) {
			EList<AADLIdentifier> imports = publicPackageSection.getImports();
			for (int i = 0; i < imports.size(); i++) {
				if (funcs.containsKey("publicPackageImport"))
					for (int j = 0; j < funcs.get("publicPackageImport").size(); j++) {
						funcs.get("publicPackageImport").get(j).doWork(p, publicPackageSection, null, imports.get(i)); 
						// TODO send AADLIdentifier
					}
			}
			
			EList<PackageAlias> packageAliases = publicPackageSection.getPackageAliases();
			for (int i = 0; i < packageAliases.size(); i++) {
				if (funcs.containsKey("publicPackageAlias"))
					for (int j = 0; j < funcs.get("publicPackageAlias").size(); j++) {
						funcs.get("publicPackageAlias").get(j).doWork(p, packageAliases.get(i), null, null);
					}
			}
			
			EList<ClassifierAlias> classifierAliases = publicPackageSection.getClassifierAliases();
			for (int i = 0; i < classifierAliases.size(); i++) {
				if (funcs.containsKey("publicClassifierAlias"))
					for (int j = 0; j < funcs.get("publicClassifierAlias").size(); j++) {
						funcs.get("publicClassifierAlias").get(j).doWork(p, classifierAliases.get(i), null, null);
					}
			}
			
			EList<AllClassifiersAlias> allAliases = publicPackageSection.getAllClassifiersAliases();
			for (int i = 0; i < allAliases.size(); i++) {
				if (funcs.containsKey("publicAllAlias"))
					for (int j = 0; j < funcs.get("publicAllAlias").size(); j++) {
						funcs.get("publicAllAlias").get(j).doWork(p, allAliases.get(i), null, null);
					}
			}
			
			EList<AADLDeclaration> declarationsList = publicPackageSection.getDeclarations();
			for (int i = 0; i < declarationsList.size(); i++) {
				if (funcs.containsKey("publicPreComponentInspect"))
					for (int j = 0; j < funcs.get("publicPreComponentInspect").size(); j++) {
						funcs.get("publicPreComponentInspect").get(j).doWork(p, declarationsList.get(i), null, null);
					}
				componentInspect(declarationsList.get(i), p);
			}
		}
		
		if (privatePackageSection != null) {
			EList<AADLIdentifier> imports = privatePackageSection.getImports();
			for (int i = 0; i < imports.size(); i++) {
				if (funcs.containsKey("privatePackageImport"))
					for (int j = 0; j < funcs.get("privatePackageImport").size(); j++) {
						funcs.get("privatePackageImport").get(j).doWork(p, null, null, null); // TODO add AADLIdentifier
					}
			}
			
			EList<PackageAlias> packageAliases = privatePackageSection.getPackageAliases();
			for (int i = 0; i < packageAliases.size(); i++) {
				if (funcs.containsKey("privatePackageAlias"))
					for (int j = 0; j < funcs.get("privatePackageAlias").size(); j++) {
						funcs.get("privatePackageAlias").get(j).doWork(p, packageAliases.get(i), null, null);
					}
			}
			
			EList<ClassifierAlias> classifierAliases = privatePackageSection.getClassifierAliases();
			for (int i = 0; i < classifierAliases.size(); i++) {
				if (funcs.containsKey("privateClassifierAlias"))
					for (int j = 0; j < funcs.get("privateClassifierAlias").size(); j++) {
						funcs.get("privateClassifierAlias").get(j).doWork(p, classifierAliases.get(i), null, null);
					}
			}
			
			EList<AllClassifiersAlias> allAliases = privatePackageSection.getAllClassifiersAliases();
			for (int i = 0; i < allAliases.size(); i++) {
				if (funcs.containsKey("privateAllAlias"))
					for (int j = 0; j < funcs.get("privateAllAlias").size(); j++) {
						funcs.get("privateAllAlias").get(j).doWork(p, allAliases.get(i), null, null);
					}
			}
			
			EList<AADLDeclaration> declarationsList = privatePackageSection.getDeclarations();
			for (int i = 0; i < declarationsList.size(); i++) {
				if (funcs.containsKey("privatePreComponentInspect"))	
					for (int j = 0; j < funcs.get("privatePreComponentInspect").size(); j++) {
						funcs.get("privatePreComponentInspect").get(j).doWork(p, declarationsList.get(i), null, null);
					}
				componentInspect(declarationsList.get(i), p);
			}
		}
	}
	
	private static void componentInspect(AADLDeclaration c, PackageImpl p) {
		if (ComponentTypeImpl.class.isAssignableFrom(c.getClass())) {
			// component type
			ComponentTypeImpl componentTypeImpl = (ComponentTypeImpl) c;
			
			if (funcs.containsKey("preComponentType"))	
				for (int j = 0; j < funcs.get("preComponentType").size(); j++) {
					funcs.get("preComponentType").get(j).doWork(p, c, null, null);
				}
			
			EList<Mode> modes = componentTypeImpl.getModes();
			for (int i = 0; i < modes.size(); i++) {
				if (funcs.containsKey("modes"))	
					for (int j = 0; j < funcs.get("modes").size(); j++) {
						funcs.get("modes").get(j).doWork(p, c, modes.get(i), null);
					}
			}
			
			EList<Prototype> prototypes = componentTypeImpl.getPrototypes();
			for (int i = 0; i < prototypes.size(); i++) {
				if (funcs.containsKey("prototypes"))	
					for (int j = 0; j < funcs.get("prototypes").size(); j++) {
						funcs.get("prototypes").get(j).doWork(p, c, prototypes.get(i), null);
					}
			}
			
			EList<Feature> features = componentTypeImpl.getFeatures();
			for (int i = 0; i < features.size(); i++) {
				if (funcs.containsKey("features"))	
					for (int j = 0; j < funcs.get("features").size(); j++) {
						funcs.get("features").get(j).doWork(p, c, features.get(i), null);
					}
			}
			
			EList<ModeTransition> modetransitions = componentTypeImpl.getModeTransitions();
			for (int i = 0; i < modetransitions.size(); i++) {
				if (funcs.containsKey("modetransitions"))	
					for (int j = 0; j < funcs.get("modetransitions").size(); j++) {
						funcs.get("modetransitions").get(j).doWork(p, c, modetransitions.get(i), null);
					}
			}
			
			EList<Flow> flows = componentTypeImpl.getFlows();
			for (int i = 0; i < flows.size(); i++) {
				if (funcs.containsKey("flows"))	
					for (int j = 0; j < funcs.get("flows").size(); j++) {
						funcs.get("flows").get(j).doWork(p, c, flows.get(i), null);
					}
			}
			
			EList<Annex> annexes = componentTypeImpl.getAnnexes();
			for (int i = 0; i < annexes.size(); i++) {
				if (funcs.containsKey("annexes"))	
					for (int j = 0; j < funcs.get("annexes").size(); j++) {
						funcs.get("annexes").get(j).doWork(p, c, annexes.get(i), null);
					}
			}
			
			EList<PropertyAssociation> properties = componentTypeImpl.getProperties();
			for (int i = 0; i < properties.size(); i++) {
				if (funcs.containsKey("properties"))	
					for (int j = 0; j < funcs.get("properties").size(); j++) {
						funcs.get("properties").get(j).doWork(p, c, properties.get(i), null);
					}
			}
		} else if (ComponentImplementationImpl.class.isAssignableFrom(c.getClass())) {
			// component implementation
			ComponentImplementationImpl componentImplementationImpl = (ComponentImplementationImpl) c;
			
			if (funcs.containsKey("preComponentImpl"))	
				for (int j = 0; j < funcs.get("preComponentImpl").size(); j++) {
					funcs.get("preComponentImpl").get(j).doWork(p, c, null, null);
				}
			
			EList<Mode> modes = componentImplementationImpl.getModes();
			for (int i = 0; i < modes.size(); i++) {
				if (funcs.containsKey("modesImpl"))	
					for (int j = 0; j < funcs.get("modesImpl").size(); j++) {
						funcs.get("modesImpl").get(j).doWork(p, c, modes.get(i), null);
					}
			}
			
			EList<Subcomponent> subcomponents = componentImplementationImpl.getSubcomponents();
			for (int i = 0; i < subcomponents.size(); i++) {
				if (funcs.containsKey("subcomponentsImpl"))	
					for (int j = 0; j < funcs.get("subcomponentsImpl").size(); j++) {
						funcs.get("subcomponentsImpl").get(j).doWork(p, c, subcomponents.get(i), null);
					}
			}
			
			EList<Prototype> prototypes = componentImplementationImpl.getPrototypes();
			for (int i = 0; i < prototypes.size(); i++) {
				if (funcs.containsKey("prototypesImpl"))	
					for (int j = 0; j < funcs.get("prototypesImpl").size(); j++) {
						funcs.get("prototypesImpl").get(j).doWork(p, c, prototypes.get(i), null);
					}
			}
			
			EList<SubprogramCallSequence> calls = componentImplementationImpl.getCalls();
			for (int i = 0; i < calls.size(); i++) {
				if (funcs.containsKey("callsImpl"))	
					for (int j = 0; j < funcs.get("callsImpl").size(); j++) {
						funcs.get("callsImpl").get(j).doWork(p, c, calls.get(i), null);
					}
			}
			
			EList<Connection> connections = componentImplementationImpl.getConnections();
			for (int i = 0; i < connections.size(); i++) {
				if (funcs.containsKey("connectionsImpl"))	
					for (int j = 0; j < funcs.get("connectionsImpl").size(); j++) {
						funcs.get("connectionsImpl").get(j).doWork(p, c, connections.get(i), null);
					}
			}
			
			EList<ModeTransition> modetransitions = componentImplementationImpl.getModeTransitions();
			for (int i = 0; i < modetransitions.size(); i++) {
				if (funcs.containsKey("modetransitionsImpl"))	
					for (int j = 0; j < funcs.get("modetransitionsImpl").size(); j++) {
						funcs.get("modetransitionsImpl").get(j).doWork(p, c, modetransitions.get(i), null);
					}
			}
			
			EList<Flow> flows = componentImplementationImpl.getFlows();
			for (int i = 0; i < flows.size(); i++) {
				if (funcs.containsKey("flowsImpl"))	
					for (int j = 0; j < funcs.get("flowsImpl").size(); j++) {
						funcs.get("flowsImpl").get(j).doWork(p, c, flows.get(i), null);
					}
			}
			
			EList<Annex> annexes = componentImplementationImpl.getAnnexes();
			for (int i = 0; i < annexes.size(); i++) {
				if (funcs.containsKey("annexesImpl"))	
					for (int j = 0; j < funcs.get("annexesImpl").size(); j++) {
						funcs.get("annexesImpl").get(j).doWork(p, c, annexes.get(i), null);
					}
			}
			
			EList<PropertyAssociation> properties = componentImplementationImpl.getProperties();
			for (int i = 0; i < properties.size(); i++) {
				if (funcs.containsKey("propertiesImpl"))	
					for (int j = 0; j < funcs.get("propertiesImpl").size(); j++) {
						funcs.get("propertiesImpl").get(j).doWork(p, c, properties.get(i), null);
					}
			}
		}
	}

	private static void inspectPrSet(PropertySetImpl p) {
		// TODO Auto-generated method stub
		
	}
}