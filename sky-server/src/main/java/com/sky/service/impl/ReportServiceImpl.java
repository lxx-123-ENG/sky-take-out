package com.sky.service.impl;



import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 统计指定时间的营业额
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        // 用于存储开始时间到结束时间的日期集合
        List<LocalDate>dateList=new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            // 日期加1
            begin=begin.plusDays(1);
            dateList.add(begin);
        }


        // 用于存储日期对应的营业额
        List<Double>turnoverList = new ArrayList<>();
        for(LocalDate date:dateList){
            //查询date日期对应的营业额，指的是已完成订单的合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.CANCELLED);
            Double turnover =orderMapper.sumByMap(map);
            turnover=turnover==null?0.0:turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end){
        // 用于存储开始时间到结束时间的日期集合
        List<LocalDate>dateList=new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            // 日期加1
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        // 用于存储日期对应的新增用户数和总用户数
        List<Integer>newUserList = new ArrayList<>();
        List<Integer>totalUserList = new ArrayList<>();
        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("end",endTime);
            // 统计日期区间内总用户数
            Integer totalUsers  = userMapper.countByMap(map);

            // 统计日期区间内新增用户数
            map.put("begin",beginTime);
            Integer newUsers = userMapper.countByMap(map);

            totalUserList.add(totalUsers);
            newUserList.add(newUsers);
        }

        // 封装结果并返回
        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    /**
     * 根据时间区间统计订单数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end){
        List<LocalDate>dateList=new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            // 日期加1
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        // 用于存储日期对应的订单总数、有效订单数
        for(LocalDate date:dateList){

            // 查询日期区间内订单总数
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = getOrderCount(beginTime, endTime, null);


            // 查询日期区间内有效订单数

            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //计算订单总数
        Integer totalOrderCount  = orderCountList.stream().reduce(Integer::sum).get();

        //计算有效订单总数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        //计算订单完成率
        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status){
        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);
        return orderMapper.countByMap(map);
    }
}
