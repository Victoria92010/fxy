import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 对文件的处理
 * @author xinyifeng
 * @date 2019/05/01
 */

public class FileProcess {

    /**
     * 文件夹中随机抽取文件
     * @param dir
     * @return
     */
    public static List<String> randomFiles(String dir, Double rate) {
        List<String> selectedFiles = new ArrayList<String>();

        File file = new File(dir);
        String[] filenames = file.list();
        int num = filenames.length;
        Random random = new Random();
        for (int i = 0; i < rate * num; i++) {
            int j = random.nextInt(num);
            selectedFiles.add(filenames[j]);
            System.out.println(filenames[j]);
        }
        System.out.println("Selected file num: " + selectedFiles.size() + "\n");

        return selectedFiles;
    }

    /**
     * 文件重命名
     * @param oldName
     * @param newName
     */
    public static void rename(String oldName, String newName) {
        if (!oldName.equals(newName)) {
            File file = new File(oldName);
            file.renameTo(new File(newName));
        }
    }

    /**
     * 删除文件
     * @param name
     */
    public static void deleteFile(String name) {
        File file = new File(name);
        if (file.exists())
            file.delete();
    }

    /**
     * 合并索引包
     * @param indexDir
     * @param indexName
     * @param analyzer
     * @throws Exception
     */
    public static void Merge(String indexDir, String indexName, Analyzer analyzer) throws Exception {
        Boolean flag = true;
        File file = new File(indexDir);
        String[] filenames = file.list();
        if (filenames.length <= 1)
            return;

        for (String filename : filenames) {
            if (filename.equals(indexName)) {
                flag = false;
                break;
            }
        }
        if (flag)
            rename(filenames[0], indexName);

        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter indexWriter = new IndexWriter(FSDirectory.open(Paths.get(indexName)), indexWriterConfig);

        for (String filename : filenames) {
            System.out.println(filename);
            Directory subIndexDir = FSDirectory.open(Paths.get(filename));
            indexWriter.addIndexes(subIndexDir);
            indexWriter.commit();
        }

        indexWriter.close();
    }

}