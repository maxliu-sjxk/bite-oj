package com.bite.friend.domain.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

//    private String phone;

    @Email(message = "邮箱格式错误")
    private String email;

    private String code;

}
