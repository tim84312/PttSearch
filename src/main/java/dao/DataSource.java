package dao;

import com.alibaba.druid.pool.DruidDataSource;
import org.nutz.dao.impl.NutDao;
import org.nutz.dao.util.Daos;

public class DataSource {
    private static NutDao dao;

    static {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl("jdbc:mysql://127.0.0.1/pttsearch?serverTimezone=UTC&characterEncoding=utf-8");
        druidDataSource.setUsername("tim");
        druidDataSource.setPassword("iamtim");
        druidDataSource.setDefaultAutoCommit(false);
        druidDataSource.setMaxWait(15000);
        druidDataSource.setInitialSize(10);
        druidDataSource.setMaxActive(50);
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dao = new NutDao(druidDataSource);
        Daos.createTablesInPackage(dao, "entity", false);
    }

    public static NutDao getDao(){return dao;}
}
