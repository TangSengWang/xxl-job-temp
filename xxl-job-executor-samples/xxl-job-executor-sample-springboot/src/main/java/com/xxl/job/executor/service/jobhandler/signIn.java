package com.xxl.job.executor.service.jobhandler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * signInSite 地点说明
 *             其他 0  首自信 1  京唐2   迁钢3  首秦4  顺义5
 *
 * deptSid   部门说明
 *             mes 外协 325
 *             迁顺外协  322
 *             mes项目开发部  18
 */

@JobHandler(value = "signIn")
@Component
public class signIn extends IJobHandler {

    @Override
    public ReturnT<String> execute(String param) throws Exception {

        List<String> list = Arrays.asList(param.split(";"));
        if (null != list && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                String name = list.get(i);
                String[] split = name.split(",");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
                CloseableHttpClient httpClient = httpClientBuilder.build();
                String httpUrl = "http://api.goseek.cn/Tools/holiday?date=" + sdf.format(new Date());
                HttpPost workPost = new HttpPost(httpUrl);

                JSONObject workjson = JSON.parseObject(EntityUtils.toString(httpClient.execute(workPost).getEntity()));
                String workresult = workjson.getString("data");
                if (Integer.parseInt(workresult) == 0) {
                    String url = "http://10.5.210.67:8090/ecm-app/system/login/login.action";

                    HttpPost httpPost = new HttpPost(url);

                    httpPost.setHeader("Content-Type", "application/json");
                    httpPost.setHeader("charset", "UTF-8");

                    Map<String, String> map = new HashMap<>();
                    map.put("pin", split[0]);
                    map.put("password", "123456");
                    httpPost.setEntity(new StringEntity(JSONObject.toJSONString(map)));


                    HttpResponse result = httpClient.execute(httpPost);
                    String cookie = result.getFirstHeader("Set-Cookie").getValue();
                    String json = EntityUtils.toString(result.getEntity());
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    String meta = jsonObject.getString("meta");
                    JSONObject metajson = JSONObject.parseObject(meta);
                    System.out.println(metajson.getString("message"));
                    if ("1".equals(metajson.getString("code"))) {
                        String url2 = "http://10.5.210.67:8090/ecm-app/attendance/doSignIn.action";

                        HttpPost httpPost2 = new HttpPost(url2);

                        httpPost2.setHeader("Content-Type", "application/json");
                        httpPost2.setHeader("charset", "UTF-8");
                        Random rdint = new Random();
                        int index = rdint.nextInt(254);
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("deptSid", split[1]);
                        map2.put("sginInType", 1);
                        map2.put("signInIp", "10.1.43."+index);
                        map2.put("signInSite", split[2]);
                        map2.put("userPin", split[0]);
                        httpPost2.setEntity(new StringEntity(JSONObject.toJSONString(map2)));
                        httpPost2.addHeader(new BasicHeader("Cookie", cookie));


                        HttpResponse result2 = httpClient.execute(httpPost2);
                        String json2 = EntityUtils.toString(result2.getEntity());
                        System.out.println(json2);
                    }
                }
                Thread.sleep(1000);
            }
        }

        return SUCCESS;
    }

}
