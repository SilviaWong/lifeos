package com.lifeos.business.service;

import com.lifeos.business.dto.CreateItemRequest;
import com.lifeos.business.dto.ItemQueryRequest;
import com.lifeos.business.dto.UpdateItemRequest;
import com.lifeos.business.vo.ItemDetailVO;
import com.lifeos.business.vo.LifeRecordSummaryVO;
import com.lifeos.business.vo.PersonalItemVO;
import com.lifeos.common.api.PageResult;

/**
 * 个人物品管理服务
 */
public interface PersonalItemService {

    PersonalItemVO createItem(CreateItemRequest request);

    PersonalItemVO updateItem(Long id, UpdateItemRequest request);

    void deleteItem(Long id);

    PersonalItemVO toggleArchive(Long id);

    ItemDetailVO getItemDetail(Long id);

    PageResult<PersonalItemVO> queryItems(ItemQueryRequest request);

    LifeRecordSummaryVO getLifeRecordSummary();
}

