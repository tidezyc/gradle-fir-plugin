package com.github.tidezyc

import groovy.json.JsonSlurper
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * @author tidezyc
 */
class FirUploadTask extends DefaultTask {

    @Input
    String appid
    @Input
    String token
    @Input
    String file

    HttpClient httpClient

    public FirUploadTask() {
        appid = ''
        token = ''
        file = ''
    }

    @TaskAction
    public void upload() {
        httpClient = new DefaultHttpClient();
        if (getAppid() == null || getToken() == null) {
            throw new RuntimeException("appid and token can't be null")
        }
        if (!new File(getFile()).exists()) {
            throw new RuntimeException("pkg file not exists!!!")
        }
        println "start upload to fir..."
        def info = getAppInfo();
        println("get app id:" + (info.id as String))
        if (info != null && info.bundle != null) {
            def result = uploadPkg(info.bundle as Map)
            if (result != null && result.code == 0) {
                println "Uploaded finish. wait update info..."
                updateInfo(info)
            } else {
                throw new RuntimeException("upload apk failed")
            }
        } else {
            throw new RuntimeException("get app info failed, please check appid and token")
        }
    }


    private Map getAppInfo() {
        println("get app info [" + getAppid() + "]")
        String url = "http://fir.im/api/v2/app/info/" + getAppid() + "?token=" + getToken() + "&type=android"
        HttpResponse response = httpClient.execute(new HttpGet(url));
        String result = EntityUtils.toString(response.getEntity(), "utf8");
        return new JsonSlurper().parseText(result) as Map;
    }

    private Map uploadPkg(Map bundle) {
        println("upload [" + getFile() + "]")
        def pkg = bundle.pkg
        HttpPost httpPost = new HttpPost(pkg.url as String);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addBinaryBody("file", new File(getFile()))
        entityBuilder.addTextBody("key", pkg.key as String)
        entityBuilder.addTextBody("token", pkg.token as String)
        httpPost.setEntity(entityBuilder.build());
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity resEntity = response.getEntity();

        if (resEntity != null) {
            def string = EntityUtils.toString(resEntity, "utf8");
            return new JsonSlurper().parseText(string) as Map;
        }
    }

    private void updateInfo(Map info) {
        String url = "http://fir.im/api/v2/app/" + (info.id as String) + "?token=" + getToken()
        HttpPut httpPut = new HttpPut(url)
        List<NameValuePair> pairList = new ArrayList<>()
        def date = new Date()
        def version = date.format("yyyyMMdd") + "_" + date.getTime()
        pairList.add(new BasicNameValuePair("version", version))
        pairList.add(new BasicNameValuePair("name", project.getName() + "_build_" + version))
        pairList.add(new BasicNameValuePair("changelog", version))
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(pairList)
        httpPut.setEntity(formEntity)
        HttpResponse response = httpClient.execute(httpPut);
        def string = EntityUtils.toString(response.getEntity(), "utf8")
        println "update fir success:" + string
    }
}
