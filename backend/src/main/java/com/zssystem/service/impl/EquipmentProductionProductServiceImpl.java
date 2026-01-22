package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zssystem.dto.EquipmentProductionProductSaveDTO;
import com.zssystem.entity.Equipment;
import com.zssystem.entity.EquipmentProductionProduct;
import com.zssystem.mapper.EquipmentMapper;
import com.zssystem.mapper.EquipmentProductionProductMapper;
import com.zssystem.service.EquipmentProductionProductService;
import com.zssystem.util.BeanUtil;
import com.zssystem.vo.EquipmentProductionProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentProductionProductServiceImpl implements EquipmentProductionProductService {

    @Autowired
    private EquipmentProductionProductMapper mapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Override
    public List<EquipmentProductionProductVO> getProductListByEquipmentId(Long equipmentId) {
        List<EquipmentProductionProduct> products = mapper.selectList(
            new LambdaQueryWrapper<EquipmentProductionProduct>()
                .eq(EquipmentProductionProduct::getEquipmentId, equipmentId)
                .orderByAsc(EquipmentProductionProduct::getSortOrder)
        );
        
        return products.stream().map(product -> {
            EquipmentProductionProductVO vo = BeanUtil.copyProperties(product, EquipmentProductionProductVO.class);
            // 计算预计完成天数
            if (vo.getDailyCapacity() != null && vo.getDailyCapacity() > 0) {
                vo.setEstimatedDays((int) Math.ceil((double) vo.getOrderQuantity() / vo.getDailyCapacity()));
            }
            // 填充设备名称
            if (product.getEquipmentId() != null) {
                Equipment equipment = equipmentMapper.selectById(product.getEquipmentId());
                if (equipment != null) {
                    vo.setEquipmentName(equipment.getEquipmentName());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public EquipmentProductionProductVO getById(Long id) {
        EquipmentProductionProduct product = mapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("设备生产产品配置不存在");
        }
        EquipmentProductionProductVO vo = BeanUtil.copyProperties(product, EquipmentProductionProductVO.class);
        // 计算预计完成天数
        if (vo.getDailyCapacity() != null && vo.getDailyCapacity() > 0) {
            vo.setEstimatedDays((int) Math.ceil((double) vo.getOrderQuantity() / vo.getDailyCapacity()));
        }
        // 填充设备名称
        if (product.getEquipmentId() != null) {
            Equipment equipment = equipmentMapper.selectById(product.getEquipmentId());
            if (equipment != null) {
                vo.setEquipmentName(equipment.getEquipmentName());
            }
        }
        return vo;
    }

    @Override
    @Transactional
    public void create(EquipmentProductionProductSaveDTO saveDTO) {
        // 校验设备是否存在
        Equipment equipment = equipmentMapper.selectById(saveDTO.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        
        EquipmentProductionProduct product = BeanUtil.copyProperties(saveDTO, EquipmentProductionProduct.class);
        product.setEquipmentNo(equipment.getEquipmentNo());
        if (product.getSortOrder() == null) {
            product.setSortOrder(0);
        }
        mapper.insert(product);
    }

    @Override
    @Transactional
    public void update(Long id, EquipmentProductionProductSaveDTO saveDTO) {
        EquipmentProductionProduct product = mapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("设备生产产品配置不存在");
        }
        
        BeanUtil.copyProperties(saveDTO, product, "id", "equipmentId", "equipmentNo", "createTime", "updateTime", "deleted");
        mapper.updateById(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EquipmentProductionProduct product = mapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("设备生产产品配置不存在");
        }
        mapper.deleteById(id);
    }
}
