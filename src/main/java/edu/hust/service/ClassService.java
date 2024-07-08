package edu.hust.service;

import edu.hust.pojo.Class;

import java.util.List;
import java.util.Map;

public interface ClassService {
    Map<String, Object> listClassByPage(Integer page, Integer limit);

    void createClass(Class aClass);

    void deleteClass(Integer id);

    void updateClass(Class aClass);

    Map<String, Object> listClassByPageWithCondition(Integer page, Integer limit, String name, String creator, Integer id);

    Map<String, Object> listClassUserByClassId(Integer classId);

    List<Class> getClassesByUserId(Integer userId);
}
