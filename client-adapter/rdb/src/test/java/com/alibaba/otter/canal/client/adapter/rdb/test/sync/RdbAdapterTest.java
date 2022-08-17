package com.alibaba.otter.canal.client.adapter.rdb.test.sync;

import com.alibaba.otter.canal.client.adapter.rdb.RdbAdapter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * @ClassName RdbAdapterTest
 * @Description TODO
 * @Author 10560
 * @email chen18668070425@163.com
 * @Date 2022/8/9 13:39
 * @Version 3.0.1
 **/
@Ignore
public class RdbAdapterTest  {

    private RdbAdapter rdbAdapter;

    @Before
    public void init() {
        rdbAdapter = Common.init();
    }
    @Test
    public void testEtl() {
        List<String> paramArray = null;
        rdbAdapter.etl("mytest_user.yml",paramArray);
    }
}
