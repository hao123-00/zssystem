package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zssystem.entity.Equipment;
import com.zssystem.entity.EquipmentCheck;
import com.zssystem.entity.ProcessFile;
import com.zssystem.mapper.EquipmentMapper;
import com.zssystem.mapper.ProcessFileMapper;
import com.zssystem.service.EquipmentCheckService;
import com.zssystem.service.EquipmentQrService;
import com.zssystem.service.ProcessFileService;
import com.zssystem.vo.EquipmentCheckVO;
import com.zssystem.vo.EquipmentQrViewVO;
import com.zssystem.util.HtmlToPdfUtil;
import com.zssystem.util.QrCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentQrServiceImpl implements EquipmentQrService {

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private com.zssystem.mapper.EquipmentCheckMapper equipmentCheckMapper;

    @Autowired
    private ProcessFileMapper processFileMapper;

    @Autowired
    private EquipmentCheckService equipmentCheckService;

    @Autowired
    private ProcessFileService processFileService;

    @Value("${qr.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Override
    public EquipmentQrViewVO getViewData(Long equipmentId) {
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }

        EquipmentQrViewVO vo = new EquipmentQrViewVO();

        EquipmentQrViewVO.EquipmentBriefVO brief = new EquipmentQrViewVO.EquipmentBriefVO();
        brief.setId(equipment.getId());
        brief.setEquipmentNo(equipment.getEquipmentNo());
        brief.setEquipmentName(equipment.getEquipmentName());
        brief.setMachineNo(equipment.getMachineNo());
        vo.setEquipment(brief);

        String checkMonth = YearMonth.now().toString();
        vo.setCheckMonth(checkMonth);

        List<EquipmentCheck> checks = equipmentCheckMapper.selectList(
            new LambdaQueryWrapper<EquipmentCheck>()
                .eq(EquipmentCheck::getEquipmentId, equipmentId)
                .eq(EquipmentCheck::getCheckMonth, checkMonth)
                .orderByAsc(EquipmentCheck::getCheckDate)
        );
        List<EquipmentCheckVO> checkVOs = checks.stream()
            .map(c -> {
                EquipmentCheckVO cv = new EquipmentCheckVO();
                cv.setId(c.getId());
                cv.setCheckDate(c.getCheckDate());
                cv.setCheckerName(c.getCheckerName());
                cv.setCircuitItem1(c.getCircuitItem1());
                cv.setCircuitItem2(c.getCircuitItem2());
                cv.setCircuitItem3(c.getCircuitItem3());
                cv.setFrameItem1(c.getFrameItem1());
                cv.setFrameItem2(c.getFrameItem2());
                cv.setFrameItem3(c.getFrameItem3());
                cv.setOilItem1(c.getOilItem1());
                cv.setOilItem2(c.getOilItem2());
                cv.setOilItem3(c.getOilItem3());
                cv.setOilItem4(c.getOilItem4());
                cv.setOilItem5(c.getOilItem5());
                cv.setPeripheralItem1(c.getPeripheralItem1());
                cv.setPeripheralItem2(c.getPeripheralItem2());
                cv.setPeripheralItem3(c.getPeripheralItem3());
                cv.setPeripheralItem4(c.getPeripheralItem4());
                cv.setPeripheralItem5(c.getPeripheralItem5());
                cv.setRemark(c.getRemark());
                return cv;
            })
            .collect(Collectors.toList());
        vo.setCheckRecords(checkVOs);

        ProcessFile enabledFile = processFileMapper.selectOne(
            new LambdaQueryWrapper<ProcessFile>()
                .eq(ProcessFile::getMachineNo, equipment.getMachineNo())
                .eq(ProcessFile::getEnabled, 1)
                .eq(ProcessFile::getStatus, 5)
                .last("LIMIT 1")
        );
        if (enabledFile != null) {
            EquipmentQrViewVO.ProcessFileBriefVO pfBrief = new EquipmentQrViewVO.ProcessFileBriefVO();
            pfBrief.setId(enabledFile.getId());
            pfBrief.setFileNo(enabledFile.getFileNo());
            pfBrief.setFileName(enabledFile.getFileName());
            pfBrief.setVersionText("V" + (enabledFile.getVersion() != null ? enabledFile.getVersion() : 1) + ".0");
            vo.setEnabledProcessFile(pfBrief);
        } else {
            vo.setEnabledProcessFile(null);
        }

        return vo;
    }

    @Override
    public byte[] generateQrCodePng(Long equipmentId) throws Exception {
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        String url = frontendBaseUrl.replaceAll("/$", "") + "/qr/equipment/" + equipmentId;
        return QrCodeUtil.generatePng(url);
    }

    @Override
    public byte[] getCheckPdf(Long equipmentId) throws Exception {
        EquipmentQrViewVO data = getViewData(equipmentId);
        String html = equipmentCheckService.getPreviewHtmlForPdf(equipmentId, data.getCheckMonth());
        return HtmlToPdfUtil.htmlToPdf(html);
    }

    @Override
    public byte[] getProcessFilePdf(Long equipmentId) throws Exception {
        EquipmentQrViewVO data = getViewData(equipmentId);
        if (data.getEnabledProcessFile() == null) {
            throw new RuntimeException("该机台暂无启用的工艺卡");
        }
        String html = processFileService.getPreviewHtmlForPdf(data.getEnabledProcessFile().getId());
        return HtmlToPdfUtil.htmlToPdf(html);
    }
}
