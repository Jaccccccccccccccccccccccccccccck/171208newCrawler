import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

public class DownloadURLFile {
    public static String downloadFromUrl(String url,String dir) {

        try {
            URL httpurl = new URL(url);
            String fileName = getFileNameFromUrl(url);
            File f = new File(dir + fileName);
            FileUtils.copyURLToFile(httpurl, f);
        } catch (Exception e) {
            e.printStackTrace();
            return "false";
        }
        return "success";
    }
    public static String returnname(String url) {
        String fileName;
        try {
            URL httpurl = new URL(url);
             fileName = getFileNameFromUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
            return "00000000000000000000";
        }
        return fileName;
    }
    public static String getFileNameFromUrl(String url){
        String name = new Long(System.currentTimeMillis()).toString() + ".X";
        int index = url.lastIndexOf("/");
        if(index > 0){
            name = url.substring(index + 1);
            if(name.trim().length()>0){
                return name;
            }
        }
        return name;
    }
}

