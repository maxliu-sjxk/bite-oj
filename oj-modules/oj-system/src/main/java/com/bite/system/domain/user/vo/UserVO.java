package com.bite.system.domain.user.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserVO {

    private Long userId;

    private String nickName;

    private Integer sex;

    private String phone;

    private String email;

    private String wechat;

    private String schoolName;

    private String majorName;

    private String introduce;

    private Integer status;

}
