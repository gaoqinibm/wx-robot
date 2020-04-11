package com.open;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.alibaba.fastjson.JSON;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @Description: 基于微信群的告警推送
 * @Author Baizhen
 * @Date 2020/4/11 15:41
 */
public class WxAppender extends UnsynchronizedAppenderBase<LoggingEvent> {

    private static final String WX_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=";
    private String pattern = "[%c%L] -%-5level [%thread] %msg%n";
    private PatternLayout layout;
    private String profilesActive = "unknown";
    private String wxKey = "unknown";
    private String appName = "unknown";
    private String env = "unknown";

    @Override
    protected void append(LoggingEvent eventObject) {
        try {
            if (Level.ERROR.equals(eventObject.getLevel())) {
                String toMsg = layout.doLayout(eventObject);
                sendWx(WX_URL + getWxKey(), toMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        PatternLayout patternLayout = new PatternLayout();
        patternLayout.setContext(context);
        patternLayout.setPattern(getPattern());
        patternLayout.start();
        this.layout = patternLayout;
        super.start();
    }

    public void sendWx(String webHookToken, String contentMsg) throws Exception {

        StringBuilder sb = new StringBuilder();
        String textCache = sb.append("【" + appName + "】")
                .append("【" + getIpAddress() + "】")
                .append("【" + env + "】").append("\n")
                .append(contentMsg).toString();

        String textMsg = "{\"msgtype\": \"text\",\"text\": {\"content\": " + JSON.toJSONString(textCache) + "}}";
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httppost = new HttpPost(webHookToken);
            httppost.addHeader("Content-Type", "application/json; charset=utf-8");
            StringEntity se = new StringEntity(textMsg, "utf-8");
            httppost.setEntity(se);

            httpclient.execute(httppost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP地址获取失败" + e.toString());
        }
        return "";
    }

    public String getProfilesActive() {
        return profilesActive;
    }

    public void setProfilesActive(String profilesActive) {
        this.profilesActive = profilesActive;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public PatternLayout getLayout() {
        return layout;
    }

    public void setLayout(PatternLayout layout) {
        this.layout = layout;
    }

    public String getWxKey() {
        return wxKey;
    }

    public void setWxKey(String wxKey) {
        this.wxKey = wxKey;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public static void main(String[] args) {
        WxAppender wxAppender = new WxAppender();
        try {
            wxAppender.sendWx("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=XXXXXXXXXXXXXXXXXX", "测试验证online异常日志推送");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
