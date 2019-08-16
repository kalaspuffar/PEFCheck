package se.mtm;

import com.sun.media.sound.InvalidFormatException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.ValidationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class PEFCheck {
    private boolean leftPage = false;
    private int titlePages = 1;

    /**
     * This function takes a row of page identifiers and extracts the page numbers.
     *
     * @param row       Data from top of page
     * @param leftPage  True if this is the left side page this means the pef page number is
     *                  on the left side. The original page numbering is on the opposite side.
     * @param indexPage Index pages are pages using roman numbers and created by the pef processor,
     *                  these pages aren't a part of the original content so they will not have an
     *                  original page numbering.
     * @return
     */
    protected PageIdentifiers getPageIdentifiers(String row, boolean leftPage, boolean indexPage) {
        PageIdentifiers pageIdentifiers = new PageIdentifiers();
        if(indexPage) {
            pageIdentifiers.setPefPage(getPageNumber(row));
            return pageIdentifiers;
        }

        String pefPage, orgPages;
        row = row.trim();
        int middle = row.indexOf(" ");
        if(leftPage) {
            pefPage = row.substring(0, middle);
            orgPages = row.substring(middle + 1);
        } else {
            orgPages = row.substring(0, middle);
            pefPage = row.substring(middle + 1);
        }

        pageIdentifiers.setPefPage(getPageNumber(pefPage));

        if(orgPages.contains("--")) {
            String[] orgPagesArr = orgPages.split("--");
            pageIdentifiers.setOrgStartPage(getPageNumber(orgPagesArr[0]));
            pageIdentifiers.setOrgEndPage(getPageNumber(orgPagesArr[1]));
        } else {
            pageIdentifiers.setOrgStartPage(getPageNumber(orgPages));
        }

        return pageIdentifiers;
    }


    /**
     * Extract page identifiers from current page.
     *
     * @param page      Element containing the page information.
     * @param leftPage  True if this is the left side page this means the pef page number is
     *                  on the left side. The original page numbering is on the opposite side.
     * @param indexPage Index pages are pages using roman numbers and created by the pef processor,
     *                  these pages aren't a part of the original content so they will not have an
     * @return          Object with startPage, endPage in the original, the pef page and if the page is empty.
     */
    protected PageIdentifiers processPage(Element page, boolean leftPage, boolean indexPage) throws InvalidFormatException {
        if (!page.getTagName().equalsIgnoreCase("page")) {
            throw new InvalidFormatException("page tag incorrect");
        }
        if (page.getChildNodes().getLength() == 0) {
            throw new InvalidFormatException("No rows present");
        }
        if (page.getFirstChild().getTextContent().isEmpty()) {
            throw new InvalidFormatException("No data in first row");
        }

        PageIdentifiers pageIdentifiers = getPageIdentifiers(
                page.getFirstChild().getTextContent(), leftPage, indexPage
        );

        if (pageIdentifiers.getPefPage() == -1) {
            throw new InvalidFormatException("Can't find the page number");
        }

        pageIdentifiers.setEmpty(page.getChildNodes().getLength() < 2);
        pageIdentifiers.setIndexPage(indexPage);

        return pageIdentifiers;
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
     * Processing a section requires functionality to know which kind of page to handle. Each
     * section starts with a right hand page and then follows by a left page, this will alternate
     * through a book.
     *
     * @param section       Section to extract page information from.
     * @param indexSection  True if the section is a index section. (Uses roman numbers)
     * @return              A list of page identifiers that we can use for validation.
     * @throws Exception    Throws an exception if the section is not correctly formatted.
     */
    protected List<PageIdentifiers> processSection(Element section, boolean indexSection) throws Exception {
        List<PageIdentifiers> pageIdentifiersList = new ArrayList<>();

        if (!section.getTagName().equalsIgnoreCase("section")) {
            throw new InvalidFormatException("section tag incorrect");
        }
        if (section.getChildNodes().getLength() == 0) {
            throw new InvalidFormatException("No pages present");
        }

        NodeList pageList = section.getChildNodes();
        boolean leftPage = false;
        for(int j = 0; j < pageList.getLength(); j++) {
            pageIdentifiersList.add(processPage((Element) pageList.item(j), leftPage, indexSection));
            leftPage = !leftPage;
        }

        return pageIdentifiersList;
    }


    /**
     * Process an PEF document finding which sections we should handle in different ways.
     *
     * @param xmlDocument       XML document in PEF format
     * @throws Exception        Throws exceptions when the document is not well formatted.
     */
    protected void processDocument(Document xmlDocument) throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList volumeList = (NodeList) xPath.compile("//volume").evaluate(xmlDocument, XPathConstants.NODESET);
        for(int i = 0; i < volumeList.getLength(); i++) {
            NodeList sectionList = (NodeList) xPath.compile("section").evaluate(volumeList.item(i), XPathConstants.NODESET);
            for(int j = titlePages; j < sectionList.getLength(); j++) {
                Element section = (Element) sectionList.item(j);
                processSection(section, isIndexSection(section));
            }
        }
    }

    /**
     * This function checks if the current section is containing a page index
     * and should therefore use roman page numbering.
     *
     * @param section   Section object to check for roman page numbering.
     * @return          True if roman page numbers are present.
     */
    protected boolean isIndexSection(Element section) throws InvalidFormatException {
        if (!section.getTagName().equalsIgnoreCase("section")) {
            throw new InvalidFormatException("section tag incorrect");
        }
        if (section.getChildNodes().getLength() == 0) {
            throw new InvalidFormatException("No pages present");
        }
        Element page = (Element)section.getFirstChild();
        if (page.getChildNodes().getLength() == 0) {
            throw new InvalidFormatException("No rows present");
        }
        Element row = (Element)page.getFirstChild();

        return row.getTextContent().trim().equalsIgnoreCase("_i");
    }

    /**
     * In order to present page numbers when we report issues we need to present them
     * both as the decimal number and the original presentation in order to search and
     * debug.
     *
     * @param pageNum       Number to present
     * @param indexPage     True if this should be presented as roman numbers.
     * @return              A string presenting both the original number and decimal.
     */
    protected String getPrintablePageNumber(int pageNum, boolean indexPage) {
        String result = "";

        if (indexPage) {
            int[] decimal = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
            String[] roman = {"m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i"};
            int num = pageNum;
            for (int i = 0; i < decimal.length; i++) {
                while (num % decimal[i] < num) {
                    result += roman[i];
                    num -= decimal[i];
                }
            }
            String prefix = result.length() == 1 ? "_" : "__";
            return prefix + result + " (" + pageNum + ")";
        }

        String decimal = "1234567890";
        String pef = "abcdefghij";

        String orgNumber = "" + pageNum;
        for (int i = 0; i < orgNumber.length(); i++) {
            int index = decimal.indexOf(orgNumber.charAt(i));
            result += pef.charAt(index);
        }

        return "#" + result + " (" + pageNum + ")";
    }

    /**
     * This function will validate the page sequence to check for page ranges missing.
     * It will report if it finds a sequence of missing numbers and throw exception.
     *
     * @param pageIdentifiers       List of pages identifiers to validate
     * @param startPage             This page is the page before this sequence starts
     * @return                      next start page
     * @throws ValidationException  Throws validation exception if a break in sequence is found.
     */
    protected int validatePageSequence(List<PageIdentifiers> pageIdentifiers, int startPage) throws ValidationException {
        return 0;
    }

    /**
     * This function runs the page list and looks for empty pages. Report on the PEF number missing and
     * returns result.
     *
     * @param pageIdentifiers     Page sequence to check for empty pages.
     * @return                    true if empty pages where found.
     */
    protected boolean hasEmptyPages(List<PageIdentifiers> pageIdentifiers) throws InvalidFormatException {
        boolean empty = false;
        for (PageIdentifiers pi : pageIdentifiers) {
            if(pi.getPefPage() == -1) {
                throw new InvalidFormatException("Incorrect page number when checking for empty pages");
            }

            if(pi.isEmpty()) {
                System.out.println("--- Empty page " + getPrintablePageNumber(pi.getPefPage(), pi.isIndexPage()));
                empty = true;
            }
        }
        return empty;
    }

    /**
     * Given a file we will check if we have the right page num sequence for all pages
     * and look for empty pages.
     *
     * @param args the first argument is the file to run.
     */
    public static void main(String[] args) {

        if(args.length != 1) {
            System.out.println("PEFCheck " + PEFCheck.class.getPackage().getImplementationVersion());
            System.out.println();
            System.out.println("java -jar pefcheck.jar [options] inputfile");

            System.exit(1);
        }

        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(args[0]);

            PEFCheck pefValidator = new PEFCheck();
            pefValidator.processDocument(xmlDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
