package com.bite.friend.mapstruct;

import com.bite.friend.domain.exam.Exam;
import com.bite.friend.domain.exam.vo.ExamVO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-02T10:55:22+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.9 (Oracle Corporation)"
)
@Component
public class ExamVoToExamMapperImpl implements ExamVoToExamMapper {

    @Override
    public Exam voToEntity(ExamVO examVO) {
        if ( examVO == null ) {
            return null;
        }

        Exam exam = new Exam();

        exam.setExamId( examVO.getExamId() );
        exam.setTitle( examVO.getTitle() );
        exam.setStartTime( examVO.getStartTime() );
        exam.setEndTime( examVO.getEndTime() );

        return exam;
    }

    @Override
    public List<Exam> voListToEntityList(List<ExamVO> examVOList) {
        if ( examVOList == null ) {
            return null;
        }

        List<Exam> list = new ArrayList<Exam>( examVOList.size() );
        for ( ExamVO examVO : examVOList ) {
            list.add( voToEntity( examVO ) );
        }

        return list;
    }
}
