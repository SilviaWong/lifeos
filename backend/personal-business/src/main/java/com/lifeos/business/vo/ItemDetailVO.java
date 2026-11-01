package com.lifeos.business.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 物品详情 VO（包含物品基本信息、当前借出状态及历史借还记录）
 */
public class ItemDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private PersonalItemVO item;
    private ItemLendingVO activeLending;
    private List<ItemLendingVO> lendingHistory = new ArrayList<>();

    public ItemDetailVO() {
    }

    public ItemDetailVO(PersonalItemVO item, ItemLendingVO activeLending, List<ItemLendingVO> lendingHistory) {
        this.item = item;
        this.activeLending = activeLending;
        this.lendingHistory = lendingHistory != null ? lendingHistory : new ArrayList<>();
    }

    public PersonalItemVO getItem() {
        return item;
    }

    public void setItem(PersonalItemVO item) {
        this.item = item;
    }

    public ItemLendingVO getActiveLending() {
        return activeLending;
    }

    public void setActiveLending(ItemLendingVO activeLending) {
        this.activeLending = activeLending;
    }

    public List<ItemLendingVO> getLendingHistory() {
        return lendingHistory;
    }

    public void setLendingHistory(List<ItemLendingVO> lendingHistory) {
        this.lendingHistory = lendingHistory;
    }
}

