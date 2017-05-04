package ru.ispras.masiw.plugin.aadl.semantic;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import ru.ispras.eclipse.utils.PlatformLogger;
import ru.ispras.masiw.plugin.aadl.ProblemHelper;
import ru.ispras.masiw.plugin.aadl.model.domain.AADLSpecificationDomain;
import semantic.old.Check;

class ProgressBar implements IRunnableWithProgress {
	  static AADLSpecificationDomain modelD;
	  public void run(IProgressMonitor monitor) {
		  try {
			ProblemHelper helper = new ProblemHelper(Activator.getDefault().getBundle());
			monitor.beginTask("Running declarative model checking", IProgressMonitor.UNKNOWN);
			monitor.subTask("Declarative model checking");
			NamespaceModel.createModel(helper, modelD);
			monitor.done();
			if (monitor.isCanceled())
				PlatformLogger.logErrorAndFail(Activator.getDefault(), "Check Canceled", null);
		} catch (Exception e) {
			PlatformLogger.logErrorAndFail(Activator.getDefault(), "Error while initializing ProblemHelper", e);
		}
		  
	  }
}
