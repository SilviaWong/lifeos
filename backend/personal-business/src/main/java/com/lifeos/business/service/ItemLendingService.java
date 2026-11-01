package com.lifeos.business.service;

import com.lifeos.business.dto.CreateLendingRequest;
import com.lifeos.business.dto.LendingQueryRequest;
import com.lifeos.business.dto.ReturnLendingRequest;
import com.lifeos.business.vo.ItemLendingVO;
import com.lifeos.common.api.PageResult;

import java.util.List;

/**
 * 物品借还记录服务
 */
public interface ItemLendingService {

    ItemLendingVO lendItem(Long itemId, CreateLendingRequest request);

    ItemLendingVO returnItem(Long lendingId, ReturnLendingRequest request);

    List<ItemLendingVO> getLendingsByItem(Long itemId);

    PageResult<ItemLendingVO> queryLendings(LendingQueryRequest request);
}

