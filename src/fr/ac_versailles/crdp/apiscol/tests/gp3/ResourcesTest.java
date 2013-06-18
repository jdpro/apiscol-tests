package fr.ac_versailles.crdp.apiscol.tests.gp3;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

//@Ignore
public class ResourcesTest extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}
	@Test
	
	public void testResourcesURL() {
		File tempDir = unzipImsPackage("solution_aqueuse_imsld.zip");
		XmlPage packResponse=postingImsLdFile1(tempDir);
		assertTrue("The pack gives a response", packResponse != null);
		Document document=packResponse.getXmlDocument();
		NodeList resource=document.getElementsByTagName("resource");
		for(int i=0;i<resource.getLength();i++){
			System.out.println("recuperation");
			System.out.println("resource n°"+i+":"+resource.item(i).getTextContent());
		}
		
		tempDir.delete();
	}
}
