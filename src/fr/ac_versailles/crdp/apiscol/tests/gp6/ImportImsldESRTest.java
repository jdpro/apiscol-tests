package fr.ac_versailles.crdp.apiscol.tests.gp6;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

//@Ignore
public class ImportImsldESRTest extends ApiScolTests {
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
		System.out.println("***begin of testPostingImsldESRFile***");
		File tempDir = unzipImsPackage("sol_aqueuse_demo.zip");
		System.out.println(tempDir.getAbsolutePath());
		XmlPage packResponse = postingImsLdFile1(tempDir);
		assertTrue("The pack gives a response", packResponse != null);
		System.out.println("***end of testPostingImsld***");
		System.out.println(packResponse.asXml());
		tempDir.delete();
	}
}
