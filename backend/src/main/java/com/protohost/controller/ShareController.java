package com.protohost.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.protohost.entity.Project;
import com.protohost.mapper.ProjectMapper;
import com.protohost.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final ProjectMapper projectMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.upload.base-path}")
    private String basePath;

    @GetMapping("/{slug}/meta")
    public ResponseEntity<Map<String, Object>> meta(@PathVariable String slug) {
        Project p = findBySlug(slug);
        return ResponseEntity.ok(Map.of(
                "name", p.getName(),
                "version", p.getVersion(),
                "isPublic", p.getIsPublic(),
                "entryFile", p.getEntryFile()
        ));
    }

    @PostMapping("/{slug}/verify")
    public ResponseEntity<Map<String, String>> verify(
            @PathVariable String slug,
            @RequestBody Map<String, String> body) {
        Project p = findBySlug(slug);
        if (p.getIsPublic() || p.getAccessPasswordHash() == null) {
            return ResponseEntity.ok(Map.of("viewToken", jwtUtil.generateViewToken(slug)));
        }
        if (!passwordEncoder.matches(body.get("password"), p.getAccessPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("message", "密码错误"));
        }
        return ResponseEntity.ok(Map.of("viewToken", jwtUtil.generateViewToken(slug)));
    }

    @GetMapping("/{slug}/files/**")
    public ResponseEntity<?> serveFile(
            @PathVariable String slug,
            @RequestParam(required = false) String viewToken,
            HttpServletRequest request) throws IOException {
        Project p = findBySlug(slug);

        String prefix = "/api/share/" + slug + "/files/";
        String uri = request.getRequestURI();
        String subPath = URLDecoder.decode(uri.substring(uri.indexOf(prefix) + prefix.length()), StandardCharsets.UTF_8);

        if (!p.getIsPublic()) {
            // 只保护 HTML 入口，子资源（css/js/img）直接放行
            // 安全性由入口 HTML 的 viewToken 保证，没有 HTML 无法使用子资源
            boolean isHtml = subPath.endsWith(".html") || subPath.endsWith(".htm");
            if (isHtml) {
                if (viewToken == null || !jwtUtil.isViewToken(viewToken, slug)) {
                    return ResponseEntity.status(403).build();
                }
            }
        }

        File baseDir = new File(basePath, p.getStoragePath()).getCanonicalFile();
        File target = new File(baseDir, subPath).getCanonicalFile();

        if (!target.getCanonicalPath().startsWith(baseDir.getCanonicalPath())) {
            return ResponseEntity.status(403).build();
        }
        if (!target.exists() || !target.isFile()) {
            return ResponseEntity.notFound().build();
        }

        // 对私密项目注入 viewToken
        if (!p.getIsPublic() && viewToken != null) {
            if (subPath.endsWith(".html") || subPath.endsWith(".htm")) {
                String html = readHtml(target);
                String injection = "<script>(function(){" +
                    "var vt='" + viewToken + "';" +
                    "function addVt(u){if(!u||u.indexOf('viewToken')>=0||u.startsWith('http')||u.startsWith('//')||u.startsWith('data:')||u.startsWith('#')||u==='about:blank')return u;" +
                    "return u+(u.indexOf('?')>=0?'&':'?')+'viewToken='+vt;}" +
                    "var _open=XMLHttpRequest.prototype.open;" +
                    "XMLHttpRequest.prototype.open=function(m,u){return _open.apply(this,[m,addVt(u)].concat([].slice.call(arguments,2)));};" +
                    "var _fetch=window.fetch;" +
                    "window.fetch=function(u,o){return _fetch.call(this,typeof u==='string'?addVt(u):u,o);};" +
                    "try{var sd=Object.getOwnPropertyDescriptor(HTMLIFrameElement.prototype,'src');" +
                    "Object.defineProperty(HTMLIFrameElement.prototype,'src',{set:function(u){sd.set.call(this,addVt(u));},get:function(){return sd.get.call(this);}});}catch(e){}" +
                    "window.__addVt=addVt;" +
                    "})();</script>";
                html = html.replace("</head>", injection + "</head>");
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("text/html; charset=utf-8"))
                        .body(html);
            }
            // 对 axplayer.js 注入：替换所有 mainFrame.contentWindow.location.href 赋值
            if (subPath.endsWith("axplayer.js")) {
                String js = readHtml(target);
                js = js.replace(
                    "mainFrame.contentWindow.location.href = linkUrlWithVars;",
                    "mainFrame.contentWindow.location.href = (window.__addVt ? window.__addVt(linkUrlWithVars) : linkUrlWithVars);"
                ).replace(
                    "mainFrame.contentWindow.location.href = urlToLoad;",
                    "mainFrame.contentWindow.location.href = (window.__addVt ? window.__addVt(urlToLoad) : urlToLoad);"
                ).replace(
                    "mainFrame.contentWindow.location.href = urlWithVars;",
                    "mainFrame.contentWindow.location.href = (window.__addVt ? window.__addVt(urlWithVars) : urlWithVars);"
                );
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("application/javascript; charset=utf-8"))
                        .body(js);
            }
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(detectContentType(subPath)))
                .body(new FileSystemResource(target));
    }

    private Project findBySlug(String slug) {
        Project p = projectMapper.selectOne(new LambdaQueryWrapper<Project>().eq(Project::getShareSlug, slug));
        if (p == null) throw new RuntimeException("项目不存在");
        return p;
    }

    private String readHtml(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                .decode(java.nio.ByteBuffer.wrap(bytes)).toString();
        } catch (java.nio.charset.CharacterCodingException e) {
            return new String(bytes, Charset.forName("GBK"));
        }
    }

    private String detectContentType(String path) {
        if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".woff2")) return "font/woff2";
        if (path.endsWith(".woff")) return "font/woff";
        if (path.endsWith(".ttf")) return "font/ttf";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}
