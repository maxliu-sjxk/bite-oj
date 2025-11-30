package com.bite.friend.service.user;

import com.bite.common.core.domain.R;
import com.bite.friend.domain.user.dto.UserSubmitDTO;
import com.bite.api.domain.vo.UserQuestionResultVO;

public interface IUserQuestionService {


    R<UserQuestionResultVO> submit(UserSubmitDTO userSubmitDTO);
}
