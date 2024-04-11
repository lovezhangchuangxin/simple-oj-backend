package edu.hust.service;

import edu.hust.pojo.Bulletin;

import java.util.Map;

public interface BulletinService {
    Map<String, Object> listBulletinByPage(Integer page, Integer limit);

    Integer addBulletin(Bulletin bulletin);

    void deleteBulletin(Integer id);

    Map<String, Object> getBulletinById(Integer id);

    void updateBulletin(Bulletin bulletin);
}
