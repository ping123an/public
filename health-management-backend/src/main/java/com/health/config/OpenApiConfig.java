package com.health.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc / Swagger UI 接口文档配置
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI healthOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("个人健康管理系统 API")
                        .description("轻量级个人健康管理系统后端接口文档<br>"
                                + "使用说明：先调用 /auth/register 或 /auth/login 获取 token，"
                                + "再点击右上角 Authorize 按钮，输入 token 值即可调试全部受保护接口。<br>"
                                + "演示账号：carol / 123456")
                        .version("v1.0"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("登录接口返回的 JWT token，直接粘贴 token 字符串即可（无需手动加 Bearer 前缀）")));
    }
}
