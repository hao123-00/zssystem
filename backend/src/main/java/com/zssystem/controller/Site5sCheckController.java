package com.zssystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zssystem.common.PageResult;
import com.zssystem.common.Result;
import com.zssystem.dto.Site5sCheckQueryDTO;
import com.zssystem.dto.Site5sCheckSaveDTO;
import com.zssystem.service.Site5sCheckService;
import com.zssystem.vo.Site5sCheckVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/site5s/check")
@Validated
public class Site5sCheckController {

    @Autowired
    private Site5sCheckService checkService;

    @GetMapping("/list")
    public Result<PageResult<Site5sCheckVO>> getCheckList(@Validated Site5sCheckQueryDTO queryDTO) {
        IPage<Site5sCheckVO> page = checkService.getCheckList(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<Site5sCheckVO> getCheckById(@PathVariable Long id) {
        Site5sCheckVO vo = checkService.getCheckById(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Void> saveCheck(@Valid @RequestBody Site5sCheckSaveDTO saveDTO) {
        checkService.saveCheck(saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCheck(@PathVariable Long id) {
        checkService.deleteCheck(id);
        return Result.success();
    }
}
