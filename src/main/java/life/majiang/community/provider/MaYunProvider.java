package life.majiang.community.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import life.majiang.community.dto.AccessTokenDTO;
import life.majiang.community.dto.MaYunUser;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MaYunProvider {
    public String getAccessToken(AccessTokenDTO accessTokenDTO){
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(mediaType, JSON.toJSONString(accessTokenDTO));
        Request request = new Request.Builder()
                .url("https://gitee.com/oauth/token?grant_type=authorization_code")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String string = response.body().string();
            System.out.println(string);
            // String token = string.split("&")[0].split("=")[1];
            return string;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public MaYunUser getUser(String accessToken){
        JSONObject token = JSON.parseObject(accessToken);
        System.out.println(token.getString("access_token"));
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://gitee.com/api/v5/user?access_token="+ token.getString("access_token"))
                .build();
        System.out.println(request.url());
        try {
            Response response = client.newCall(request).execute();
            String string = response.body().string();
//            System.out.println(string);
            MaYunUser maYunUser = JSON.parseObject(string, MaYunUser.class);
//            System.out.println(maYunUser.getName());
            return maYunUser;
        } catch (IOException e) {
        }
        return null;
    }

}
