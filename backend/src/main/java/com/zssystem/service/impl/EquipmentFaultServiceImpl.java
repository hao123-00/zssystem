package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.EquipmentFaultQueryDTO;
import com.zssystem.dto.EquipmentFaultSaveDTO;
import com.zssystem.entity.Equipment;
import com.zssystem.entity.EquipmentFault;
import com.zssystem.mapper.EquipmentFaultMapper;
import com.zssystem.mapper.EquipmentMapper;
import com.zssystem.service.EquipmentFaultService;
import com.zssystem.util.BeanUtil;
import com.zssystem.vo.EquipmentFaultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class EquipmentFaultServiceImpl implements EquipmentFaultService {

    @Autowired
    private EquipmentFaultMapper faultMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put(0, "待处理");
        STATUS_MAP.put(1, "处理中");
        STATUS_MAP.put(2, "已处理");
    }

    @Override
    public IPage<EquipmentFaultVO> getFaultList(EquipmentFaultQueryDTO queryDTO) {
        Page<EquipmentFault> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<EquipmentFault> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getEquipmentId() != null, EquipmentFault::getEquipmentId, queryDTO.getEquipmentId())
                .eq(queryDTO.getStatus() != null, EquipmentFault::getStatus, queryDTO.getStatus())
                .ge(queryDTO.getStartDate() != null, EquipmentFault::getFaultDate, queryDTO.getStartDate())
                .le(queryDTO.getEndDate() != null, EquipmentFault::getFaultDate, queryDTO.getEndDate())
                .orderByDesc(EquipmentFault::getFaultDate)
                .orderByDesc(EquipmentFault::getCreateTime);

        IPage<EquipmentFault> faultPage = faultMapper.selectPage(page, wrapper);
        return faultPage.convert(fault -> {
            EquipmentFaultVO vo = BeanUtil.copyProperties(fault, EquipmentFaultVO.class);
            vo.setStatusText(STATUS_MAP.getOrDefault(fault.getStatus(), "未知"));
            // 填充设备信息
            if (fault.getEquipmentId() != null) {
                Equipment equipment = equipmentMapper.selectById(fault.getEquipmentId());
                if (equipment != null) {
                    vo.setEquipmentNo(equipment.getEquipmentNo());
                    vo.setEquipmentName(equipment.getEquipmentName());
                }
            }
            return vo;
        });
    }

    @Override
    public EquipmentFaultVO getFaultById(Long id) {
        EquipmentFault fault = faultMapper.selectById(id);
        if (fault == null) {
            throw new RuntimeException("故障记录不存在");
        }
        EquipmentFaultVO vo = BeanUtil.copyProperties(fault, EquipmentFaultVO.class);
        vo.setStatusText(STATUS_MAP.getOrDefault(fault.getStatus(), "未知"));
        // 填充设备信息
        if (fault.getEquipmentId() != null) {
            Equipment equipment = equipmentMapper.selectById(fault.getEquipmentId());
            if (equipment != null) {
                vo.setEquipmentNo(equipment.getEquipmentNo());
                vo.setEquipmentName(equipment.getEquipmentName());
            }
        }
        return vo;
    }

    @Override
    @Transactional
    public void createFault(EquipmentFaultSaveDTO saveDTO) {
        Equipment equipment = equipmentMapper.selectById(saveDTO.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        
        EquipmentFault fault = BeanUtil.copyProperties(saveDTO, EquipmentFault.class);
        if (fault.getStatus() == null) {
            fault.setStatus(0); // 默认待处理
        }
        faultMapper.insert(fault);
    }

    @Override
    @Transactional
    public void updateFault(Long id, EquipmentFaultSaveDTO saveDTO) {
        EquipmentFault fault = faultMapper.selectById(id);
        if (fault == null) {
            throw new RuntimeException("故障记录不存在");
        }
        
        BeanUtil.copyProperties(saveDTO, fault, "id", "createTime", "updateTime", "deleted");
        faultMapper.updateById(fault);
    }

    @Override
    @Transactional
    public void deleteFault(Long id) {
        EquipmentFault fault = faultMapper.selectById(id);
        if (fault == null) {
            throw new RuntimeException("故障记录不存在");
        }
        faultMapper.deleteById(id);
    }
}
