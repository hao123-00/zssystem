package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.Site5sCheckSaveDTO;
import com.zssystem.dto.Site5sCheckQueryDTO;
import com.zssystem.entity.Site5sCheck;
import com.zssystem.mapper.Site5sCheckMapper;
import com.zssystem.service.Site5sCheckService;
import com.zssystem.util.BeanUtil;
import com.zssystem.util.CodeGenerator;
import com.zssystem.vo.Site5sCheckVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class Site5sCheckServiceImpl implements Site5sCheckService {

    @Autowired
    private Site5sCheckMapper checkMapper;

    @Override
    public IPage<Site5sCheckVO> getCheckList(Site5sCheckQueryDTO queryDTO) {
        Page<Site5sCheck> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Site5sCheck> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(queryDTO.getCheckDate() != null, Site5sCheck::getCheckDate, queryDTO.getCheckDate())
                .like(queryDTO.getCheckArea() != null && !queryDTO.getCheckArea().isBlank(),
                        Site5sCheck::getCheckArea, queryDTO.getCheckArea())
                .like(queryDTO.getCheckerName() != null && !queryDTO.getCheckerName().isBlank(),
                        Site5sCheck::getCheckerName, queryDTO.getCheckerName())
                .orderByDesc(Site5sCheck::getCheckDate)
                .orderByDesc(Site5sCheck::getCreateTime);

        IPage<Site5sCheck> checkPage = checkMapper.selectPage(page, wrapper);
        return checkPage.convert(check -> BeanUtil.copyProperties(check, Site5sCheckVO.class));
    }

    @Override
    public Site5sCheckVO getCheckById(Long id) {
        Site5sCheck check = checkMapper.selectById(id);
        if (check == null) {
            throw new RuntimeException("5S检查记录不存在");
        }
        return BeanUtil.copyProperties(check, Site5sCheckVO.class);
    }

    @Override
    @Transactional
    public void saveCheck(Site5sCheckSaveDTO saveDTO) {
        Site5sCheck check;
        if (saveDTO.getId() != null) {
            // 更新
            check = checkMapper.selectById(saveDTO.getId());
            if (check == null) {
                throw new RuntimeException("5S检查记录不存在");
            }
        } else {
            // 新增
            check = new Site5sCheck();
            // 生成检查单号
            String checkNo = generateCheckNo();
            check.setCheckNo(checkNo);
        }
        
        // 填充数据
        check.setCheckDate(saveDTO.getCheckDate());
        check.setCheckArea(saveDTO.getCheckArea());
        check.setCheckerName(saveDTO.getCheckerName());
        check.setSortScore(saveDTO.getSortScore());
        check.setSetScore(saveDTO.getSetScore());
        check.setShineScore(saveDTO.getShineScore());
        check.setStandardizeScore(saveDTO.getStandardizeScore());
        check.setSustainScore(saveDTO.getSustainScore());
        
        // 计算总分
        int totalScore = 0;
        if (saveDTO.getSortScore() != null) totalScore += saveDTO.getSortScore();
        if (saveDTO.getSetScore() != null) totalScore += saveDTO.getSetScore();
        if (saveDTO.getShineScore() != null) totalScore += saveDTO.getShineScore();
        if (saveDTO.getStandardizeScore() != null) totalScore += saveDTO.getStandardizeScore();
        if (saveDTO.getSustainScore() != null) totalScore += saveDTO.getSustainScore();
        check.setTotalScore(totalScore);
        
        check.setProblemDescription(saveDTO.getProblemDescription());
        check.setRemark(saveDTO.getRemark());
        
        if (saveDTO.getId() != null) {
            checkMapper.updateById(check);
        } else {
            // 重试机制处理并发
            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    checkMapper.insert(check);
                    break;
                } catch (DuplicateKeyException e) {
                    if (i == maxRetries - 1) {
                        throw new RuntimeException("检查单号生成失败，请重试");
                    }
                    // 重新生成检查单号
                    check.setCheckNo(generateCheckNo());
                }
            }
        }
    }

    /**
     * 生成检查单号：5SCHECK + 日期 + 序号
     */
    private String generateCheckNo() {
        String prefix = "5SCHECK" + java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        Integer maxSequence = checkMapper.getMaxSequenceByPrefix(prefix);
        int nextSequence = (maxSequence == null ? 0 : maxSequence) + 1;
        
        return CodeGenerator.generate5sCheckNo(nextSequence);
    }

    @Override
    @Transactional
    public void deleteCheck(Long id) {
        Site5sCheck check = checkMapper.selectById(id);
        if (check == null) {
            throw new RuntimeException("5S检查记录不存在");
        }
        checkMapper.deleteById(id);
    }
}
