package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.Site5sRectificationQueryDTO;
import com.zssystem.dto.Site5sRectificationSaveDTO;
import com.zssystem.entity.Site5sCheck;
import com.zssystem.entity.Site5sRectification;
import com.zssystem.mapper.Site5sCheckMapper;
import com.zssystem.mapper.Site5sRectificationMapper;
import com.zssystem.service.Site5sRectificationService;
import com.zssystem.util.BeanUtil;
import com.zssystem.util.CodeGenerator;
import com.zssystem.vo.Site5sRectificationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class Site5sRectificationServiceImpl implements Site5sRectificationService {

    @Autowired
    private Site5sRectificationMapper rectificationMapper;

    @Autowired
    private Site5sCheckMapper checkMapper;

    // 状态文本映射
    private static final Map<Integer, String> STATUS_TEXT_MAP = new HashMap<>();
    static {
        STATUS_TEXT_MAP.put(0, "待整改");
        STATUS_TEXT_MAP.put(1, "整改中");
        STATUS_TEXT_MAP.put(2, "待验证");
        STATUS_TEXT_MAP.put(3, "已完成");
    }

    @Override
    public IPage<Site5sRectificationVO> getRectificationList(Site5sRectificationQueryDTO queryDTO) {
        Page<Site5sRectification> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Site5sRectification> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.like(queryDTO.getTaskNo() != null && !queryDTO.getTaskNo().isBlank(),
                        Site5sRectification::getTaskNo, queryDTO.getTaskNo())
                .like(queryDTO.getArea() != null && !queryDTO.getArea().isBlank(),
                        Site5sRectification::getArea, queryDTO.getArea())
                .like(queryDTO.getDepartment() != null && !queryDTO.getDepartment().isBlank(),
                        Site5sRectification::getDepartment, queryDTO.getDepartment())
                .like(queryDTO.getResponsiblePerson() != null && !queryDTO.getResponsiblePerson().isBlank(),
                        Site5sRectification::getResponsiblePerson, queryDTO.getResponsiblePerson())
                .eq(queryDTO.getStatus() != null, Site5sRectification::getStatus, queryDTO.getStatus())
                .le(queryDTO.getDeadline() != null, Site5sRectification::getDeadline, queryDTO.getDeadline())
                .orderByDesc(Site5sRectification::getCreateTime);

        IPage<Site5sRectification> rectificationPage = rectificationMapper.selectPage(page, wrapper);
        return rectificationPage.convert(rectification -> {
            Site5sRectificationVO vo = BeanUtil.copyProperties(rectification, Site5sRectificationVO.class);
            // 设置状态文本
            if (vo.getStatus() != null) {
                vo.setStatusText(STATUS_TEXT_MAP.getOrDefault(vo.getStatus(), "未知"));
            }
            // 设置关联的检查单号
            if (rectification.getCheckId() != null) {
                Site5sCheck check = checkMapper.selectById(rectification.getCheckId());
                if (check != null) {
                    vo.setCheckNo(check.getCheckNo());
                }
            }
            return vo;
        });
    }

    @Override
    public Site5sRectificationVO getRectificationById(Long id) {
        Site5sRectification rectification = rectificationMapper.selectById(id);
        if (rectification == null) {
            throw new RuntimeException("整改任务不存在");
        }
        Site5sRectificationVO vo = BeanUtil.copyProperties(rectification, Site5sRectificationVO.class);
        // 设置状态文本
        if (vo.getStatus() != null) {
            vo.setStatusText(STATUS_TEXT_MAP.getOrDefault(vo.getStatus(), "未知"));
        }
        // 设置关联的检查单号
        if (rectification.getCheckId() != null) {
            Site5sCheck check = checkMapper.selectById(rectification.getCheckId());
            if (check != null) {
                vo.setCheckNo(check.getCheckNo());
            }
        }
        return vo;
    }

    @Override
    @Transactional
    public void saveRectification(Site5sRectificationSaveDTO saveDTO) {
        Site5sRectification rectification;
        if (saveDTO.getId() != null) {
            // 更新
            rectification = rectificationMapper.selectById(saveDTO.getId());
            if (rectification == null) {
                throw new RuntimeException("整改任务不存在");
            }
        } else {
            // 新增
            rectification = new Site5sRectification();
            // 生成任务编号
            String taskNo = generateTaskNo();
            rectification.setTaskNo(taskNo);
        }
        
        // 填充数据
        rectification.setCheckId(saveDTO.getCheckId());
        rectification.setProblemDescription(saveDTO.getProblemDescription());
        rectification.setArea(saveDTO.getArea());
        rectification.setDepartment(saveDTO.getDepartment());
        rectification.setResponsiblePerson(saveDTO.getResponsiblePerson());
        rectification.setDeadline(saveDTO.getDeadline());
        rectification.setRectificationContent(saveDTO.getRectificationContent());
        rectification.setRectificationDate(saveDTO.getRectificationDate());
        rectification.setVerifierName(saveDTO.getVerifierName());
        rectification.setVerificationDate(saveDTO.getVerificationDate());
        rectification.setVerificationResult(saveDTO.getVerificationResult());
        rectification.setStatus(saveDTO.getStatus() != null ? saveDTO.getStatus() : 0);
        rectification.setRemark(saveDTO.getRemark());
        
        if (saveDTO.getId() != null) {
            rectificationMapper.updateById(rectification);
        } else {
            // 重试机制处理并发
            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    rectificationMapper.insert(rectification);
                    break;
                } catch (DuplicateKeyException e) {
                    if (i == maxRetries - 1) {
                        throw new RuntimeException("任务编号生成失败，请重试");
                    }
                    // 重新生成任务编号
                    rectification.setTaskNo(generateTaskNo());
                }
            }
        }
    }

    @Override
    @Transactional
    public void createFromCheck(Long checkId) {
        Site5sCheck check = checkMapper.selectById(checkId);
        if (check == null) {
            throw new RuntimeException("5S检查记录不存在");
        }
        
        // 创建整改任务
        Site5sRectificationSaveDTO saveDTO = new Site5sRectificationSaveDTO();
        saveDTO.setCheckId(checkId);
        saveDTO.setProblemDescription(check.getProblemDescription());
        saveDTO.setArea(check.getCheckArea());
        // 设置整改期限为检查日期后7天
        saveDTO.setDeadline(check.getCheckDate().plusDays(7));
        saveDTO.setStatus(0); // 待整改
        
        saveRectification(saveDTO);
    }

    /**
     * 生成任务编号：5STASK + 日期 + 序号
     */
    private String generateTaskNo() {
        String prefix = "5STASK" + java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        Integer maxSequence = rectificationMapper.getMaxSequenceByPrefix(prefix);
        int nextSequence = (maxSequence == null ? 0 : maxSequence) + 1;
        
        return CodeGenerator.generate5sTaskNo(nextSequence);
    }

    @Override
    @Transactional
    public void deleteRectification(Long id) {
        Site5sRectification rectification = rectificationMapper.selectById(id);
        if (rectification == null) {
            throw new RuntimeException("整改任务不存在");
        }
        rectificationMapper.deleteById(id);
    }
}
