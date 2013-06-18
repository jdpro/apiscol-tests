package fr.ac_versailles.crdp.apiscol.tests.gp5;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

//@Ignore
public class ImportImsldTest extends ApiScolTests {
	@Before
	public void initialize() {
		createClient();
	}

	@After
	public void close() {
		closeClient();
	}
	@Test
	
	public void testPostingImsLdFile1() {
		System.out.println("***begin of testPostingImsldFile1***");
		File tempDir = unzipImsPackage("solution_aqueuse_imsld.zip");
		XmlPage packResponse=postingImsLdFile1(tempDir);
		assertTrue("The pack gives a response", packResponse != null);
		System.out.println("***end of testPostingImsld***");
		System.out.println(packResponse.asXml());
		tempDir.delete();
	}
}
