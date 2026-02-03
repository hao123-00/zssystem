package com.zssystem.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zssystem.entity.HandoverRecord;
import com.zssystem.mapper.HandoverRecordMapper;
import com.zssystem.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交接班拍照照片清理任务：保留15天后自动删除
 */
@Component
public class HandoverPhotoCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(HandoverPhotoCleanupTask.class);

    @Autowired
    private HandoverRecordMapper recordMapper;

    @Value("${file.upload.handover-photos:/Users/czd/zssystem/uploads/handover-photos}")
    private String handoverPhotosPath;

    /** 每天凌晨2点执行 */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredPhotos() {
        LocalDateTime expireBefore = LocalDateTime.now().minusDays(15);
        LambdaQueryWrapper<HandoverRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(HandoverRecord::getPhotoPath)
                .lt(HandoverRecord::getCreateTime, expireBefore);
        List<HandoverRecord> records = recordMapper.selectList(wrapper);
        int deleted = 0;
        for (HandoverRecord r : records) {
            String path = r.getPhotoPath();
            if (path != null && !path.isBlank()) {
                if (FileUtil.deleteFile(path)) {
                    deleted++;
                }
                r.setPhotoPath(null);
                recordMapper.updateById(r);
            }
        }
        if (deleted > 0) {
            log.info("交接班照片清理：删除{}张超过15天的照片", deleted);
        }
    }
}
