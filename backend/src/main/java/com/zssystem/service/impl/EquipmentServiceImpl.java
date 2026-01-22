package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.EquipmentQueryDTO;
import com.zssystem.dto.EquipmentSaveDTO;
import com.zssystem.entity.Equipment;
import com.zssystem.mapper.EquipmentMapper;
import com.zssystem.service.EquipmentService;
import com.zssystem.util.BeanUtil;
import com.zssystem.vo.EquipmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    @Autowired
    private EquipmentMapper equipmentMapper;

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put(0, "停用");
        STATUS_MAP.put(1, "正常");
        STATUS_MAP.put(2, "维修中");
    }

    @Override
    public IPage<EquipmentVO> getEquipmentList(EquipmentQueryDTO queryDTO) {
        Page<Equipment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getEquipmentNo() != null && !queryDTO.getEquipmentNo().isBlank(),
                        Equipment::getEquipmentNo, queryDTO.getEquipmentNo())
                .like(queryDTO.getEquipmentName() != null && !queryDTO.getEquipmentName().isBlank(),
                        Equipment::getEquipmentName, queryDTO.getEquipmentName())
                .like(queryDTO.getGroupName() != null && !queryDTO.getGroupName().isBlank(),
                        Equipment::getGroupName, queryDTO.getGroupName())
                .like(queryDTO.getMachineNo() != null && !queryDTO.getMachineNo().isBlank(),
                        Equipment::getMachineNo, queryDTO.getMachineNo())
                .eq(queryDTO.getStatus() != null, Equipment::getStatus, queryDTO.getStatus())
                .orderByDesc(Equipment::getCreateTime);

        IPage<Equipment> equipmentPage = equipmentMapper.selectPage(page, wrapper);
        return equipmentPage.convert(equipment -> {
            EquipmentVO vo = BeanUtil.copyProperties(equipment, EquipmentVO.class);
            vo.setStatusText(STATUS_MAP.getOrDefault(equipment.getStatus(), "未知"));
            return vo;
        });
    }

    @Override
    public EquipmentVO getEquipmentById(Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        EquipmentVO vo = BeanUtil.copyProperties(equipment, EquipmentVO.class);
        vo.setStatusText(STATUS_MAP.getOrDefault(equipment.getStatus(), "未知"));
        return vo;
    }

    @Override
    public EquipmentVO getEquipmentByNo(String equipmentNo) {
        Equipment equipment = equipmentMapper.selectOne(
            new LambdaQueryWrapper<Equipment>()
                .eq(Equipment::getEquipmentNo, equipmentNo)
        );
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        EquipmentVO vo = BeanUtil.copyProperties(equipment, EquipmentVO.class);
        vo.setStatusText(STATUS_MAP.getOrDefault(equipment.getStatus(), "未知"));
        return vo;
    }

    @Override
    @Transactional
    public void createEquipment(EquipmentSaveDTO saveDTO) {
        // 校验设备编号唯一性
        Equipment exist = equipmentMapper.selectOne(
            new LambdaQueryWrapper<Equipment>()
                .eq(Equipment::getEquipmentNo, saveDTO.getEquipmentNo())
        );
        if (exist != null) {
            throw new RuntimeException("设备编号已存在");
        }

        Equipment equipment = BeanUtil.copyProperties(saveDTO, Equipment.class);
        if (equipment.getStatus() == null) {
            equipment.setStatus(1); // 默认正常
        }
        equipmentMapper.insert(equipment);
    }

    @Override
    @Transactional
    public void updateEquipment(Long id, EquipmentSaveDTO saveDTO) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }

        // 如果设备编号有变化，校验唯一性
        if (!equipment.getEquipmentNo().equals(saveDTO.getEquipmentNo())) {
            Equipment exist = equipmentMapper.selectOne(
                new LambdaQueryWrapper<Equipment>()
                    .eq(Equipment::getEquipmentNo, saveDTO.getEquipmentNo())
            );
            if (exist != null) {
                throw new RuntimeException("设备编号已存在");
            }
        }

        BeanUtil.copyProperties(saveDTO, equipment, "id", "createTime", "updateTime", "deleted");
        equipmentMapper.updateById(equipment);
    }

    @Override
    @Transactional
    public void deleteEquipment(Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        // 检查是否有关联的点检记录、维护记录、故障记录等（简化处理，直接删除）
        equipmentMapper.deleteById(id);
    }
}
