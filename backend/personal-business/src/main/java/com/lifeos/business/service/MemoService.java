package com.lifeos.business.service;

import com.lifeos.business.dto.CreateMemoRequest;
import com.lifeos.business.dto.MemoQueryRequest;
import com.lifeos.business.dto.UpdateMemoRequest;
import com.lifeos.business.vo.MemoVO;
import com.lifeos.common.api.PageResult;

/**
 * 个人备忘录服务
 */
public interface MemoService {

    MemoVO createMemo(CreateMemoRequest request);

    MemoVO updateMemo(Long id, UpdateMemoRequest request);

    void deleteMemo(Long id);

    MemoVO togglePin(Long id);

    MemoVO toggleArchive(Long id);

    MemoVO getMemo(Long id);

    PageResult<MemoVO> queryMemos(MemoQueryRequest request);
}

