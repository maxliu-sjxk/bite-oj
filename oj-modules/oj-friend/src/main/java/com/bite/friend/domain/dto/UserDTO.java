package com.bite.friend.domain.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

//    private String phone;

    //TODO 确认是否能够将该message返回给前端
    @Email(message = "邮箱格式错误")
    private String email;
}
