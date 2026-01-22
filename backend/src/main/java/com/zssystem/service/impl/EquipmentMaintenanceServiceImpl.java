package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.EquipmentMaintenanceQueryDTO;
import com.zssystem.dto.EquipmentMaintenanceSaveDTO;
import com.zssystem.entity.Equipment;
import com.zssystem.entity.EquipmentMaintenance;
import com.zssystem.mapper.EquipmentMapper;
import com.zssystem.mapper.EquipmentMaintenanceMapper;
import com.zssystem.service.EquipmentMaintenanceService;
import com.zssystem.util.BeanUtil;
import com.zssystem.vo.EquipmentMaintenanceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipmentMaintenanceServiceImpl implements EquipmentMaintenanceService {

    @Autowired
    private EquipmentMaintenanceMapper maintenanceMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Override
    public IPage<EquipmentMaintenanceVO> getMaintenanceList(EquipmentMaintenanceQueryDTO queryDTO) {
        Page<EquipmentMaintenance> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<EquipmentMaintenance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getEquipmentId() != null, EquipmentMaintenance::getEquipmentId, queryDTO.getEquipmentId())
                .like(queryDTO.getMaintenanceType() != null && !queryDTO.getMaintenanceType().isBlank(),
                        EquipmentMaintenance::getMaintenanceType, queryDTO.getMaintenanceType())
                .ge(queryDTO.getStartDate() != null, EquipmentMaintenance::getMaintenanceDate, queryDTO.getStartDate())
                .le(queryDTO.getEndDate() != null, EquipmentMaintenance::getMaintenanceDate, queryDTO.getEndDate())
                .orderByDesc(EquipmentMaintenance::getMaintenanceDate)
                .orderByDesc(EquipmentMaintenance::getCreateTime);

        IPage<EquipmentMaintenance> maintenancePage = maintenanceMapper.selectPage(page, wrapper);
        return maintenancePage.convert(maintenance -> {
            EquipmentMaintenanceVO vo = BeanUtil.copyProperties(maintenance, EquipmentMaintenanceVO.class);
            // 填充设备信息
            if (maintenance.getEquipmentId() != null) {
                Equipment equipment = equipmentMapper.selectById(maintenance.getEquipmentId());
                if (equipment != null) {
                    vo.setEquipmentNo(equipment.getEquipmentNo());
                    vo.setEquipmentName(equipment.getEquipmentName());
                }
            }
            return vo;
        });
    }

    @Override
    public EquipmentMaintenanceVO getMaintenanceById(Long id) {
        EquipmentMaintenance maintenance = maintenanceMapper.selectById(id);
        if (maintenance == null) {
            throw new RuntimeException("维护记录不存在");
        }
        EquipmentMaintenanceVO vo = BeanUtil.copyProperties(maintenance, EquipmentMaintenanceVO.class);
        // 填充设备信息
        if (maintenance.getEquipmentId() != null) {
            Equipment equipment = equipmentMapper.selectById(maintenance.getEquipmentId());
            if (equipment != null) {
                vo.setEquipmentNo(equipment.getEquipmentNo());
                vo.setEquipmentName(equipment.getEquipmentName());
            }
        }
        return vo;
    }

    @Override
    @Transactional
    public void createMaintenance(EquipmentMaintenanceSaveDTO saveDTO) {
        Equipment equipment = equipmentMapper.selectById(saveDTO.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        
        EquipmentMaintenance maintenance = BeanUtil.copyProperties(saveDTO, EquipmentMaintenance.class);
        maintenanceMapper.insert(maintenance);
    }

    @Override
    @Transactional
    public void updateMaintenance(Long id, EquipmentMaintenanceSaveDTO saveDTO) {
        EquipmentMaintenance maintenance = maintenanceMapper.selectById(id);
        if (maintenance == null) {
            throw new RuntimeException("维护记录不存在");
        }
        
        BeanUtil.copyProperties(saveDTO, maintenance, "id", "createTime", "updateTime", "deleted");
        maintenanceMapper.updateById(maintenance);
    }

    @Override
    @Transactional
    public void deleteMaintenance(Long id) {
        EquipmentMaintenance maintenance = maintenanceMapper.selectById(id);
        if (maintenance == null) {
            throw new RuntimeException("维护记录不存在");
        }
        maintenanceMapper.deleteById(id);
    }
}
