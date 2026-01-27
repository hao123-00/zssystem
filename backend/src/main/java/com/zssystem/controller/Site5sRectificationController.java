package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.Site5sRectificationQueryDTO;
import com.zssystem.dto.Site5sRectificationSaveDTO;
import com.zssystem.service.Site5sRectificationService;
import com.zssystem.vo.Site5sRectificationVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/site5s/rectification")
@Validated
public class Site5sRectificationController {

    @Autowired
    private Site5sRectificationService rectificationService;

    @GetMapping("/list")
    public Result<PageResult<Site5sRectificationVO>> getRectificationList(@Validated Site5sRectificationQueryDTO queryDTO) {
        IPage<Site5sRectificationVO> page = rectificationService.getRectificationList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<Site5sRectificationVO> getRectificationById(@PathVariable Long id) {
        Site5sRectificationVO vo = rectificationService.getRectificationById(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Void> saveRectification(@Valid @RequestBody Site5sRectificationSaveDTO saveDTO) {
        rectificationService.saveRectification(saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRectification(@PathVariable Long id) {
        rectificationService.deleteRectification(id);
        return Result.success();
    }

    @PostMapping("/create-from-check/{checkId}")
    public Result<Void> createFromCheck(@PathVariable Long checkId) {
        rectificationService.createFromCheck(checkId);
        return Result.success();
    }
}
