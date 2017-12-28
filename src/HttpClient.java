import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Main on 16/11/6.
 */
public class HttpClient {
    //单例
    private static HttpClient httpClient;

    /**
     * private 的构造函数
     */
    private HttpClient() {

    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    /**
     * 服务器存储的用户token
     */
    private String bearerToken;

    /**
     * 获取CustomHttpClient单例
     * @return HttpClient实例
     */
    public static synchronized HttpClient instance() {
        if (httpClient == null)
        {
            httpClient = new HttpClient();
        }
        return httpClient;
    }

    /**
     * Web Api Get方法
     * @param Uri 请求的Uri地址
     * @return 返回的数据
     */
    public String executeGet(String Uri) {
        try {
            URL url = new URL(Uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if (bearerToken != null)
            {
                conn.setRequestProperty("Authorization", "Bearer " + getBearerToken());
            }

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("Network exception:"+"Failed : HTTP error code : "
                        + conn.getResponseCode());
                return null;
            }

            String ans = new String(readStream(conn.getInputStream()));
            ans = ans.replace("\r\n","");

            //conn.disconnect();
            return ans;

        } catch (MalformedURLException e) {

            e.printStackTrace();
            return null;

        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取用户的token
     * @param Uri 请求的Uri地址
     * @param jsonEntity 请求的用户参数
     * @return 返回的数据
     */
    public String executeToken(String Uri, String jsonEntity) {
        try {
            URL url = new URL(Uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if (bearerToken != null)
            {
                conn.setRequestProperty("Authorization", "Bearer " + getBearerToken());
            }

            OutputStream os = conn.getOutputStream();
            os.write(jsonEntity.getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("Network Exception:"+"Failed : HTTP error code : "
                        + conn.getResponseCode());
                return null;
            }

            String ans = new String(readStream(conn.getInputStream()));
            ans = ans.replace("\r\n","");
            return ans;
        } catch (MalformedURLException e) {

            e.printStackTrace();
            return null;

        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }
    }

    public String executePost(String Uri) {
        try {
            URL url = new URL(Uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if (bearerToken != null)
            {
                conn.setRequestProperty("Authorization", "Bearer " + getBearerToken());
            }

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("Network exception:"+"Failed : HTTP error code : "
                        + conn.getResponseCode());
                return null;
            }

            String ans = new String(readStream(conn.getInputStream()));
            ans = ans.replace("\r\n","");

            //conn.disconnect();
            return ans;

        } catch (MalformedURLException e) {

            e.printStackTrace();
            return null;

        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }
    }

    private static byte[] readStream(InputStream inputStream) throws IOException{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            bout.write(buffer, 0, len);
        }
        bout.close();
        inputStream.close();
        return bout.toByteArray();
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}