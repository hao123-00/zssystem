package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zssystem.dto.EquipmentProductSaveDTO;
import com.zssystem.entity.Equipment;
import com.zssystem.entity.EquipmentProduct;
import com.zssystem.mapper.EquipmentMapper;
import com.zssystem.mapper.EquipmentProductMapper;
import com.zssystem.service.EquipmentProductService;
import com.zssystem.util.BeanUtil;
import com.zssystem.vo.EquipmentProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentProductServiceImpl implements EquipmentProductService {

    @Autowired
    private EquipmentProductMapper productMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Override
    public List<EquipmentProductVO> getProductListByEquipmentId(Long equipmentId) {
        List<EquipmentProduct> products = productMapper.selectList(
            new LambdaQueryWrapper<EquipmentProduct>()
                .eq(EquipmentProduct::getEquipmentId, equipmentId)
        );
        
        return products.stream().map(product -> {
            EquipmentProductVO vo = BeanUtil.copyProperties(product, EquipmentProductVO.class);
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
    public List<EquipmentProductVO> getEquipmentListByProductCode(String productCode) {
        List<EquipmentProduct> products = productMapper.selectList(
            new LambdaQueryWrapper<EquipmentProduct>()
                .eq(EquipmentProduct::getProductCode, productCode)
        );
        
        return products.stream().map(product -> {
            EquipmentProductVO vo = BeanUtil.copyProperties(product, EquipmentProductVO.class);
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
    @Transactional
    public void bindProduct(EquipmentProductSaveDTO saveDTO) {
        Equipment equipment = equipmentMapper.selectById(saveDTO.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        
        // 检查是否已存在绑定
        EquipmentProduct exist = productMapper.selectOne(
            new LambdaQueryWrapper<EquipmentProduct>()
                .eq(EquipmentProduct::getEquipmentId, saveDTO.getEquipmentId())
                .eq(EquipmentProduct::getProductCode, saveDTO.getProductCode())
        );
        if (exist != null) {
            throw new RuntimeException("该设备已配置该产品");
        }
        
        EquipmentProduct ep = new EquipmentProduct();
        ep.setEquipmentId(saveDTO.getEquipmentId());
        ep.setEquipmentNo(equipment.getEquipmentNo());
        ep.setProductCode(saveDTO.getProductCode());
        ep.setProductName(saveDTO.getProductName());
        productMapper.insert(ep);
    }

    @Override
    @Transactional
    public void unbindProduct(Long id) {
        EquipmentProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("设备产品配置不存在");
        }
        productMapper.deleteById(id);
    }
}
