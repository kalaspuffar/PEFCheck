package se.mtm;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class PefValidator {
    private boolean leftPage = false;
    private int titlePages = 1;

    /**
     * Extract page identifiers from current page.
     *
     * @param page  Element containing the page information.
     * @return      Object with startPage, endPage in the original, the pef page and if the page is empty.
     */
    protected PageIdentifiers getPageIdentifiers(Element page) {
        return new PageIdentifiers();
    }

    /**
     * Given a string representing a page number return the current number.
     *
     * @param num   String representing the number #[a-z]+ or __[ivl]+
     * @return      A number equal to the number representation. Returns -1 if incorrect.
     */
    protected int getPageNumber(String num) {
        return -1;
    }

    /**
     *
     * @param xmlDocument
     * @throws Exception
     */
    protected void processDocument(Document xmlDocument) throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList volumeList = (NodeList) xPath.compile("//volume").evaluate(xmlDocument, XPathConstants.NODESET);
        for(int i = 0; i < volumeList.getLength(); i++) {
            NodeList pageList = (NodeList) xPath.compile("section/page").evaluate(volumeList.item(i), XPathConstants.NODESET);
            for(int j = titlePages; j < pageList.getLength(); j++) {
                PageIdentifiers pageIdentifiers = getPageIdentifiers((Element) pageList.item(j));
            }
        }
    }

    /**
     * Given a file we will check if we have the right page num sequence for all pages
     * and look for empty pages.
     *
     * @param args the first argument is the file to run.
     */
    public static void main(String[] args) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(args[0]);

            PefValidator pefValidator = new PefValidator();
            pefValidator.processDocument(xmlDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
