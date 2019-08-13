package se.mtm;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class PefValidator {

    private boolean leftPage = false;

    /**
     * Extract page identifiers from current page.
     * @param row   String with the first row of a page containing the identifiers.
     * @return      Object with startPage, endPage in the original and the pef page.
     */
    private static PageIdentifiers getPageIdentifiers(String row) {
        return new PageIdentifiers();
    }

    /**
     * Given a string representing a page number return the current number.
     *
     * @param num   String representing the number #[a-z]+
     * @return      A number equal to the number representation. Returns -1 if incorrect.
     */
    private static int getPageNumber(String num) {
        return -1;
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
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "//page";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

            System.out.println(nodeList.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
