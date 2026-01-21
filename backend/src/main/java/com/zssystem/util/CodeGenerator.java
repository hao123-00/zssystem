package com.zssystem.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CodeGenerator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成订单编号：ORDER + 日期（YYYYMMDD）+ 序号（3位）
     */
    public static String generateOrderNo(int sequence) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        return String.format("ORDER%s%03d", date, sequence);
    }

    /**
     * 生成计划编号：PLAN + 日期 + 序号
     */
    public static String generatePlanNo(int sequence) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        return String.format("PLAN%s%03d", date, sequence);
    }

    /**
     * 生成记录编号：RECORD + 日期 + 序号
     */
    public static String generateRecordNo(int sequence) {
        String date = LocalDate.now().format(DATE_FORMATTER);
        return String.format("RECORD%s%03d", date, sequence);
    }
}
