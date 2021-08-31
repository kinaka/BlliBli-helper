package top.misec.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import top.misec.login.Verify;

import java.io.IOException;

/**
 * @author Junzhou Liu
 * @create 2020/10/11 4:03
 */
public class HttpUtil {

    static Logger logger = (Logger) LogManager.getLogger(HttpUtil.class.getName());

    private static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36 Edg/85.0.564.70";

    public static String getUserAgent() {
        return userAgent;
    }

    public static void setUserAgent(String userAgent) {
        HttpUtil.userAgent = userAgent;
    }

    /**
     * 设置配置请求参数
     * 设置连接主机服务超时时间
     * 设置连接请求超时时间
     * 设置读取数据连接超时时间
     */
    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom().setConnectTimeout(5000)
            .setConnectionRequestTimeout(5000)
            .setSocketTimeout(10000)
            .build();

    static Verify verify = Verify.getInstance();

    HttpUtil() {

    }

    public static JsonObject doPost(String url, String requestBody) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpPostResponse = null;


        JsonObject resultJson = null;
        // 创建httpPost远程连接实例
        HttpPost httpPost = new HttpPost(url);

        // 设置请求头
        httpPost.setConfig(REQUEST_CONFIG);

        /*
          addHeader：添加一个新的请求头字段。（一个请求头中允许有重名字段。）
          setHeader：设置一个请求头字段，有则覆盖，无则添加。
          有什么好的方式判断key1=value和{"key1":"value"}
         */
        if (requestBody.startsWith("{")) {
            //java的正则表达式咋写......
            httpPost.setHeader("Content-Type", "application/json");
        } else {
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        }
        httpPost.setHeader("Referer", "https://www.bilibili.com/");
        httpPost.setHeader("Connection", "keep-alive");
        httpPost.setHeader("User-Agent", userAgent);
        httpPost.setHeader("Cookie", verify.getVerify());

        // 封装post请求参数

        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");

        httpPost.setEntity(stringEntity);

        try {
            // httpClient对象执行post请求,并返回响应参数对象
            httpPostResponse = httpClient.execute(httpPost);
            if (httpPostResponse != null) {
                int responseStatusCode = httpPostResponse.getStatusLine().getStatusCode();
                if (responseStatusCode == 200) {
                    // 从响应对象中获取响应内容
                    HttpEntity entity = httpPostResponse.getEntity();
                    String result = EntityUtils.toString(entity);
                    resultJson = new JsonParser().parse(result).getAsJsonObject();
                } else {
                    logger.debug(httpPostResponse.getStatusLine().toString());
                }
            } else {
                logger.debug("httpPostResponse null");
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        } finally {
            // 关闭资源
            httpResource(httpClient, httpPostResponse);
        }
        return resultJson;
    }

    public static JsonObject doGet(String url) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpGetResponse = null;
        JsonObject resultJson = null;
        try {
            // 通过址默认配置创建一个httpClient实例
            httpClient = HttpClients.createDefault();
            // 创建httpGet远程连接实例
            HttpGet httpGet = new HttpGet(url);
            // 设置请求头信息，鉴权
            httpGet.setHeader("Referer", "https://www.bilibili.com/");
            httpGet.setHeader("Connection", "keep-alive");
            httpGet.setHeader("User-Agent", userAgent);
            httpGet.setHeader("Cookie", verify.getVerify());
            // 为httpGet实例设置配置
            httpGet.setConfig(REQUEST_CONFIG);

            // 执行get请求得到返回对象
            httpGetResponse = httpClient.execute(httpGet);
            int responseStatusCode = httpGetResponse.getStatusLine().getStatusCode();

            if (responseStatusCode == 200) {
                // 从响应对象中获取响应内容
                // 通过返回对象获取返回数据
                HttpEntity entity = httpGetResponse.getEntity();
                // 通过EntityUtils中的toString方法将结果转换为字符串
                String result = EntityUtils.toString(entity);
                resultJson = new JsonParser().parse(result).getAsJsonObject();
            } else if (responseStatusCode == 412) {
                logger.info("出了一些问题，请在自定义配置中更换UA");
            } else {
                logger.debug(httpGetResponse.getStatusLine().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            httpResource(httpClient, httpGetResponse);
        }
        return resultJson;

    }


    private static void httpResource(CloseableHttpClient httpClient, CloseableHttpResponse response) {
        if (null != response) {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (null != httpClient) {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
