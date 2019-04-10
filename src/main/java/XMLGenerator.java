import java.io.File;
import java.io.StringWriter;
import java.util.logging.XMLFormatter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLGenerator {

    public String postXmlResponse(String file){
        String postXMLResponse = null;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            // Create File Metadata root element
            Element fileMetadata = doc.createElement("FileMetadata");
            doc.appendChild(fileMetadata);
            // Create File Size Element
            Element fileSize = doc.createElement("FileSize");
            fileSize.appendChild(doc.createTextNode(file.length() + " Bytes"));
            fileMetadata.appendChild(fileSize);
            // Transform Document to XML String
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            // Get the String value of final xml document
            postXMLResponse = writer.getBuffer().toString();
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
        System.out.println("personXMLStringValue = " + postXMLResponse);

        return postXMLResponse;
    }
}
