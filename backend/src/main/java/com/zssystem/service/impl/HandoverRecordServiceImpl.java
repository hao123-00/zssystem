package com.zssystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zssystem.dto.HandoverRecordQueryDTO;
import com.zssystem.dto.HandoverRecordSaveDTO;
import com.zssystem.entity.Equipment;
import com.zssystem.entity.HandoverRecord;
import com.zssystem.mapper.EquipmentMapper;
import com.zssystem.mapper.HandoverRecordMapper;
import com.zssystem.service.HandoverRecordService;
import com.zssystem.util.BeanUtil;
import com.zssystem.util.FileUtil;
import com.zssystem.util.HandoverRecordExcelGenerator;
import com.zssystem.util.HandoverRecordExcelToHtmlConverter;
import com.zssystem.vo.HandoverRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class HandoverRecordServiceImpl implements HandoverRecordService {

    @Autowired
    private HandoverRecordMapper recordMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Value("${file.upload.handover-photos:/Users/czd/zssystem/uploads/handover-photos}")
    private String handoverPhotosPath;

    @Override
    public IPage<HandoverRecordVO> getRecordList(HandoverRecordQueryDTO queryDTO) {
        Page<HandoverRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<HandoverRecord> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(queryDTO.getEquipmentId() != null, HandoverRecord::getEquipmentId, queryDTO.getEquipmentId())
                .like(queryDTO.getEquipmentNo() != null && !queryDTO.getEquipmentNo().isBlank(),
                        HandoverRecord::getEquipmentNo, queryDTO.getEquipmentNo())
                .like(queryDTO.getProductName() != null && !queryDTO.getProductName().isBlank(),
                        HandoverRecord::getProductName, queryDTO.getProductName());

        if (queryDTO.getRecordMonth() != null && !queryDTO.getRecordMonth().isBlank()) {
            YearMonth ym = YearMonth.parse(queryDTO.getRecordMonth());
            wrapper.ge(HandoverRecord::getRecordDate, ym.atDay(1).atStartOfDay())
                    .le(HandoverRecord::getRecordDate, ym.atEndOfMonth().atTime(23, 59, 59));
        }

        wrapper.orderByAsc(HandoverRecord::getRecordDate)
                .orderByAsc(HandoverRecord::getProductName)
                .orderByDesc(HandoverRecord::getCreateTime);

        IPage<HandoverRecord> recordPage = recordMapper.selectPage(page, wrapper);
        return recordPage.convert(r -> {
            HandoverRecordVO vo = BeanUtil.copyProperties(r, HandoverRecordVO.class);
            vo.setHasPhoto(r.getPhotoPath() != null && !r.getPhotoPath().isBlank());
            return vo;
        });
    }

    @Override
    public HandoverRecordVO getRecordById(Long id) {
        HandoverRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("交接班记录不存在");
        }
        HandoverRecordVO vo = BeanUtil.copyProperties(record, HandoverRecordVO.class);
        vo.setHasPhoto(record.getPhotoPath() != null && !record.getPhotoPath().isBlank());
        return vo;
    }

    @Override
    @Transactional
    public void saveRecord(HandoverRecordSaveDTO saveDTO) {
        Equipment equipment = equipmentMapper.selectById(saveDTO.getEquipmentId());
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }

        HandoverRecord record;
        if (saveDTO.getId() != null) {
            record = recordMapper.selectById(saveDTO.getId());
            if (record == null) {
                throw new RuntimeException("交接班记录不存在");
            }
        } else {
            record = new HandoverRecord();
            if (saveDTO.getPhotoPath() == null || saveDTO.getPhotoPath().isBlank()) {
                throw new RuntimeException("新增交接班记录必须拍照或上传照片");
            }
        }

        BeanUtil.copyProperties(saveDTO, record, "id", "recordDate", "createTime", "updateTime", "deleted");
        record.setEquipmentNo(equipment.getEquipmentNo());
        record.setRecordDate(LocalDateTime.now());

        if (record.getId() == null) {
            recordMapper.insert(record);
        } else {
            recordMapper.updateById(record);
        }
    }

    @Override
    @Transactional
    public void deleteRecord(Long id) {
        recordMapper.deleteById(id);
    }

    @Override
    public List<String> getProductNamesByEquipmentId(Long equipmentId) {
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            return List.of();
        }
        List<String> list = new ArrayList<>();
        addIfNotBlank(list, equipment.getBasicMold());
        addIfNotBlank(list, equipment.getSpareMold1());
        addIfNotBlank(list, equipment.getSpareMold2());
        addIfNotBlank(list, equipment.getSpareMold3());
        addIfNotBlank(list, equipment.getBasicMold4());
        return list;
    }

    private static void addIfNotBlank(List<String> list, String s) {
        if (s != null && !s.isBlank() && !list.contains(s.trim())) {
            list.add(s.trim());
        }
    }

    @Override
    public Object[] exportExcel(Long equipmentId, String recordMonth) throws Exception {
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }

        YearMonth ym = YearMonth.parse(recordMonth);
        LambdaQueryWrapper<HandoverRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HandoverRecord::getEquipmentId, equipmentId)
                .ge(HandoverRecord::getRecordDate, ym.atDay(1).atStartOfDay())
                .le(HandoverRecord::getRecordDate, ym.atEndOfMonth().atTime(23, 59, 59))
                .orderByAsc(HandoverRecord::getRecordDate)
                .orderByAsc(HandoverRecord::getProductName);

        List<HandoverRecord> allRecords = recordMapper.selectList(wrapper);
        List<byte[]> excelFiles = HandoverRecordExcelGenerator.generate(equipment, recordMonth, allRecords);

        if (excelFiles.size() == 1) {
            return new Object[]{excelFiles.get(0), "交接班记录_" + recordMonth + ".xlsx"};
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (int i = 0; i < excelFiles.size(); i++) {
                String name = "交接班记录_" + recordMonth + "_" + (i + 1) + ".xlsx";
                zos.putNextEntry(new ZipEntry(name));
                zos.write(excelFiles.get(i));
                zos.closeEntry();
            }
            zos.finish();
            return new Object[]{baos.toByteArray(), "交接班记录_" + recordMonth + ".zip"};
        }
    }

    private static final int DATA_ROWS_PER_SHEET = 28;

    @Override
    public int getExportFileCount(Long equipmentId, String recordMonth) {
        YearMonth ym = YearMonth.parse(recordMonth);
        LambdaQueryWrapper<HandoverRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HandoverRecord::getEquipmentId, equipmentId)
                .ge(HandoverRecord::getRecordDate, ym.atDay(1).atStartOfDay())
                .le(HandoverRecord::getRecordDate, ym.atEndOfMonth().atTime(23, 59, 59));
        long count = recordMapper.selectCount(wrapper);
        if (count == 0) return 1;
        return (int) ((count + DATA_ROWS_PER_SHEET - 1) / DATA_ROWS_PER_SHEET);
    }

    @Override
    public Object[] exportExcelPage(Long equipmentId, String recordMonth, int page) throws Exception {
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        YearMonth ym = YearMonth.parse(recordMonth);
        LambdaQueryWrapper<HandoverRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HandoverRecord::getEquipmentId, equipmentId)
                .ge(HandoverRecord::getRecordDate, ym.atDay(1).atStartOfDay())
                .le(HandoverRecord::getRecordDate, ym.atEndOfMonth().atTime(23, 59, 59))
                .orderByAsc(HandoverRecord::getRecordDate)
                .orderByAsc(HandoverRecord::getProductName);
        List<HandoverRecord> allRecords = recordMapper.selectList(wrapper);
        int totalPages = allRecords.isEmpty() ? 1 : (int) ((allRecords.size() + DATA_ROWS_PER_SHEET - 1) / DATA_ROWS_PER_SHEET);
        if (page < 1 || page > totalPages) {
            throw new RuntimeException("页码超出范围");
        }
        int start = (page - 1) * DATA_ROWS_PER_SHEET;
        int end = Math.min(start + DATA_ROWS_PER_SHEET, allRecords.size());
        List<HandoverRecord> batch = allRecords.subList(start, end);
        String pageSuffix = totalPages > 1 ? "（第" + page + "页）" : "";
        byte[] bytes = HandoverRecordExcelGenerator.createWorkbookForPage(
                equipment.getEquipmentNo(), recordMonth, batch, pageSuffix);
        String fileName = totalPages > 1
                ? "交接班记录_" + recordMonth + "_" + page + ".xlsx"
                : "交接班记录_" + recordMonth + ".xlsx";
        return new Object[]{bytes, fileName};
    }

    @Override
    public String getPreviewHtml(Long equipmentId, String recordMonth) throws Exception {
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        YearMonth ym = YearMonth.parse(recordMonth);
        LambdaQueryWrapper<HandoverRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HandoverRecord::getEquipmentId, equipmentId)
                .ge(HandoverRecord::getRecordDate, ym.atDay(1).atStartOfDay())
                .le(HandoverRecord::getRecordDate, ym.atEndOfMonth().atTime(23, 59, 59))
                .orderByAsc(HandoverRecord::getRecordDate)
                .orderByAsc(HandoverRecord::getProductName);
        List<HandoverRecord> allRecords = recordMapper.selectList(wrapper);
        List<byte[]> excelFiles = HandoverRecordExcelGenerator.generate(equipment, recordMonth, allRecords);
        if (excelFiles.isEmpty()) {
            excelFiles = HandoverRecordExcelGenerator.generate(equipment, recordMonth, List.of());
        }
        byte[] firstExcel = excelFiles.get(0);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(firstExcel)) {
            return HandoverRecordExcelToHtmlConverter.convertToHtml(bais);
        } catch (IOException e) {
            throw new RuntimeException("预览生成失败", e);
        }
    }

    @Override
    public String getPreviewHtmlForSvg(Long equipmentId, String recordMonth) throws Exception {
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) {
            throw new RuntimeException("设备不存在");
        }
        YearMonth ym = YearMonth.parse(recordMonth);
        LambdaQueryWrapper<HandoverRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HandoverRecord::getEquipmentId, equipmentId)
                .ge(HandoverRecord::getRecordDate, ym.atDay(1).atStartOfDay())
                .le(HandoverRecord::getRecordDate, ym.atEndOfMonth().atTime(23, 59, 59))
                .orderByAsc(HandoverRecord::getRecordDate)
                .orderByAsc(HandoverRecord::getProductName);
        List<HandoverRecord> allRecords = recordMapper.selectList(wrapper);
        List<byte[]> excelFiles = HandoverRecordExcelGenerator.generate(equipment, recordMonth, allRecords);
        if (excelFiles.isEmpty()) {
            excelFiles = HandoverRecordExcelGenerator.generate(equipment, recordMonth, List.of());
        }
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"hr-embed-root\">").append(HandoverRecordExcelToHtmlConverter.getStyleBlock());
        for (byte[] excelBytes : excelFiles) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(excelBytes)) {
                html.append(HandoverRecordExcelToHtmlConverter.convertToHtmlFragment(bais));
            }
        }
        html.append("</div>");
        return html.toString();
    }

    @Override
    public String uploadHandoverPhoto(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择或拍摄照片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只能上传图片格式");
        }
        String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String saveDir = handoverPhotosPath + "/" + yearMonth;
        FileUtil.createDirectoryIfNotExists(saveDir);
        String ext = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                : ".jpg";
        String fileName = "HP_" + System.currentTimeMillis() + ext;
        String fullPath = saveDir + "/" + fileName;
        file.transferTo(new File(fullPath));
        return fullPath;
    }

    @Override
    public byte[] getRecordPhoto(Long id) throws Exception {
        HandoverRecord record = recordMapper.selectById(id);
        if (record == null) throw new RuntimeException("交接班记录不存在");
        String path = record.getPhotoPath();
        if (path == null || path.isBlank()) return null;
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) return null;
        return Files.readAllBytes(filePath);
    }
}
