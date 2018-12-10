package ru.kvinsoft.creanis.xmltest;


import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("MagicConstant")
public class Program {

    public static void main(String[] args) throws IOException {

        Files.list(Paths.get("c:\\Develop\\Export\\"))
                .filter((p) -> Pattern.matches("^[kK][pP](\\d+)\\.[dD][wW][gG].[xX][mM][lL]$", p.getFileName().toString()))
                .forEach((p) -> getOidsFromDrawings(p.toString()));
//        TestExportXML();
    }

    private static void getOidsFromDrawings(String filename) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        // включаем поддержку пространства имен XML
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;

        try {
            builder = builderFactory.newDocumentBuilder();
            doc = builder.parse(filename);

            // Создаем объект XPathFactory
            XPathFactory xpathFactory = XPathFactory.newInstance();

            // Получаем экзмепляр XPath для создания
            // XPathExpression выражений
            XPath xpath = xpathFactory.newXPath();

            List<String> opticalNodeOids = getOidFromOpticalNodes(doc, xpath);

            for (String opticalNodeOid : opticalNodeOids) {
                System.out.println(opticalNodeOid);
            }


        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }


    private static List<String> getOidFromOpticalNodes(Document doc, XPath xpath){
        List<String> list = new ArrayList<>();


        try {
            //создаем объект XPathExpression
            XPathExpression xPathExpression = xpath.compile(
                    "Drawing/Amplifiers/Amplifier[BlockName='AMP1_20' or BlockName='AMP1_19']/EstateDatas/Oid/text()"
            );

            // получаем список всех тегов, которые отвечают условию
            NodeList nodes = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);

            // проходим по списку и получаем значение с помощью getNodeValue()
            for (int i = 0; i < nodes.getLength(); i++)
                list.add(doc.getDocumentURI() + " \t " + nodes.item(i).getNodeValue());

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        if(list.isEmpty()){
                list.add(doc.getDocumentURI() + " \t Optical node not found.");
        }

        return list;
    }


  // Метод для проверки чертежей на содержание корректных экспортных файлов путём сравнения даты создания файлов
    private static void TestExportXML() throws IOException {
        //        ArrayList<Path> paths = new ArrayList<Path>();

        ArrayList<String> projects = new ArrayList<>();
        for(int i = 1; i<23; i++){
            if(i != 1 && i != 4 && i !=14 && i != 16 && i != 20 ) {
                projects.add(String.format("KP_%03d", i));
            }
        }

        Counter cAllItog = new Counter();
        Counter cAllCopy = new Counter();

        for (String project : projects) {
            Counter cItog = new Counter();
            Counter cCopy = new Counter();

            String project_path = String.format("y:\\PLC4.1\\Projects_PLC\\%s\\export_data\\", project);

            if(Files.exists(Paths.get(project_path)))
            {
                Files.list(Paths.get(project_path))
                        .filter((p) -> Pattern.matches("^[kK][pP](\\d+)\\.[dD][wW][gG].[xX][mM][lL]$", p.getFileName().toString()))
                        .forEach((p) -> {
                            String sPath = p.getFileName().toString();
                            Path pathDWG = Paths.get(String.format("y:\\PLC4.1\\Projects_PLC\\%s\\", project), sPath.substring(0, sPath.length() - 4));

                            try {
                                FileTime ftXML = Files.getLastModifiedTime(p, LinkOption.NOFOLLOW_LINKS);
                                FileTime ftDWG = Files.getLastModifiedTime(pathDWG, LinkOption.NOFOLLOW_LINKS);

                                if (ftXML.toMillis() < (ftDWG.toMillis() - 32000000)) {
                                System.out.println(p.getFileName());
//                                System.out.printf("Файл %s копируем так как %s \t %s \n", p.getFileName(),ftXML.toString(), ftDWG.toString());
                                    cCopy.increase();
                                }
                                cItog.increase();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
            if(cItog.get() != 0) {
                cAllItog.add(cItog.get());

                cAllCopy.add(cCopy.get());
                System.out.printf("В папке %s к экспорту годятся %3d из %3d файлов.\n", project_path, cCopy.get(), cItog.get());
            }
        }
//        System.out.printf("Всего к экспорту годятся %3d из %3d файлов.\n", cAllCopy.get(), cAllItog.get());
    }


    public static void copyXML(Path path) {
        System.out.println(path.getFileName());
    }
}



