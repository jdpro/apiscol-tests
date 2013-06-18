package fr.ac_versailles.crdp.apiscol.tests.gp2;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

public class NameSpaceTest  extends ApiScolTests{
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}
	@Test
	public void testNameSpace(){
		File tempDir = unzipImsPackage("solution_aqueuse_imsld.zip");
		XmlPage packResponse=postingImsLdFile1(tempDir);
		assertTrue("The pack gives a response", packResponse != null);
		System.out.println("***Checking namespace in the pack response***");
		
		Document document=packResponse.getXmlDocument();
		NodeList resources=document.getElementsByTagNameNS("*","resources");
		assertTrue("the <resources> tag should exist",resources.getLength()>0);
		assertTrue("the <resources> namespace should be: http://www.crdp.ac-versailles.fr/2012/apiscol not: "+resources.item(0).getNamespaceURI(),resources.item(0).getNamespaceURI().equals("http://www.crdp.ac-versailles.fr/2012/apiscol")) ;
		
		NodeList resource=document.getElementsByTagNameNS("*", "resource");
		assertTrue("the <resource> tag should exist",resource.getLength()>0);
		assertTrue("the <resource> namespace should be: http://www.crdp.ac-versailles.fr/2012/apiscol not: "+resources.item(0).getNamespaceURI(),resources.item(0).getNamespaceURI().equals("http://www.crdp.ac-versailles.fr/2012/apiscol")) ;
		
		System.out.println("***End of namespace checking in the pack response***");
		tempDir.delete();
	}

}
