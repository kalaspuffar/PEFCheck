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
        return new PageIdentifiers(-1, -1, -1, false);
    }

    /**
     * Given a string representing a page number return the current number.
     *
     * @param num   String representing the number #[a-j]+ or __[ivl]+
     * @return      A number equal to the number representation. Returns -1 if incorrect.
     */
    protected int getPageNumber(String num) {
        num = num.trim();
        final String allowedSyntax = "(#[a-j]+)|(_[mdclxvi])|(__[mdclxvi]+)";
        final String alphaNumbers = "jabcdefghi";

        if (!num.matches(allowedSyntax)) {
            return -1;
        }

        if(num.startsWith("#")) {
            int sum = 0;
            for(int i = 1; i < num.length(); i++) {
                if(i != 1) {
                    sum *= 10;
                }
                sum += alphaNumbers.indexOf(num.charAt(i));
            }
            return sum;
        } else if(num.startsWith("__")) {
            // We don't allow single numbers when 2 control characters are used.
            if (num.length() < 4) {
                return -1;
            }
            num = num.substring(2).toUpperCase();

            int result = 0;
            int[] decimal = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
            String[] roman = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
            for (int i = 0; i < decimal.length; i++) {
                while (num.indexOf(roman[i]) == 0){
                    result += decimal[i];
                    num = num.substring(roman[i].length());
                }
            }
            return result;
        } else if(num.startsWith("_")) {
            final String romanNumbers = "MDCLXVI";
            final int[] romanToDecimal = {1000, 500, 100, 50, 10, 5, 1};
            int index = romanNumbers.indexOf(num.substring(1).toUpperCase());
            if(index == -1) {
                return -1;
            }
            return romanToDecimal[index];
        }
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

        if(args.length != 1) {
            System.out.println("PEF-Validator " + PefValidator.class.getPackage().getImplementationVersion());
            System.out.println();
            System.out.println("java -jar pef-validator.jar [options] inputfile");

            System.exit(1);
        }

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
