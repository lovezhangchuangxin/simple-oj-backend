package edu.hust.service;

public interface ClassUserService {
    void addClassUser(Integer classId, Integer userId);

    void deleteClassUser(Integer classId, Integer userId);
}
