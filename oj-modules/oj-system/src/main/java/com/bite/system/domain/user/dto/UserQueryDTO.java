package com.bite.system.domain.user.dto;

import com.bite.common.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserQueryDTO extends PageQueryDTO {

    private Long userId;

    private String nickName;
}
