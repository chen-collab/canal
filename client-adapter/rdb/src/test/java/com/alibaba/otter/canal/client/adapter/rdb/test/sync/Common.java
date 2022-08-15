package com.alibaba.otter.canal.client.adapter.rdb.test.sync;

import com.alibaba.otter.canal.client.adapter.rdb.RdbAdapter;
import com.alibaba.otter.canal.client.adapter.rdb.test.TestConstant;
import com.alibaba.otter.canal.client.adapter.support.DatasourceConfig;
import com.alibaba.otter.canal.client.adapter.support.OuterAdapterConfig;

import java.util.HashMap;
import java.util.Map;

public class Common {

    public static RdbAdapter init() {
        DatasourceConfig.DATA_SOURCES.put("defaultDS", TestConstant.dataSource);

        OuterAdapterConfig outerAdapterConfig = new OuterAdapterConfig();
        outerAdapterConfig.setName("rdb");
        outerAdapterConfig.setKey("sqlserver");
        Map<String, String> properties = new HashMap<>();
        properties.put("jdbc.driveClassName", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        properties.put("jdbc.url", "jdbc:sqlserver://192.168.3.128:1433;DatabaseName=db_test");
        properties.put("jdbc.username", "sa");
        properties.put("jdbc.password", "yskj123456");
        outerAdapterConfig.setProperties(properties);

        RdbAdapter adapter = new RdbAdapter();
        adapter.init(outerAdapterConfig, null);
        return adapter;
    }
}
