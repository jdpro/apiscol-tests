package fr.ac_versailles.crdp.apiscol.tests.gp7;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.parser.AutoDetectParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sun.security.util.Length;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

import fr.ac_versailles.crdp.apiscol.tests.ApiScolTests;

//@Ignore
public class ImportEdubasesTest extends ApiScolTests {
	static String[] docs = {
			"application/msword",
			"application/vnd.ms-excel",
			"application/vnd.ms-powerpoint",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"application/vnd.openxmlformats-officedocument.presentationml.presentation",
			"application/vnd.oasis.opendocument.text", "application/rtf",
			"application/pdf", "image/tiff", "image/jpeg", "image/png",
			"video/x-ms-wmv", "video/x-m4v", "video/flv", "video/x-flv",
			"video/ogg", "video/avi" };
	private static final String EDUBASE = "http://eduscol.education.fr/bd/urtic/";
	private URL url;
	private Tika tika;

	@Before
	public void initialize() {
		createClient();
		tika = new Tika(new DefaultDetector(), new AutoDetectParser());
	}

	@After
	public void close() {
		closeClient();
	}

	@Test
	public void testEdubasesDump() {
		// testEdubasesDumpFor("lv");
		// testEdubasesDumpFor("arpl");
		// testEdubasesDumpFor("ses");
		testEdubasesDumpFor("histgeo");
	}

