package com.lifeos.business.controller;

import com.lifeos.business.dto.CreateItemRequest;
import com.lifeos.business.dto.CreateLendingRequest;
import com.lifeos.business.dto.ItemQueryRequest;
import com.lifeos.business.dto.UpdateItemRequest;
import com.lifeos.business.service.ItemLendingService;
import com.lifeos.business.service.PersonalItemService;
import com.lifeos.business.vo.ItemDetailVO;
import com.lifeos.business.vo.ItemLendingVO;
import com.lifeos.business.vo.LifeRecordSummaryVO;
import com.lifeos.business.vo.PersonalItemVO;
import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 个人物品与借还接口控制器
 */
@RestController
@RequestMapping("/api/items")
public class PersonalItemController {

    private final PersonalItemService personalItemService;
    private final ItemLendingService itemLendingService;

    public PersonalItemController(PersonalItemService personalItemService,
                                  ItemLendingService itemLendingService) {
        this.personalItemService = personalItemService;
        this.itemLendingService = itemLendingService;
    }

    /**
     * 新增个人物品
     */
    @PostMapping
    public ApiResponse<PersonalItemVO> createItem(@Valid @RequestBody CreateItemRequest request) {
        return ApiResponse.success(personalItemService.createItem(request));
    }

    /**
     * 更新物品信息
     */
    @PutMapping("/{id}")
    public ApiResponse<PersonalItemVO> updateItem(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateItemRequest request) {
        return ApiResponse.success(personalItemService.updateItem(id, request));
    }

    /**
     * 删除物品
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        personalItemService.deleteItem(id);
        return ApiResponse.success();
    }

    /**
     * 物品归档 / 恢复
     */
    @PutMapping("/{id}/archive")
    public ApiResponse<PersonalItemVO> toggleArchive(@PathVariable Long id) {
        return ApiResponse.success(personalItemService.toggleArchive(id));
    }

    /**
     * 获取物品详情（含历史借还）
     */
    @GetMapping("/{id}")
    public ApiResponse<ItemDetailVO> getItemDetail(@PathVariable Long id) {
        return ApiResponse.success(personalItemService.getItemDetail(id));
    }

    /**
     * 分页查询物品列表
     */
    @GetMapping
    public ApiResponse<PageResult<PersonalItemVO>> queryItems(ItemQueryRequest request) {
        return ApiResponse.success(personalItemService.queryItems(request));
    }

    /**
     * 获取生活记录统计摘要
     */
    @GetMapping("/summary")
    public ApiResponse<LifeRecordSummaryVO> getLifeRecordSummary() {
        return ApiResponse.success(personalItemService.getLifeRecordSummary());
    }

    /**
     * 登记借出目标物品
     */
    @PostMapping("/{id}/lendings")
    public ApiResponse<ItemLendingVO> lendItem(@PathVariable Long id,
                                               @Valid @RequestBody CreateLendingRequest request) {
        return ApiResponse.success(itemLendingService.lendItem(id, request));
    }

    /**
     * 查询目标物品的历史借还记录
     */
    @GetMapping("/{id}/lendings")
    public ApiResponse<List<ItemLendingVO>> getLendingsByItem(@PathVariable Long id) {
        return ApiResponse.success(itemLendingService.getLendingsByItem(id));
    }
}

