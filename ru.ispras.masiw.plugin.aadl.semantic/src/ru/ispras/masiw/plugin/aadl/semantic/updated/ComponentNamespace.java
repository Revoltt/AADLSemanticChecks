package ru.ispras.masiw.plugin.aadl.semantic.updated;

import org.eclipse.emf.common.util.EList;

import ru.ispras.masiw.plugin.aadl.metamodel.Connection;
import ru.ispras.masiw.plugin.aadl.metamodel.Feature;
import ru.ispras.masiw.plugin.aadl.metamodel.Flow;
import ru.ispras.masiw.plugin.aadl.metamodel.Mode;
import ru.ispras.masiw.plugin.aadl.metamodel.Prototype;
import ru.ispras.masiw.plugin.aadl.metamodel.Subcomponent;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.AADLDeclaration;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.Annex;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.PropertyAssociation;
import ru.ispras.masiw.plugin.aadl.metamodel.extra.SubprogramCallSequence;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentImplementationImpl;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.ComponentTypeImpl;

public class ComponentNamespace {
	AADLDeclaration src;
	ComponentTypeImpl componentTypeImpl;
	ComponentImplementationImpl componentImplementationImpl;
	
	EList<Mode> modes;
	EList<Prototype> prototypes;
	EList<Feature> features;
	EList<PropertyAssociation> properties;
	EList<Flow> flows;
	EList<Annex> annexes;
	
	EList<Subcomponent> subcomponents;
	EList<SubprogramCallSequence> calls;
	EList<Connection> connections;
	
	ComponentNamespace(AADLDeclaration component) {
		src = component;
		
		if (isType()) {
			modes = componentTypeImpl.getModes();
			properties = componentTypeImpl.getProperties();
			flows = componentTypeImpl.getFlows();
			prototypes = componentTypeImpl.getPrototypes();
			annexes = componentTypeImpl.getAnnexes();
			
			features = componentTypeImpl.getFeatures();
		} else {
			modes = componentImplementationImpl.getModes();
			properties = componentImplementationImpl.getProperties();
			flows = componentImplementationImpl.getFlows();
			prototypes = componentImplementationImpl.getPrototypes();
			annexes = componentImplementationImpl.getAnnexes();
			
			subcomponents = componentImplementationImpl.getSubcomponents();
			calls = componentImplementationImpl.getCalls();
			connections = componentImplementationImpl.getConnections();
		}
	}
	
	boolean isType() {
		if (ComponentTypeImpl.class.isAssignableFrom(src.getClass())) {
			componentTypeImpl = (ComponentTypeImpl) src;
			return true;	
		} else {
			componentImplementationImpl = (ComponentImplementationImpl) src;
			return false;
		}
	}
	
}
