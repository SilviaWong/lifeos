package com.lifeos.business.service;

import com.lifeos.business.dto.CreateDiaryRequest;
import com.lifeos.business.dto.DiaryQueryRequest;
import com.lifeos.business.dto.UpdateDiaryRequest;
import com.lifeos.business.vo.DiaryVO;
import com.lifeos.common.api.PageResult;

/**
 * 个人日记服务
 */
public interface DiaryService {

    DiaryVO createDiary(CreateDiaryRequest request);

    DiaryVO updateDiary(Long id, UpdateDiaryRequest request);

    void deleteDiary(Long id);

    DiaryVO getDiary(Long id);

    PageResult<DiaryVO> queryDiaries(DiaryQueryRequest request);
}

