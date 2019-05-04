import org.ansj.lucene6.AnsjAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.List;

/**
 * @author xinyifeng
 * @date 2019/05/01
 */

public class Pack {

    private static final String dir = "E:/SogouT/";                      //数据存放路径
    private static final String newName = dir + "pages.001";
    private static final String filePath = dir + "pages";
    private static final String indexDir = "H:/Index/";                  //索引包存放路径
    private static final String indexName = indexDir + "indexSogou";     //最终索引包名称
    private static final String fileDir = "H:/html_files/";              //html文件存储路径
    private static final String winrarExePath = "C:/Program Files (x86)/WinRAR/WinRAR.exe";
    private static final String winrarCommand = "%s x %s %s";
    private static final String sevenZipExePath = "C:/Program Files/7-Zip/7z.exe";
    private static final String sevenZipCommand = "%s x -aou \"%s\" -o\"%s\"";

    public static void main(String[] args) {
        String subIndexDir = indexDir + "indexSogou" + getRandomNumber(4);

        Analyzer analyzer = new AnsjAnalyzer(AnsjAnalyzer.TYPE.nlp_ansj);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter indexWriter = null;
        try {
            Directory directory = FSDirectory.open(Paths.get(subIndexDir));
            indexWriter = new IndexWriter(directory, indexWriterConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }

        FieldType fieldType = new FieldType(TextField.TYPE_STORED);
        fieldType.setStoreTermVectors(true);
        fieldType.setStoreTermVectorOffsets(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.setStoreTermVectorPayloads(true);
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        fieldType.freeze();

        List<String> selectedZips = FileProcess.randomFiles(dir, 0.0625);
        for (int i = 0; i < selectedZips.size(); i++) {
            String rarPath = selectedZips.get(i);
            //解压rar文件，得到7z文件
            ZipProcess.unZip(rarPath, winrarExePath, winrarCommand);
            String sevenZipPath = rarPath.substring(0, rarPath.length() - 3);
            //7z文件重命名，分卷文件完整(.001)才能解压
            FileProcess.rename(sevenZipPath, newName);
            //解压7z文件，得到html文件
            ZipProcess.unZip(newName, sevenZipExePath, sevenZipCommand);
            FileProcess.deleteFile(newName);

            try {
                Index.write(indexWriter, filePath, fieldType, fileDir);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            FileProcess.deleteFile(filePath);
        }

        try {
            indexWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                FileProcess.Merge(indexDir, indexName, analyzer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 随机生成一个指定长度的数字
     * @param length
     * @return
     */
    public static int getRandomNumber(int length) {
        return (int)((Math.random() * 9 + 1) * Math.pow(10, length));
    }

}