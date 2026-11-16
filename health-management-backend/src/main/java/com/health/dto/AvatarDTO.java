package com.health.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 头像上传请求。前端压缩后的 base64 DataURL。
 */
@Data
public class AvatarDTO {

    /** 形如 data:image/jpeg;base64,xxxxx ，限制 200KB 以内的 base64 串 */
    @NotBlank(message = "头像数据不能为空")
    @Pattern(regexp = "^data:image/(png|jpeg|jpg|webp|gif);base64,.+$", message = "头像格式不正确，仅支持 png/jpeg/webp/gif")
    @Size(max = 200000, message = "头像过大，请使用压缩后的图片")
    private String avatar;
}
