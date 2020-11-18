package life.majiang.community.controller;

import life.majiang.community.dto.AccessTokenDTO;
import life.majiang.community.dto.GithubUser;
import life.majiang.community.dto.MaYunUser;
import life.majiang.community.model.User;
import life.majiang.community.provider.GithubProvider;
import life.majiang.community.provider.MaYunProvider;
import life.majiang.community.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Created by codedrinker on 2019/4/24.
 */
@Controller
@Slf4j
public class AuthorizeController {

    @Autowired
    private GithubProvider githubProvider;

    @Value("${github.client.id}")
    private String gitClientId;
    @Value("${github.client.secret}")
    private String gitClientSecret;
    @Value("${github.redirect.uri}")
    private String gitRedirectUri;

    @Autowired
    private MaYunProvider maYunProvider;

    @Value("${maYun.clientId}")
    private String maYunClientId;
    @Value("${maYun.clientSecret}")
    private String maYunClientSecret;
    @Value("${maYun.redirectUrl}")
    private String maYunRedirectUri;
    @Autowired
    private UserService userService;

    @GetMapping("/maYunCallback")
    public String callback(@RequestParam(name = "code") String code,
//                           @RequestParam(name = "state") String state,
                           HttpServletResponse response) {
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setClient_id(maYunClientId);
        accessTokenDTO.setClient_secret(maYunClientSecret);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(maYunRedirectUri);
//        accessTokenDTO.setState(state);
        String accessToken = maYunProvider.getAccessToken(accessTokenDTO);
        MaYunUser maYunUser = maYunProvider.getUser(accessToken);
        if (maYunUser != null && maYunUser.getId() != null) {
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setName(maYunUser.getName());
            user.setAccountId(String.valueOf(maYunUser.getId()));
            user.setAvatarUrl(maYunUser.getAvatarUrl());
            userService.createOrUpdate(user);
            Cookie cookie = new Cookie("token", token);
            cookie.setMaxAge(60 * 60 * 24 * 30 * 6);
            response.addCookie(cookie);
            return "redirect:/";
        } else {
            log.error("callback get github error,{}", maYunUser);
            // 登录失败，重新登录
            return "redirect:/";
        }
    }

    @GetMapping("/gitCallback")
    public String callback(@RequestParam(name = "code") String code,
                           @RequestParam(name = "state") String state,
                           HttpServletResponse response) {
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setClient_id(gitClientId);
        accessTokenDTO.setClient_secret(gitClientSecret);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(gitRedirectUri);
        accessTokenDTO.setState(state);
        String accessToken = githubProvider.getAccessToken(accessTokenDTO);
        GithubUser githubUser = githubProvider.getUser(accessToken);
        if (githubUser != null && githubUser.getId() != null) {
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setName(githubUser.getName());
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setAvatarUrl(githubUser.getAvatarUrl());
            userService.createOrUpdate(user);
            Cookie cookie = new Cookie("token", token);
            cookie.setMaxAge(60 * 60 * 24 * 30 * 6);
            response.addCookie(cookie);
            return "redirect:/";
        } else {
            log.error("callback get github error,{}", githubUser);
            // 登录失败，重新登录
            return "redirect:/";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response) {
        request.getSession().removeAttribute("user");
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }
}