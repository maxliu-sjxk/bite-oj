package com.bite.friend.service.user;

import com.bite.common.core.domain.PageQueryDTO;
import com.bite.common.core.domain.TableDataInfo;

public interface IUserMessageService {
    TableDataInfo list(PageQueryDTO pageQueryDTO);
}
