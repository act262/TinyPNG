package io.microact.tinypng;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.StreamUtil;
import com.tinify.Tinify;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 执行压缩图片
 *
 * @author act262@gmail.com
 */
public class TinyAction extends AnAction {

    private static final String PROFILE = ".profile.ini";
    private static final String KEY_API_KEY = "apiKey";

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        String basePath = project.getBasePath();

        // 校验API key是否有效
        String apiKey = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            File profile = new File(basePath, PROFILE);
            if (!profile.exists()) {
                profile.createNewFile();
            }
            inputStream = new BufferedInputStream(new FileInputStream(profile));
            outputStream = new FileOutputStream(profile);
            Properties properties = new Properties();
            properties.load(inputStream);
            apiKey = properties.getProperty(KEY_API_KEY);

            if (apiKey == null || apiKey.isEmpty()) {
                apiKey = Messages.showInputDialog(project, "还没有API Key?", "请输入你的key", Messages.getQuestionIcon());
                try {
                    initClient(apiKey);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    Messages.showInfoMessage(e1.getMessage(), "");
                    return;
                }
                // save property
                properties.setProperty(KEY_API_KEY, apiKey);
                properties.store(outputStream, "TinyPNG API Key");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            Messages.showErrorDialog("发生错误了", "");
            return;
        } finally {
            StreamUtil.closeStream(inputStream);
            StreamUtil.closeStream(outputStream);
        }

        File baseDir = new File(basePath, "app/src/main/res/");
        File[] dirFiles = getMatchesDir(baseDir);
        if (dirFiles == null || dirFiles.length == 0) {
            Messages.showInfoMessage("没有文件", "");
            return;
        }

        List<File> fileList = new ArrayList<>();
        for (File dir : dirFiles) {
            File[] files = getFiles(dir);
            fileList.addAll(Arrays.asList(files));
        }

        // all ready go
        asyncTask(new TinyRunnable(fileList));
    }

    private void initClient(String key) throws Exception {
        Tinify.setKey(key);
        Tinify.setAppIdentifier("TinyPNG4AndroidStudio");

        boolean validate = Tinify.validate();
        // API KEY 无效！
//        if (!validate) {
//            Messages.showErrorDialog("API KEY Invalid", "");
//        }
    }

    private File[] getMatchesDir(File baseDir) {
        return baseDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String dirName = pathname.getName();
                boolean matches = dirName.matches("mipmap-.+") || dirName.matches("drawable[-]?.*");
                return pathname.exists() && pathname.isDirectory() && matches;
            }
        });
    }

    private File[] getFiles(File dirFile) {
        return dirFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                System.out.println("dir = [" + dir + "], name = [" + name + "]");
                // 匹配png、jpg文件,不匹配.9文件
                boolean matches = name.matches(".*\\.png$");
                matches = name.endsWith(".jpg") || (name.endsWith(".png") && !name.endsWith(".9.png"));
                return matches;
            }
        });
    }

    /**
     * 异步执行
     *
     * @param runnable
     */
    public void asyncTask(Runnable runnable) {
        ApplicationManager.getApplication().executeOnPooledThread(runnable);
    }

    public void invokeLater(Runnable runnable) {
        ApplicationManager.getApplication().invokeLater(runnable);
    }

    public void readAction(Runnable runnable) {
        ApplicationManager.getApplication().runReadAction(runnable);
    }

    public void writeAction(Runnable runnable) {
        ApplicationManager.getApplication().runWriteAction(runnable);
    }
}
