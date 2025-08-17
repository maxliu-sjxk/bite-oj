package com.bite.system.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysUserVO {
    @Schema(description = "管理员账号")
    private String userAccount;

    @Schema(description = "管理员昵称")
    private String nickName;
}
