package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量保存套餐和菜品的关联关系
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id删除套餐和菜品的关联关系
     * @param setmealId
     */
    @Delete("Delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

    /**
     * 根据套餐id获取菜关联菜品的数据
     * @param setmealId
     * @return
     */
    @Select("Select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getDishBySetmealId(Long setmealId);

}
