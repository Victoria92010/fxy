import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;

import java.io.*;

/**
 * @author xinyifeng
 * @date 2019/05/01
 */

public class Index {

    public static void write(IndexWriter indexWriter, String filePath, FieldType fieldType) throws Exception {
        File file = new File(filePath);
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            if(line.equalsIgnoreCase("<doc>")) {
                String docId = bufferedReader.readLine().replaceAll("<DOCNO>|</DOCNO>|<docno>|</docno>", "");
                String url = bufferedReader.readLine().replaceAll("<URL>|</URL>|<url>|</url>", "");

                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null)
                    if(!line.equalsIgnoreCase("</doc>"))
                        stringBuilder.append(line + "\n");
                    else
                        break;
                String html = stringBuilder.toString();
                org.jsoup.nodes.Document document = Jsoup.parse(html, "utf-8");
                String charset = getCharset(document).toLowerCase();
                if (charset.equals("utf-8")) {
                    String body = textProcess(document);
                    if (!"".equals(body)) {
                        Document doc = new Document();
                        doc.add(new StringField("docno", docId, Field.Store.YES));
                        doc.add(new StringField("url", url, Field.Store.YES));
                        doc.add(new StoredField("html", html));
                        doc.add(new Field("body", body, fieldType));
                        indexWriter.addDocument(doc);
                    }
                }

            }
        }

        inputStreamReader.close();
    }

    /**
     * 判断html文件的编码方式
     * @param document
     * @return
     */
    public static String getCharset(org.jsoup.nodes.Document document) {
        String meta = document.getElementsByTag("meta").toString();
        int start = meta.indexOf("charset");
        if (start != -1) {
            int end = meta.substring(start).indexOf(">");
            if (end != -1)
                return meta.substring(start + 7, start + end).replaceAll("=|\"|\'|/|\\s*", "");
        }

        return "";
    }

    /**
     * 对html的文本进行处理
     * @param docment
     * @return
     */
    public static String textProcess(org.jsoup.nodes.Document docment) {
        try {
            String body = docment.text().replace(Jsoup.parse("&nbsp;").text(), " ").replaceAll("\\s+([\u4e00-\u9fa5])", "$1").replaceAll("\\s+", " ");
            if (!"".equals(body.replaceAll("\\s*", "")) && body != null)
                return body;
            else
                return "";
        }
        catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

}