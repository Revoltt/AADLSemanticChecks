package ru.ispras.masiw.plugin.aadl.semantic.support;

import ru.ispras.antlr.v4.editing.core.runtime.IExtendedEObject;
import ru.ispras.masiw.plugin.aadl.model.AADLIdentifier;

@FunctionalInterface
public interface Usefull {
	public void doWork(IExtendedEObject p, IExtendedEObject c, IExtendedEObject subc, AADLIdentifier additional);
}
