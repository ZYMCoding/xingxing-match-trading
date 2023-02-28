package com.star.engine.bean;

import com.star.engine.mapper.DBQuery;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 单例模式
 */
@Log4j2
public class DBUtil {

    private static DBUtil ourInstance = new DBUtil();

    private DBUtil() {}

    public static DBUtil getInstance() {
        return ourInstance;
    }

    private static SqlSessionFactory sqlSessionFactory;

    static {
        log.info("构建SqlSessionFactory...");
        String resource = "mybatis-config.xml";
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        log.info(sqlSessionFactory);
    }

    public List<Map<String, Object>> queryAllBalance() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DBQuery mapper = session.getMapper(DBQuery.class);
            List<Map<String, Object>> maps = mapper.queryAllBalance();
            return maps;
        } catch (Exception e) {
            log.error("Exception in queryAllBalance", e);
        }
        return null;
    }

    public HashSet<Integer> queryAllStockCode() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DBQuery mapper = session.getMapper(DBQuery.class);
            HashSet<Integer> allStockCode = mapper.queryAllStockCode();
            return allStockCode;
        } catch (Exception e) {
            log.error("Exception in queryAllStockCode", e);
        }
        return null;
    }

    public List<Integer> queryAllMemberIds() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            DBQuery mapper = session.getMapper(DBQuery.class);
            List<Integer> memberIds = mapper.queryAllMemberIds();
            return memberIds;
        } catch (Exception e) {
            log.error("Exception in queryAllMemberIds", e);
        }
        return null;
    }
}
