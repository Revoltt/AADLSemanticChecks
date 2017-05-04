package ru.ispras.masiw.plugin.aadl.semantic.updated;

import ru.ispras.masiw.plugin.aadl.metamodel.extra.PackageSection;
import ru.ispras.masiw.plugin.aadl.metamodel.impl.PackageImpl;

public class PackageNamespace {
	PackageImpl src;
	PackageDeclaration publicDeclaration, privateDeclaration;
	
	PackageNamespace(PackageImpl p) {
		src = p;
		
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
			//TODO error
		} else {
			if (publicPackageSection != null)
				publicDeclaration = new PackageDeclaration(publicPackageSection, this, true);
			if (privatePackageSection != null)
				privateDeclaration = new PackageDeclaration(privatePackageSection, this, false);
		}
	}
}
