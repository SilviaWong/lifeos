package com.lifeos.business.service;

import com.lifeos.business.dto.AbortPomodoroRequest;
import com.lifeos.business.dto.FinishPomodoroRequest;
import com.lifeos.business.dto.PomodoroQueryRequest;
import com.lifeos.business.dto.StartPomodoroRequest;
import com.lifeos.business.dto.UpdatePomodoroSettingRequest;
import com.lifeos.business.vo.PomodoroRecordVO;
import com.lifeos.business.vo.PomodoroSettingVO;
import com.lifeos.business.vo.PomodoroSummaryVO;
import com.lifeos.common.api.PageResult;

/**
 * 番茄钟专注会话与偏好设置业务服务
 */
public interface PomodoroService {

    /**
     * 开启番茄钟会话（限制单用户同时最多一个 RUNNING/PAUSED 会话）
     */
    PomodoroRecordVO startSession(StartPomodoroRequest request);

    /**
     * 暂停当前进行中的会话
     */
    PomodoroRecordVO pauseSession(Long id);

    /**
     * 恢复/继续已暂停的会话
     */
    PomodoroRecordVO resumeSession(Long id);

    /**
     * 正常完成专注会话（进行服务端时间差严格校验）
     */
    PomodoroRecordVO finishSession(Long id, FinishPomodoroRequest request);

    /**
     * 中途放弃/中止专注会话
     */
    PomodoroRecordVO abortSession(Long id, AbortPomodoroRequest request);

    /**
     * 获取用户当前未结束的活动会话（供前端刷新或恢复状态）
     */
    PomodoroRecordVO getCurrentSession();

    /**
     * 获取指定 ID 会话详情
     */
    PomodoroRecordVO getSessionById(Long id);

    /**
     * 分页查询番茄钟历史记录
     */
    PageResult<PomodoroRecordVO> queryRecords(PomodoroQueryRequest request);

    /**
     * 获取今日与本周专注时长、完成次数统计摘要
     */
    PomodoroSummaryVO getSummary();

    /**
     * 获取番茄钟偏好配置
     */
    PomodoroSettingVO getSetting();

    /**
     * 修改番茄钟偏好配置
     */
    PomodoroSettingVO updateSetting(UpdatePomodoroSettingRequest request);
}

