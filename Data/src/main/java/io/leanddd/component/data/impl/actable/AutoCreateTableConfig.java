package io.leanddd.component.data.impl.actable;

import com.gitee.sunchenbin.mybatis.actable.dao.common.BaseCRUDMapper;
import com.gitee.sunchenbin.mybatis.actable.manager.handler.StartUpHandler;
import com.gitee.sunchenbin.mybatis.actable.manager.handler.StartUpHandlerImpl;
import com.gitee.sunchenbin.mybatis.actable.manager.system.SysMysqlCreateTableManager;
import com.gitee.sunchenbin.mybatis.actable.manager.util.ConfigurationUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.component-features.auto-create-table", havingValue = "true", matchIfMissing = false) // 允许使用
public class AutoCreateTableConfig {

    @Bean
    SysMysqlCreateTableManager MySysMysqlCreateTableManager() {
        return new SysMysqlCreateTableManagerImpl();
    }
    
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    
    @Bean
    Object initAutoCreateTableMapper() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            session.getConfiguration().addMapper(BaseCRUDMapper.class);
        }
        return new Object();
    }

    @Bean
    ConfigurationUtil configurationUtil() {
        return new ConfigurationUtil();
    }

    @Bean
    StartUpHandler startUpHandlerImpl() {
        return new StartUpHandlerImpl();
    }
}
