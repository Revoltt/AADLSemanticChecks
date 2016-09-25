import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Main {
	public static void main(String args[]) throws IOException {
		@SuppressWarnings("resource")
		Scanner input = new Scanner(new File("std.txt"));
		PrintWriter output = new PrintWriter(new File("rules.txt"));
		String sectionNumber = "";
		String sectionName = "";
		String ruleNumber = "";
		String ruleID = "";
		String ruleText = "";
		String ruleChecked = "";
		boolean ruleTextFlag = false;
		boolean infFlag = false;
		while (input.hasNextLine()) {
			String s = input.nextLine();
			
			if (s.equals("Foreword")) {
				infFlag = true;
			}
			if (!infFlag) {
				continue;
			}
			
			if ((s.length() > 2) && (s.charAt(0) >= '0') && (s.charAt(0) <= '9') && (s.indexOf('.') > 0) && (s.indexOf('.') < 3) && (Integer.valueOf(s.substring(0, s.indexOf('.'))) > 3)) {
				ruleTextFlag = false;
				sectionNumber = s.substring(0, s.length());
				//ruleID = "p" + sectionNumber.substring(0, sectionNumber.indexOf('.')) + sectionNumber.substring(sectionNumber.indexOf('.') + 1);
				s = input.nextLine();
				s = input.nextLine();
				sectionName = s.substring(0, s.length());
			} else if ((sectionNumber.length() > 0) && (s.length() > 3) && ((s.startsWith("(")) && ((s.charAt(1) == 'N') || (s.charAt(1) == 'L') || (s.charAt(1) == 'C')) && (s.indexOf(')') < 6))) {
				if (ruleTextFlag) {
					System.out.println( sectionNumber + "\t" + sectionName + "\t" + ruleNumber + "\t" + ruleID + "\t" + ruleText + "\t" + ruleChecked);
					output.println(sectionName + "\t" + ruleID + "\t" + ruleText + "\t" + ruleChecked);
				}
				ruleID = "p" + sectionNumber.substring(0, sectionNumber.indexOf('.')) + sectionNumber.substring(sectionNumber.indexOf('.') + 1);
				ruleNumber = s.substring(1, s.indexOf(')'));
				ruleID  = ruleID + ruleNumber.toLowerCase(); 
				ruleTextFlag = true;
				ruleText = s.substring(0, s.length()) + " ";
				//s = input.nextLine();
			} else if (ruleTextFlag && ((s.equals("Standard properties") || s.equals("Semantics") 
					|| s.equals("Runtime Support") || s.equals("Processing Requirements and Permissions")))) {
				ruleTextFlag = false;
				ruleChecked = "NO";
				System.out.println(sectionNumber + "\t" + sectionName + "\t" + ruleNumber + "\t" + ruleID + "\t" + ruleText + "\t" + ruleChecked);
				output.println(sectionName + "\t" + ruleID + "\t" + ruleText + "\t" + ruleChecked);
				ruleID = "p" + sectionNumber.substring(0, sectionNumber.indexOf('.')) + sectionNumber.substring(sectionNumber.indexOf('.') + 1);
			} else if (ruleTextFlag) {
				if (s.length() > 2) {
					ruleText = ruleText + " " + s.substring(0, s.length());
				}
			}
		}
	}
}
