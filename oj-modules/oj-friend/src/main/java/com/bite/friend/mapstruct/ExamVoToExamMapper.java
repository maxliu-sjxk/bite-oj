package com.bite.friend.mapstruct;

import com.bite.friend.domain.exam.Exam;
import com.bite.friend.domain.exam.vo.ExamVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 转换 ExamVO 与 Exam 的映射接口
 * componentModel = "spring"：生成 Spring 可管理的 Bean
 */
@Mapper(componentModel = "spring")
public interface ExamVoToExamMapper {

    /**
     * 单对象转换：ExamVO -> Exam
     * 说明：
     * 1. 同名同类型字段（examId、title、startTime、endTime）自动映射
     * 2. Exam 继承的 BaseEntity 字段（createBy、createTime 等）由数据库/框架自动填充，无需从 VO 映射，故忽略
     * 3. Exam 的 status 字段在 VO 中不存在，需忽略或通过其他方式赋值（此处忽略，业务中按需处理）
     */
    @Mapping(target = "status", ignore = true) // VO 中无 status，忽略
    @Mapping(target = "createBy", ignore = true) // BaseEntity 字段，框架填充
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    Exam voToEntity(ExamVO examVO);

    /**
     * 集合转换：List<ExamVO> -> List<Exam>
     * MapStruct 自动循环调用 voToEntity 方法，无需手动处理流
     */
    List<Exam> voListToEntityList(List<ExamVO> examVOList);
}
