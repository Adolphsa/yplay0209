package com.yeejay.yplay.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
        public static void zip(String src, String dest) throws IOException {
        //提供了一个数据项压缩成一个ZIP归档输出流  
        ZipOutputStream out = null;
        try {
            File outFile = new File(dest);//源文件或者目录  
            File fileOrDirectory = new File(src);//压缩文件路径  
            out = new ZipOutputStream(new FileOutputStream(outFile));
            //如果此文件是一个文件，否则为false。  
            if (fileOrDirectory.isFile()) {
                zipFileOrDirectory(out, fileOrDirectory, "");
            } else {
            //返回一个文件或空阵列。  
                File[] entries = fileOrDirectory.listFiles();
                for (int i = 0; i < entries.length; i++) {
                    // 递归压缩，更新curPaths  
                    zipFileOrDirectory(out, entries[i], "");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //关闭输出流  
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        }

    private static void zipFileOrDirectory(ZipOutputStream out,
        File fileOrDirectory, String curPath) throws IOException {
        //从文件中读取字节的输入流  
        FileInputStream in = null;
        try {
            //如果此文件是一个目录，否则返回false。  
            if (!fileOrDirectory.isDirectory()) {
                // 压缩文件  
                byte[] buffer = new byte[4096];
                int bytes_read;
                in = new FileInputStream(fileOrDirectory);
                //实例代表一个条目内的ZIP归档  
                ZipEntry entry = new ZipEntry(curPath + fileOrDirectory.getName());
                //条目的信息写入底层流  
                out.putNextEntry(entry);
                while ((bytes_read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes_read);
                }
                out.closeEntry();
            } else {
                // 压缩目录  
                File[] entries = fileOrDirectory.listFiles();
                for (int i = 0; i < entries.length; i++) {
                    // 递归压缩，更新curPaths  
                    zipFileOrDirectory(out, entries[i], curPath
                            + fileOrDirectory.getName() + "/");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            // throw ex;  
        } finally {
            if (in != null) {
               try{
                   in.close();
               } catch (IOException ex) {
                 ex.printStackTrace();
               }
            }
        }
    }

    public static byte[] File2byte(String filePath) {
        byte[] buffer = null;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try{
                    fis.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            if (bos != null) {
                try{
                    bos.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return buffer;
    }
}