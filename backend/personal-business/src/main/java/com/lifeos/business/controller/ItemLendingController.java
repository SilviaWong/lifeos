package com.lifeos.business.controller;

import com.lifeos.business.dto.LendingQueryRequest;
import com.lifeos.business.dto.ReturnLendingRequest;
import com.lifeos.business.service.ItemLendingService;
import com.lifeos.business.vo.ItemLendingVO;
import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 借还记录接口控制器
 */
@RestController
@RequestMapping("/api/lendings")
public class ItemLendingController {

    private final ItemLendingService itemLendingService;

    public ItemLendingController(ItemLendingService itemLendingService) {
        this.itemLendingService = itemLendingService;
    }

    /**
     * 登记归还已借出物品
     */
    @PostMapping("/{id}/return")
    public ApiResponse<ItemLendingVO> returnItem(@PathVariable Long id,
                                                 @RequestBody(required = false) ReturnLendingRequest request) {
        return ApiResponse.success(itemLendingService.returnItem(id, request));
    }

    /**
     * 分页查询所有借还记录
     */
    @GetMapping
    public ApiResponse<PageResult<ItemLendingVO>> queryLendings(LendingQueryRequest request) {
        return ApiResponse.success(itemLendingService.queryLendings(request));
    }
}

