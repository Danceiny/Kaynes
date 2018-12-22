package cc.cannot.dict.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ApiIgnore
@RestController
@RequestMapping("/api/dict/documentation")
@Slf4j
public class DocumentationController {
    private static final String HOME_DOC_URL = "https://gitlab.baixing.cn/tidy/kaynes";
    private static final String BUSINESS_DOC_URL = "https://gitlab.baixing.cn/tidy/kaynes/tree/master/kaynes-business";
    private static final String WEB_DOC_URL = "https://gitlab.baixing.cn/tidy/kaynes/tree/master/kaynes-web";

    @GetMapping("/{index}")
    public void index(@PathVariable String index, HttpServletResponse response) throws IOException {
        String docUrl;
        switch (index) {
            case "api":
                docUrl = WEB_DOC_URL;
                break;
            case "redis":
                docUrl = BUSINESS_DOC_URL + "#redis缓存结构";
                break;
            case "tree":
                docUrl = BUSINESS_DOC_URL + "#basetree代码导读";
                break;
            default:
                docUrl = HOME_DOC_URL;
                break;
        }
        response.sendRedirect(docUrl);
    }
}
