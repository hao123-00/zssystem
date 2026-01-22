package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.EquipmentCheckQueryDTO;
import com.zssystem.dto.EquipmentCheckSaveDTO;
import com.zssystem.entity.Equipment;
import com.zssystem.entity.EquipmentCheck;
import com.zssystem.mapper.EquipmentCheckMapper;
import com.zssystem.mapper.EquipmentMapper;
import com.zssystem.service.EquipmentCheckService;
import com.zssystem.util.BeanUtil;
import com.zssystem.vo.EquipmentCheckVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
public class EquipmentCheckServiceImpl implements EquipmentCheckService {

    @Autowired
    private EquipmentCheckMapper checkMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public IPage<EquipmentCheckVO> getCheckList(EquipmentCheckQueryDTO queryDTO) {
        Page<EquipmentCheck> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<EquipmentCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getEquipmentNo() != null && !queryDTO.getEquipmentNo().isBlank(),
                        EquipmentCheck::getEquipmentNo, queryDTO.getEquipmentNo())
                .like(queryDTO.getEquipmentName() != null && !queryDTO.getEquipmentName().isBlank(),
                        EquipmentCheck::getEquipmentName, queryDTO.getEquipmentName())
                .eq(queryDTO.getCheckMonth() != null && !queryDTO.getCheckMonth().isBlank(),
                        EquipmentCheck::getCheckMonth, queryDTO.getCheckMonth())
                .like(queryDTO.getCheckerName() != null && !queryDTO.getCheckerName().isBlank(),
                        EquipmentCheck::getCheckerName, queryDTO.getCheckerName())
                .orderByDesc(EquipmentCheck::getCheckDate)
                .orderByDesc(EquipmentCheck::getCreateTime);

        IPage<EquipmentCheck> checkPage = checkMapper.selectPage(page, wrapper);
        return checkPage.convert(check -> BeanUtil.copyProperties(check, EquipmentCheckVO.class));
    }

    @Override
    public EquipmentCheckVO getCheckById(Long id) {
        EquipmentCheck check = checkMapper.selectById(id);
        if (check == null) {
            throw new RuntimeException("点检记录不存在");
        }
        return BeanUtil.copyProperties(check, EquipmentCheckVO.class);
    }

    @Override
    @Transactional
    public void saveCheck(EquipmentCheckSaveDTO saveDTO) {
        // 1. 查询设备信息
        Equipment equipment = equipmentMapper.selectById(saveDTO.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        
        // 2. 生成检查月份（YYYY-MM格式）
        String checkMonth = saveDTO.getCheckDate().format(MONTH_FORMATTER);
        
        // 3. 检查当天是否已有点检记录
        EquipmentCheck existCheck = checkMapper.selectOne(
            new LambdaQueryWrapper<EquipmentCheck>()
                .eq(EquipmentCheck::getEquipmentId, saveDTO.getEquipmentId())
                .eq(EquipmentCheck::getCheckDate, saveDTO.getCheckDate())
        );
        
        EquipmentCheck check;
        if (existCheck != null) {
            // 更新
            check = existCheck;
        } else {
            // 新增
            check = new EquipmentCheck();
        }
        
        // 4. 填充数据
        check.setEquipmentId(saveDTO.getEquipmentId());
        check.setEquipmentNo(equipment.getEquipmentNo());
        check.setEquipmentName(equipment.getEquipmentName());
        check.setCheckMonth(checkMonth);
        check.setCheckDate(saveDTO.getCheckDate());
        check.setCheckerName(saveDTO.getCheckerName());
        
        // 填充检查项
        check.setCircuitItem1(saveDTO.getCircuitItem1());
        check.setCircuitItem2(saveDTO.getCircuitItem2());
        check.setCircuitItem3(saveDTO.getCircuitItem3());
        check.setFrameItem1(saveDTO.getFrameItem1());
        check.setFrameItem2(saveDTO.getFrameItem2());
        check.setFrameItem3(saveDTO.getFrameItem3());
        check.setOilItem1(saveDTO.getOilItem1());
        check.setOilItem2(saveDTO.getOilItem2());
        check.setOilItem3(saveDTO.getOilItem3());
        check.setOilItem4(saveDTO.getOilItem4());
        check.setOilItem5(saveDTO.getOilItem5());
        check.setPeripheralItem1(saveDTO.getPeripheralItem1());
        check.setPeripheralItem2(saveDTO.getPeripheralItem2());
        check.setPeripheralItem3(saveDTO.getPeripheralItem3());
        check.setPeripheralItem4(saveDTO.getPeripheralItem4());
        check.setPeripheralItem5(saveDTO.getPeripheralItem5());
        
        check.setRemark(saveDTO.getRemark());
        
        if (existCheck != null) {
            checkMapper.updateById(check);
        } else {
            checkMapper.insert(check);
        }
    }

    @Override
    @Transactional
    public void deleteCheck(Long id) {
        EquipmentCheck check = checkMapper.selectById(id);
        if (check == null) {
            throw new RuntimeException("点检记录不存在");
        }
        checkMapper.deleteById(id);
    }
}
