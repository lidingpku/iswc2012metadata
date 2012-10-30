package org.iswc.iswc2012main;

import sw4j.rdf.util.ToolOwl2Java;
import sw4j.util.Sw4jException;

public class TaskImportOntology {
	public static void main(String[] args){

		{
			String szOntologyNamespace = "http://data.semanticweb.org/ns/swc/ontology#";
			String szPackageBaseDir = "java/src";
			String szPackageName = "org.iswc.vocabulary";
			String szJavaClassName = "SWC";
			String szPrefix = "swc";
			boolean bUseJena = true;
			try {
				ToolOwl2Java.genSimpleJavaCode(szOntologyNamespace, szPackageBaseDir, szPackageName, szJavaClassName, szPrefix, bUseJena);
			} catch (Sw4jException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		{
			String szOntologyNamespace = "http://swrc.ontoware.org/ontology#";
			String szPackageBaseDir = "java/src";
			String szPackageName = "org.iswc.vocabulary";
			String szJavaClassName = "SWRC";
			String szPrefix = "swrc";
			boolean bUseJena = true;
			try {
				ToolOwl2Java.genSimpleJavaCode(szOntologyNamespace, szPackageBaseDir, szPackageName, szJavaClassName, szPrefix, bUseJena);
			} catch (Sw4jException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		{
			String szOntologyNamespace = "http://purl.org/ontology/bibo/";
			String szPackageBaseDir = "java/src";
			String szPackageName = "org.iswc.vocabulary";
			String szJavaClassName = "BIBO";
			String szPrefix = "bibo";
			boolean bUseJena = true;
			try {
				ToolOwl2Java.genSimpleJavaCode(szOntologyNamespace, szPackageBaseDir, szPackageName, szJavaClassName, szPrefix, bUseJena);
			} catch (Sw4jException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		{
			String szOntologyNamespace = "http://data-gov.tw.rpi.edu/2009/data-gov-twc.rdf#";
			String szPackageBaseDir = "java/src";
			String szPackageName = "org.iswc.vocabulary";
			String szJavaClassName = "DGTWC";
			String szPrefix = "dgtwc";
			boolean bUseJena = true;
			try {
				ToolOwl2Java.genSimpleJavaCode(szOntologyNamespace, szPackageBaseDir, szPackageName, szJavaClassName, szPrefix, bUseJena);
			} catch (Sw4jException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
