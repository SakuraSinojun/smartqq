package com.scienjus.smartqq;

import com.scienjus.smartqq.callback.MessageCallback;
import com.scienjus.smartqq.client.SmartQQClient;
import com.scienjus.smartqq.model.*;

import java.io.IOException;
import java.util.List;

import java.io.BufferedReader;  
import java.io.IOException;  
import java.io.InputStream;  
import java.io.InputStreamReader;  

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


public class Application {
    private static SmartQQClient client = null;

    public static String parseMessage(String message) {
        // #车找人，晚上6:20，通州北苑-——富力新城——香河兴业，13041148458
        // #车找人，晚上5:55，大郊亭商务酒店-物美-兴业，18611551125
        // #车找人，今天下午17：30公益西桥回香河物美，走南四环，西集，有走的小窗或电联18210211498
        String str = null;
        if (message.startsWith("#")) {
            System.out.println("starts with #");
            try {
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpgets = new HttpGet("http://114.215.82.75/smartqq/testhttp.php?action=qqinfo&message=" + java.net.URLEncoder.encode(message, "UTF8"));
                HttpResponse response = httpclient.execute(httpgets);

                System.out.println("response = " + response);

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    str = EntityUtils.toString(entity);
                    // System.out.println("Do something");
                    System.out.println(str);
                    httpgets.abort();
                }
                httpgets.releaseConnection();
                httpclient.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
        return str;
    }

    public static void main(String[] args) {
        //创建一个新对象时需要扫描二维码登录，并且传一个处理接收到消息的回调，如果你不需要接收消息，可以传null
        client = new SmartQQClient(new MessageCallback() {
            @Override
            public void onMessage(Message message) {
                System.out.println("<" + message.getTime() + "> [" + message.getUserId() + "]: " + message.getContent());
                // client.sendMessageToFriend(message.getUserId(), message.getContent()) ;
                String response = Application.parseMessage(message.getContent());
                if (response != null) {
                    JSONObject json = JSON.parseObject(response);
                    Integer status = json.getInteger("status");
                    String reply = json.getString("reply");
                    if (status == 0) {
                        client.sendMessageToFriend(message.getUserId(), reply);
                    }
                }
            }

            @Override
            public void onGroupMessage(GroupMessage message) {
                // System.out.println(message.getContent());
                System.out.println("<" + message.getTime() + "> [" + message.getGroupId() + "|" + message.getUserId() + "]: " + message.getContent());
                Application.parseMessage(message.getContent());
            }

            @Override
            public void onDiscussMessage(DiscussMessage message) {
                // System.out.println(message.getContent());
                System.out.println("<" + message.getTime() + "> [" + message.getDiscussId() + "|" + message.getUserId() + "]: " + message.getContent());
                String response = Application.parseMessage(message.getContent());
                if (response != null) {
                    JSONObject json = JSON.parseObject(response);
                    Integer status = json.getInteger("status");
                    String reply = json.getString("reply");
                    if (status == 0) {
                        client.sendMessageToDiscuss(message.getDiscussId(), reply);
                    }
                }
            }
        });
        //登录成功后便可以编写你自己的业务逻辑了
        List<Category> categories = client.getFriendListWithCategory();
        for (Category category : categories) {
            System.out.println(category.getName());
            for (Friend friend : category.getFriends()) {
                System.out.println("————" + friend.getNickname());
            }
        }
        while (true) {
            try {
                java.lang.Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        //使用后调用close方法关闭，你也可以使用try-with-resource创建该对象并自动关闭
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