	public void testEdubasesDumpFor(String matiere) {
		matiere += "/";
		new File(testDataDirectory + "edubases").delete();
		new File(testDataDirectory + "edubases").mkdirs();
		System.out.println("création du répertoire " + testDataDirectory
				+ "edubases");
		url = getServiceUrl("/edit/meta", editionServiceBaseUrl);
		assertTrue("The Url must be valid", url != null);
		HtmlPage page = null;
		String url = EDUBASE + matiere + "liste_fiches.php";
		try {
			WebRequest request = new WebRequest(new URL(url), HttpMethod.GET);
			page = webClient.getPage(request);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<HtmlElement> h3 = page.getElementById("liste-fiches")
				.getElementsByTagName("li");
		Iterator<HtmlElement> it = h3.iterator();
		int i = 0;
		while (it.hasNext()) {
			i++;
			System.out.println(i + ".");
			if (i > 30)
				break;

			HtmlElement h3Elem = it.next();
			// if (i < 3000)
			// continue;
			DomNodeList<HtmlElement> a = h3Elem.getElementsByTagName("a");
			if (a.getLength() == 0)
				continue;
			DomElement item = (DomElement) a.item(0);
			String href = item.getAttribute("href");
			getNoticePage(href, matiere);
		}
	}

	private void getNoticePage(String href, String matiere) {
		WebRequest request;
		try {
			request = new WebRequest(new URL(EDUBASE + matiere + href),
					HttpMethod.GET);
			HtmlPage page = webClient.getPage(request);
			DomNodeList<HtmlElement> link = page.getBody()
					.getElementsByTagName("a");
			Iterator<HtmlElement> it = link.iterator();
			while (it.hasNext()) {
				HtmlElement a = it.next();
				if (!a.hasAttribute("class"))
					continue;
				if (!a.getAttribute("class").equals("popup"))
					continue;
				getLomfr(a.getAttribute("href"), matiere);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void getLomfr(String href, String matiere) {
		try {
			XmlPage page = webClient.getPage(new WebRequest(new URL(EDUBASE
					+ matiere + href), HttpMethod.GET));
			correctProblems(page);
			// if (true == true)
			// return;
			String fileName = "edubases/"
					+ href.replace("../xml/" + matiere, "");
			String xml = page
					.asXml()
					.replaceAll("#nomenclature", "nomenclature")
					.replaceAll("(\\r|\\n)", "")
					.replaceAll(
							"<dateTime>[^\\d<>]+(\\d{4})-(\\d+)-(\\d+)[^\\d<>]+</dateTime>",
							"<dateTime>$1-01-01</dateTime>");
			if (matiere.contains("arpl"))
				xml = xml.replaceAll(
						"<dateTime>[^\\d<>]+(\\d{4}-\\d+)[^\\d<>]+</dateTime>",
						"<dateTime>$1-01</dateTime>");
			writeToFile(xml, fileName);

			String pageUri = ((DomElement) ((DomElement) (page
					.getDocumentElement()).getElementsByTagName("general")
					.item(0)).getElementsByTagName("identifier").item(0))
					.getElementsByTagName("entry").item(0).getTextContent();
			String filePath = testDataDirectory + downloadFile(pageUri);
			File file = new File(filePath);
			if (!file.exists() || file.isDirectory())
				return;
			String mimeType = null;
			try {
				mimeType = tika.detect(file);
				System.out
						.println("Tika has detected the following mime type :"
								+ mimeType + " for the file " + filePath);
			} catch (IOException e) {
				System.out.println("Tika was not able to Parse : " + filePath);
				e.printStackTrace();
			}
			if (Arrays.asList(docs).contains(mimeType))
				postFileToApiscol(fileName, pageUri);
			else
				postLinkToApiscol(fileName, pageUri);
			if (StringUtils.isNotEmpty(filePath)) {
				if (file.exists() && !file.isDirectory())
					file.delete();
			}
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void correctProblems(XmlPage page) {
		correctNameSpaceProblems(page);
		correctVcardsProblems(page);
		correctTaxonProblems(page);
		correctDateTimeProblems(page);
		correctClassificationProblems(page);
		correctClassificationProblems(page);
		correctDoubleEducationalNodeProblems(page);
	}

	private void correctDoubleEducationalNodeProblems(XmlPage page) {
		Element root = getLomDocumentRootItem(page);
		NodeList classifications = root.getElementsByTagName("educational");
		Element ed1, ed2;
		if (classifications.getLength() == 2) {
			ed1 = (Element) classifications.item(0);
			ed2 = (Element) classifications.item(1);
			while (ed2.hasChildNodes()) {
				ed1.appendChild(ed2.getFirstChild());
			}
		}

	}

	private void correctClassificationProblems(XmlPage page) {
		String level1 = "";
		String level2 = "";
		Element root = getLomDocumentRootItem(page);
		NodeList classifications = root.getElementsByTagName("classification");
		for (int i = 0; i < classifications.getLength(); i++) {
			DomElement classification = (DomElement) classifications.item(i);
			DomElement purpose = classification.getElementsByTagName("purpose")
					.get(0);
			DomElement purposeValue = purpose.getElementsByTagName("value")
					.get(0);

			if (purposeValue.getTextContent().trim().toLowerCase()
					.equals("discipline"))
				purposeValue.setTextContent("domaine d'enseignement");
			NodeList taxonPaths = root.getElementsByTagName("taxonPath");
			DomElement taxonPathToComplete = null;
			HashMap<String, DomElement> competenciesTaxonPath = new HashMap<String, DomElement>();
			HashMap<String, DomElement> domainsTaxons = new HashMap<String, DomElement>();
			for (int j = 0; j < taxonPaths.getLength(); j++) {
				DomElement taxonPath = (DomElement) taxonPaths.item(j);
				DomElement source = taxonPath.getElementsByTagName("source")
						.get(0);
				DomElement string = source.getElementsByTagName("string")
						.get(0);
				System.out.println("on cherche " + string.getTextContent());
				if (string
						.getTextContent()
						.trim()
						.toLowerCase()
						.contains(
								"nomenclature niveaux EDU'bases v2099".trim()
										.toLowerCase())) {
					taxonPath.getParentNode().removeChild(taxonPath);

				} else if (string.getTextContent().trim()
						.contains("Nomenclature niveaux EDU'bases v2007")) {
					level1 = getTaxon(taxonPath).getTextContent();
					string.setTextContent("scolomfr-voc-022");
					if (level1.equals("Lycée")) {
						getTaxon(taxonPath).setTextContent("lycée");
						getTaxonId(taxonPath).setTextContent(
								"scolomfr-voc-022-num-024");
					} else if (level1.equals("Collège")) {
						getTaxon(taxonPath).setTextContent("collège");
						getTaxonId(taxonPath).setTextContent(
								"scolomfr-voc-022-num-016");
					} else if (level1.equals("Lycée professionnel")) {
						getTaxon(taxonPath).setTextContent(
								"lycée professionnel");
						getTaxonId(taxonPath).setTextContent(
								"scolomfr-voc-022-num-087");
					} else
						assertTrue(level1, false);
					taxonPathToComplete = taxonPath;
				} else if (string.getTextContent().trim()
						.contains("Niveaux EDU'bases v2012")) {
					String id = null;
					String entry;
					if (getTaxon(taxonPath) == null)
						continue;
					entry = level2 = getTaxon(taxonPath).getTextContent();
					taxonPath.getParentNode().removeChild(taxonPath);

					switch (level2) {
					case "6e":
						id = "scolomfr-voc-022-num-018";
						break;
					case "5e":
						id = "scolomfr-voc-022-num-020";
						break;
					case "4e":
						id = "scolomfr-voc-022-num-021";
						break;
					case "3e":
						id = "scolomfr-voc-022-num-023";
						break;
					case "2nde":
						id = "scolomfr-voc-022-num-025";
						entry = "2de";
						break;
					case "1re L":
						id = "scolomfr-voc-022-num-030";
						entry = "1ère L";
						break;
					case "1re ES":
						id = "scolomfr-voc-022-num-029";
						entry = "1ère ES";
						break;
					case "1re S":
						id = "scolomfr-voc-022-num-031";
						entry = "1ère S";
						break;
					case "terminale ES":
						id = "scolomfr-voc-022-num-040";
						break;
					case "terminale L":
						id = "scolomfr-voc-022-num-041";
						break;
					case "terminale STG":
						id = "scolomfr-voc-022-num-045";
						break;
					case "terminale Bac Pro":
						id = "scolomfr-voc-022-num-050";
						entry = "terminale professionnelle";
						break;
					case "1re Bac Pro":
						id = "scolomfr-voc-022-num-038";
						entry = "1ère professionnelle";
						break;
					case "1re STI2D":
						id = "scolomfr-voc-022-num-035";
						entry = "1ère STI2D";
						break;
					case "CAP (certificat d'aptitude professionnelle)":
						id = "scolomfr-voc-022-num-054";
						entry = "CAP - certificat d'aptitude professionnelle";
						break;
					case "1re ST2S":
						id = "scolomfr-voc-022-num-032";
						entry = "1ère ST2S";
						break;
					case "2de Bac Pro":
						id = "scolomfr-voc-022-num-027";
						entry = "2de professionnelle";
						break;
					case "1re STG":
						id = "scolomfr-voc-022-num-034";
						entry = "1ère STG";
						break;
					case "1re STL":
						id = "scolomfr-voc-022-num-036";
						entry = "1ère STL";
						break;
					case "terminale ST2S":
						id = "scolomfr-voc-022-num-043";
						entry = "terminale ST2S";
						break;
					default:
						System.out.println("absent : " + level2);
						assertTrue(level2, false);

					}
					DomElement newTaxon = (DomElement) taxonPathToComplete
							.getElementsByTagName("taxon").item(0)
							.cloneNode(true);
					newTaxon.getElementsByTagName("id").item(0)
							.setTextContent(id);
					newTaxon.getElementsByTagName("string").item(0)
							.setTextContent(entry);
					taxonPathToComplete.appendChild(newTaxon);
				} else if (string
						.getTextContent()
						.trim()
						.toLowerCase()
						.contains(
								"Nomenclature diciplines EDU'bases v2007"
										.trim().toLowerCase())) {
					String str = getTaxon(taxonPath).getTextContent();
					if (str.trim().toLowerCase()
							.equals("Histoire-géographie".toLowerCase())) {
						string.setTextContent("scolomfr-voc-015");
						getTaxon(taxonPath).setTextContent(
								"domaines d'enseignement du second degré");
						getTaxonId(taxonPath).setTextContent(
								"scolomfr-voc-015-num-008");
						DomElement newTaxon = (DomElement) taxonPath
								.getElementsByTagName("taxon").item(0)
								.cloneNode(true);
						newTaxon.getElementsByTagName("id").item(0)
								.setTextContent("scolomfr-voc-015-num-042");
						newTaxon.getElementsByTagName("string").item(0)
								.setTextContent("histoire et géographie");
						taxonPath.appendChild(newTaxon);
					}
				} else if (string
						.getTextContent()
						.trim()
						.toLowerCase()
						.contains(
								"domaines EDU'bases histgeo v2007".trim()
										.toLowerCase())) {
					String str = getTaxon(taxonPath).getTextContent();
					String id = "";
					String entry = "";
					if (str.trim().toLowerCase()
							.equals("Histoire".toLowerCase())) {
						id = "scolomfr-voc-015-num-040";
						entry = "histoire";
					}
					if (str.trim().toLowerCase()
							.equals("Géographie".toLowerCase())) {
						id = "scolomfr-voc-015-num-036";
						entry = "géographie";
					}
					if (str.trim().toLowerCase()
							.equals("Éducation civique".toLowerCase())) {
						id = "scolomfr-voc-015-num-029";
						entry = "Éducation civique";
					}
					assertTrue(str, StringUtils.isNotBlank(str));
					string.setTextContent("scolomfr-voc-015");
					getTaxon(taxonPath).setTextContent(
							"domaines d'enseignement du second degré");
					getTaxonId(taxonPath).setTextContent(
							"scolomfr-voc-015-num-008");
					DomElement newTaxon = (DomElement) taxonPath
							.getElementsByTagName("taxon").item(0)
							.cloneNode(true);
					newTaxon.getElementsByTagName("id").item(0)
							.setTextContent(id);
					newTaxon.getElementsByTagName("string").item(0)
							.setTextContent(entry);
					taxonPath.appendChild(newTaxon);

				} else if (string.getTextContent().trim().toLowerCase()
						.contains("Compétences du socle".trim().toLowerCase())) {
					String str = getTaxon(taxonPath).getTextContent();
					String id = "";
					String entry = "";
					String key = "";
					if (str.trim().startsWith("C1")) {
						id = "scolomfr-voc-016-num-001";
						entry = "compétence 1 - la maîtrise de la langue française";
						key = "C1";
					} else if (str.trim().startsWith("C2")) {
						id = "scolomfr-voc-016-num-002";
						entry = "compétence 2 - la pratique d'une langue vivante étrangère (niveau a2)";
						key = "C2";
					} else if (str.trim().startsWith("C3")) {
						id = "scolomfr-voc-016-num-003";
						entry = "compétence 3 - les principaux éléments de mathématiques et la culture scientifique et technologique";
						key = "C3";
					} else if (str.trim().startsWith("C4")) {
						id = "scolomfr-voc-016-num-004";
						entry = "compétence 4 - la maîtrise des techniques usuelles de l'information et de la communication (b2i)";
						key = "C4";
					} else if (str.trim().startsWith("C5")) {
						id = "scolomfr-voc-016-num-005";
						entry = "compétence 5 - la culture humaniste";
						key = "C5";
					} else if (str.trim().startsWith("C6")) {
						id = "scolomfr-voc-016-num-006";
						entry = "compétence 6 - les compétences sociales et civiques";
						key = "C6";
					} else if (str.trim().startsWith("C7")) {
						id = "scolomfr-voc-016-num-007";
						entry = "compétence 7 - l'autonomie et l'initiative";
						key = "C7";
					}

					purposeValue.setTextContent("competency");
					string.setTextContent("scolomfr-voc-016");
					getTaxon(taxonPath).setTextContent(entry);
					getTaxonId(taxonPath).setTextContent(id);
					assertTrue(key + "is blank", StringUtils.isNotEmpty(key));
					if (domainsTaxons.keySet().contains(key)) {
						taxonPath.appendChild(domainsTaxons.get(key));
						System.out.println("copie " + key);
					} else
						competenciesTaxonPath.put(key, taxonPath);

				} else if (string
						.getTextContent()
						.trim()
						.toLowerCase()
						.contains(
								"Domaines de compétence du socle".trim()
										.toLowerCase())) {
					System.out.println("on trouve !!!!!!!!!!!!");
					DomElement taxonEntry = getTaxon(taxonPath);
					if (taxonEntry != null) {
						String str = taxonEntry.getTextContent();
						String id = "";
						String entry = "";
						String key = "";
						System.out.println("Compétence recherchée :" + str);
						if (str.trim().startsWith("C1")) {
							key = "C1";
							if (str.contains("crire")) {
								id = "scolomfr-voc-016-num-0012";
								entry = "écrire";
							}
							if (str.contains("lire")) {
								id = "scolomfr-voc-016-num-0011";
								entry = "lire";
							}
							if (str.contains("oral")) {
								id = "scolomfr-voc-016-num-0013";
								entry = "s'exprimer à l'oral";
							}
							if (str.contains("outils")) {
								id = "scolomfr-voc-016-num-0014";
								entry = "utiliser des outils";
							}
						} else if (str.trim().startsWith("C2")) {
							key = "C2";
							if (str.contains("dialoguer")) {
								id = "scolomfr-voc-016-num-0021";
								entry = "réagir et dialoguer";
							}
							if (str.contains("comprendre")) {
								id = "scolomfr-voc-016-num-0022";
								entry = "écouter et comprendre";
							}
							if (str.contains("continu")) {
								id = "scolomfr-voc-016-num-0023";
								entry = "scolomfr-voc-016-num-0023";
							}
							if (str.contains("lire")) {
								id = "scolomfr-voc-016-num-0024";
								entry = "lire";
							}
							if (str.contains("écrire")) {
								id = "scolomfr-voc-016-num-0025";
								entry = "écrire";
							}

						} else if (str.trim().startsWith("C3")) {
							key = "C3";
							if (str.contains("technologique")) {
								id = "scolomfr-voc-016-num-0031";
								entry = "pratiquer une démarche scientifique et technologique, résoudre des problèmes";
							}
							if (str.contains("mathématiques")) {
								id = "scolomfr-voc-016-num-0032";
								entry = "savoir utiliser des connaissances et des compétences mathématiques";
							}
							if (str.contains("scientifiques")) {
								id = "scolomfr-voc-016-num-0033";
								entry = "savoir utiliser des connaissances dans divers domaines scientifiques";
							}
							if (str.contains("environnement")) {
								id = "scolomfr-voc-016-num-0034";
								entry = "mobiliser ses connaissances pour comprendre des questions liées à l'environnement et au développement durable";
							}

						} else if (str.trim().startsWith("C4")) {
							key = "C4";
							if (str.contains("approprier")) {
								id = "scolomfr-voc-016-num-0041";
								entry = "domaine 1 - s'approprier un environnement informatique de travail";
							}
							if (str.contains("responsable")) {
								id = "scolomfr-voc-016-num-0042";
								entry = "domaine 2 - adopter une attitude responsable";
							}
							if (str.contains("exploiter")) {
								id = "scolomfr-voc-016-num-0043";
								entry = "domaine 3 - créer, produire, traiter, exploiter des données";
							}
							if (str.contains("documenter")) {
								id = "scolomfr-voc-016-num-0044";
								entry = "domaine 4 - s'informer, se documenter";
							}
							if (str.contains("communiquer")) {
								id = "scolomfr-voc-016-num-0045";
								entry = "domaine 5 - communiquer, échanger";
							}

						} else if (str.trim().startsWith("C5")) {
							key = "C5";
							if (str.contains("géographiques")) {
								id = "scolomfr-voc-016-num-0051";
								entry = "avoir des repères géographiques";
							}
							if (str.contains("historiques")) {
								id = "scolomfr-voc-016-num-0052";
								entry = "avoir des repères historiques";
							}
							if (str.contains("littéraires")) {
								id = "scolomfr-voc-016-num-0053";
								entry = "avoir des repères littéraires";
							}
							if (str.contains("histoire des arts")) {
								id = "scolomfr-voc-016-num-0054";
								entry = "avoir des repères en histoire des arts et pratiquer les arts";
							}
							if (str.contains("langages")) {
								id = "scolomfr-voc-016-num-0055";
								entry = "lire et utiliser différents langages";
							}
							if (str.contains("outils")) {
								id = "scolomfr-voc-016-num-0056";
								entry = "avoir des outils pour comprendre l'unité et la complexité du monde";
							}

						} else if (str.trim().startsWith("C6")) {
							key = "C6";
							if (str.contains("civique")) {
								id = "scolomfr-voc-016-num-0061";
								entry = "connaître les principes et fondements de la vie civique et sociale";
							}
							if (str.contains("responsable")) {
								id = "scolomfr-voc-016-num-0062";
								entry = "avoir un comportement responsable";
							}

						} else if (str.trim().startsWith("C7")) {
							key = "C7";
							if (str.contains("métiers")) {
								id = "scolomfr-voc-016-num-0071";
								entry = "découvrir les métiers et les formations";
							}
							if (str.contains("mobiliser")) {
								id = "scolomfr-voc-016-num-00712";
								entry = "être capable de mobiliser ses ressources intellectuelles et physiques dans diverses situations";
							}
							if (str.contains("initiative")) {
								id = "scolomfr-voc-016-num-00713";
								entry = "faire preuve d'initiative";
							}

						}
						assertTrue(key + "   " + str,
								StringUtils.isNotBlank(entry));
						getTaxon(taxonPath).setTextContent(entry);
						getTaxonId(taxonPath).setTextContent(id);
						taxonPath.getParentNode().removeChild(taxonPath);
						DomElement taxon = taxonPath.getElementsByTagName(
								"taxon").get(0);
						assertTrue(key + "is blank",
								StringUtils.isNotEmpty(key));
						if (competenciesTaxonPath.keySet().contains(key)) {

							DomElement targetTaxonPath = competenciesTaxonPath
									.get(key);
							if (targetTaxonPath.getElementsByTagName("taxon")
									.size() > 1) {
								DomElement targetTaxonPath2 = (DomElement) targetTaxonPath
										.cloneNode(true);
								DomElement taxon2 = (DomElement) targetTaxonPath2
										.getElementsByTagName("taxon").item(1);
								taxon2.getParentNode().removeChild(taxon2);
								taxon.getParentNode().removeChild(taxon);
								targetTaxonPath2.appendChild(taxon);
								classification.appendChild(targetTaxonPath2);
								System.out.println("replication " + key);
							} else {
								targetTaxonPath.appendChild(taxon);
								System.out.println("ajout " + key);
							}
						} else
							domainsTaxons.put(key, taxon);
					}

				}
			}
		}
		DomElement domaineEnseignementClassification = null;
		for (int i = 0; i < classifications.getLength(); i++) {
			DomElement classification = (DomElement) classifications.item(i);
			DomElement purpose = classification.getElementsByTagName("purpose")
					.get(0);
			DomElement purposeValue = purpose.getElementsByTagName("value")
					.get(0);
			if (purposeValue.getTextContent().trim().toLowerCase()
					.equals("domaine d'enseignement"))
				domaineEnseignementClassification = classification;
		}
		if (domaineEnseignementClassification == null)
			return;
		for (int i = 0; i < classifications.getLength(); i++) {
			DomElement classification = (DomElement) classifications.item(i);
			DomElement purpose = classification.getElementsByTagName("purpose")
					.get(0);
			DomElement purposeValue = purpose.getElementsByTagName("value")
					.get(0);
			if (!purposeValue.getTextContent().trim().toLowerCase()
					.equals("enseignement"))
				continue;
			NodeList taxonPaths = classification
					.getElementsByTagName("taxonPath");
			for (int j = 0; j < taxonPaths.getLength(); j++) {
				DomElement taxonPath = (DomElement) taxonPaths.item(j);
				DomElement source = taxonPath.getElementsByTagName("source")
						.get(0);
				DomElement string = source.getElementsByTagName("string")
						.get(0);
				if (string.getTextContent().trim().toLowerCase()
						.contains("scolomfr-voc-015".trim().toLowerCase())) {
					taxonPath.getParentNode().removeChild(taxonPath);
					domaineEnseignementClassification.appendChild(taxonPath);
				}

			}
		}
	}

	private DomElement getTaxon(DomElement taxonPath) {
		if (taxonPath.getElementsByTagName("taxon").size() == 0)
			return null;
		DomElement taxon = taxonPath.getElementsByTagName("taxon").get(0);
		DomElement entry = taxon.getElementsByTagName("entry").get(0);
		DomElement string = entry.getElementsByTagName("string").get(0);
		return string;
	}

	private DomElement getTaxonId(DomElement taxonPath) {
		DomElement taxon = taxonPath.getElementsByTagName("taxon").get(0);
		DomElement id = taxon.getElementsByTagName("id").get(0);
		return id;
	}

	private void correctNameSpaceProblems(XmlPage page) {
		Element root = getLomDocumentRootItem(page);
		if (root.hasAttribute("xmlns:lomfr"))
			root.removeAttribute("xmlns:lomfr");
		root.setAttribute("xmlns:lomfr", "http://www.lom-fr.fr/xsd/LOMFR");
		if (root.hasAttribute("xmlns:scolomfr"))
			root.removeAttribute("xmlns:scolomfr");
		root.setAttribute("xmlns:scolomfr", "http://www.lom-fr.fr/xsd/SCOLOMFR");
		if (root.hasAttribute("xmlns"))
			root.removeAttribute("xmlns");
		root.setAttribute("xmlns", "http://ltsc.ieee.org/xsd/LOM");

	}

	private void correctDateTimeProblems(XmlPage page) {
		Element root = getLomDocumentRootItem(page);
		NodeList dateTimes = root.getElementsByTagName("dateTime");
		for (int i = 0; i < dateTimes.getLength(); i++) {
			DomElement dateTime = (DomElement) dateTimes.item(i);

			String textContent = dateTime.getTextContent();
			String replace = textContent.trim().replaceAll(
					"(\\r|\\n|\\t|\\s| )", "");
			DomElement domElement = new DomElement(dateTime.getNamespaceURI(),
					"dateTime", page, Collections.EMPTY_MAP);
			domElement.setTextContent(replace);
			dateTime.replace(domElement);
			dateTime.normalize();
		}

	}

	private void correctVcardsProblems(XmlPage page) {
		Element root = getLomDocumentRootItem(page);
		NodeList entities = root.getElementsByTagName("entity");
		for (int i = 0; i < entities.getLength(); i++) {
			DomElement entity = (DomElement) entities.item(i);

			String textContent = entity.getTextContent();
			String replace = textContent.trim().replaceAll(";[^:]+", "");
			DomElement domElement = new DomElement(entity.getNamespaceURI(),
					"entity", page, Collections.EMPTY_MAP);
			domElement.setTextContent(replace);
			entity.replace(domElement);
			entity.normalize();
		}

	}

	private void correctTaxonProblems(XmlPage page) {
		Element root = getLomDocumentRootItem(page);
		NodeList txpths = root.getElementsByTagName("taxonPath");
		for (int i = 0; i < txpths.getLength(); i++) {
			Node txpth = txpths.item(i);
			Node child = txpth.getFirstChild();
			Node idOnlyTaxon = null;
			List<Node> nodesToBeRemoved = new LinkedList<Node>();
			while (child != null) {
				if (child instanceof DomElement
						&& child.getLocalName().equals("taxon")) {
					if (((DomElement) child).getChildNodes().getLength() == 3) {
						if (((DomElement) child).getElementsByTagName("id")
								.getLength() == 1) {
							idOnlyTaxon = child;
						} else if (((DomElement) child).getElementsByTagName(
								"entry").getLength() == 1) {
							Node entryNode = ((DomElement) child)
									.getElementsByTagName("entry").item(0);
							child.removeChild(entryNode);
							if (idOnlyTaxon != null)
								idOnlyTaxon.appendChild(entryNode);
							nodesToBeRemoved.add(child);
							idOnlyTaxon = null;
						}
					}

				}
				child = child.getNextSibling();
			}
			if (idOnlyTaxon != null)
				nodesToBeRemoved.add(idOnlyTaxon);
			Iterator<Node> it = nodesToBeRemoved.iterator();
			while (it.hasNext()) {
				Node node = it.next();
				node.getParentNode().removeChild(node);
			}
		}

	}

	private File writeToFile(String content, String fileName) {
		FileOutputStream fop = null;
		File file = null;

		try {

			file = new File(testDataDirectory + fileName);
			fop = new FileOutputStream(file);

			if (!file.exists()) {
				file.mkdirs();
				file.createNewFile();
			}

			byte[] contentInBytes = content.getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;

	}

	private void postLinkToApiscol(String filePath, String pageUri) {

		XmlPage page = postMetadataDocument(filePath, url, false);
		String metadataLinkLocation = getAtomLinkLocation(page, "self",
				"text/html");
		XmlPage newResourcePage = getNewResourcePage(metadataLinkLocation,
				"url");
		String urn = getAtomId(newResourcePage);
		String editUri = getEditMediaUri(newResourcePage);
		XmlPage page2 = postUrlContent(editUri, urn, pageUri, true,
				getAtomUpdatedField(newResourcePage));
		if (page2 == null) {
			System.out.println("Invalid URL : " + pageUri + " (" + filePath
					+ ") ");
			page = getMetadata(metadataLinkLocation, true);
			String linkLocation2 = getAtomLinkLocation(page, "self",
					"text/html");
			deleteMetadataEntry(linkLocation2, getAtomUpdatedField(page));
			return;
		}
		XmlPage page3 = getThumbForMetadataId(metadataLinkLocation);
		askForAutomaticThumbForMetadataId(metadataLinkLocation,
				getThumbEtag(page3));

	}

	private void postFileToApiscol(String filePath, String pageUri) {

		XmlPage page = postMetadataDocument(filePath, url, false);
		String metadataLinkLocation = getAtomLinkLocation(page, "self",
				"text/html");
		XmlPage newResourcePage = getNewResourcePage(metadataLinkLocation);
		String urn = getAtomId(newResourcePage);
		String editUri = getEditMediaUri(newResourcePage);
		String pdfFilePath = downloadFile(pageUri);
		if (StringUtils.isEmpty(pdfFilePath))
			return;
		String eTag = getAtomUpdatedField(newResourcePage);
		postFileDocument(editUri, urn, pdfFilePath, true, eTag);
		XmlPage page3 = getThumbForMetadataId(metadataLinkLocation);
		askForAutomaticThumbForMetadataId(metadataLinkLocation,
				getThumbEtag(page3));
	}

	private String downloadFile(String pageUri) {
		URL u;
		try {
			u = new URL(pageUri);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return StringUtils.EMPTY;
		}
		URLConnection uc;
		try {
			uc = u.openConnection();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return StringUtils.EMPTY;
		}
		String contentType = uc.getContentType();
		int contentLength = uc.getContentLength();
		if (contentLength <= 0)
			contentLength = 100 * 1024 * 1024;
		else
			System.out.println("téléchargement : " + pageUri);
		InputStream raw;
		try {
			raw = uc.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return StringUtils.EMPTY;
		}
		InputStream in = new BufferedInputStream(raw);

		byte[] data;
		try {
			data = new byte[contentLength];
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return StringUtils.EMPTY;
		}
		int bytesRead = 0;
		int offset = 0;
		while (offset < contentLength) {
			try {
				bytesRead = in.read(data, offset, data.length - offset);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return StringUtils.EMPTY;
			}
			if (bytesRead == -1)
				break;
			offset += bytesRead;
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return StringUtils.EMPTY;
		}

		if (offset != contentLength) {
			System.out.println("Only read " + offset + " bytes; Expected "
					+ contentLength + " bytes");
		}

		String filename = "";
		filename = u.getFile();
		if (StringUtils.isEmpty(filename))
			return StringUtils.EMPTY;
		filename = filename.substring(filename.lastIndexOf('/') + 1);
		filename = filename.replaceAll("[^\\dA-Za-z. ]", "").replaceAll("\\s+",
				"+");
		try {
			filename = URLEncoder.encode(filename, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return StringUtils.EMPTY;
		}
		filename = "edubases/" + filename;
		try {
			FileOutputStream out = new FileOutputStream(testDataDirectory
					+ filename);
			out.write(data);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return StringUtils.EMPTY;
		}

		return filename;
	}
}
