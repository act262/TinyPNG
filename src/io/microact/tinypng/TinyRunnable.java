package io.microact.tinypng;

import com.tinify.Result;
import com.tinify.Source;
import com.tinify.Tinify;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 压缩图片
 */
public class TinyRunnable implements Runnable {

    private List<File> mFiles;

    public TinyRunnable(List<File> files) {
        this.mFiles = files;
    }

    @Override
    public void run() {
        if (mFiles == null || mFiles.isEmpty()) return;

        for (File file : mFiles) {
            if (file == null || !file.exists()) {
                System.out.println(file + " File not exists");
                break;
            }
            try {
                Source source = Tinify.fromFile(file.getAbsolutePath());
                Result result = source.result();
                System.out.println("result.mediaType() = " + result.mediaType());
                System.out.println("result.size() = " + result.size());
                System.out.println("result.height() = " + result.height());
                System.out.println("result.width() = " + result.width());

                // 统计这个月已经压缩过图片的数量
                System.out.println("Compression-Count" + result);

                // 保存压缩后的文件
                File dir = new File(file.getParent() + "-compressed");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                result.toFile(dir.getAbsolutePath() + File.separator + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
