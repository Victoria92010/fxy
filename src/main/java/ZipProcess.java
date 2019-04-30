import java.io.*;

/**
 * 对压缩包的处理
 * @author xinyifeng
 * @date 2019/05/01
 */

public class ZipProcess {

    /**
     * 压缩包解压缩
     * @param zipPath
     * @param exePath
     * @param command
     * @return
     */
    public static boolean unZip(String zipPath, String exePath, String command) {
        File zipFile = new File(zipPath);
        if (!zipFile.exists())
            return false;

        File zipExeFile = new File(exePath);
        String exec = String.format(command, zipExeFile.getAbsolutePath(), zipFile.getAbsolutePath(), zipFile.getParent());
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        int exitVal = 1;
        try {
            process = runtime.exec(exec);
            exitVal = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (exitVal == 0)
            return true;
        else
            return false;
    }

}