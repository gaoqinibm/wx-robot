package com.open;

import org.junit.Test;

/**
 * 测试用例验证
 */
public class TestMessage {

    @Test
    public void testSendNewsMessage() throws Exception {

        WxAppender wxAppender = new WxAppender();
        try {
            wxAppender.sendWx("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=XXXXXXXXXXXXXXXXXX", "测试验证online异常日志推送");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}