package com.alibaba.otter.canal.client.adapter.rdb.monitor;

import com.alibaba.otter.canal.client.adapter.rdb.RdbAdapter;
import com.alibaba.otter.canal.client.adapter.rdb.config.MappingConfig;
import com.alibaba.otter.canal.client.adapter.support.MappingConfigsLoader;
import com.alibaba.otter.canal.client.adapter.support.Util;
import com.alibaba.otter.canal.client.adapter.support.YamlUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdbConfigMonitor {

    private static final Logger   logger      = LoggerFactory.getLogger(RdbConfigMonitor.class);

    private static final String   adapterName = "rdb";

    private String                key;

    private RdbAdapter            rdbAdapter;

    private Properties            envProperties;

    private FileAlterationMonitor fileMonitor;

    public void init(String key, RdbAdapter rdbAdapter, Properties envProperties) {
        this.key = key;
        this.rdbAdapter = rdbAdapter;
        this.envProperties = envProperties;
        File confDir = Util.getConfDirPath(adapterName);
        try {
            //监听rdb 目录中的目录以及以yml为后缀的文件
            //https://blog.csdn.net/skyupward/article/details/104837466
            FileAlterationObserver observer = new FileAlterationObserver(confDir,
                FileFilterUtils.or(FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), HiddenFileFilter.VISIBLE),
                        FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.suffixFileFilter("yml"))));
            FileListener listener = new FileListener();
            observer.addListener(listener);
            fileMonitor = new FileAlterationMonitor(3000, observer);
            fileMonitor.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void destroy() {
        try {
            fileMonitor.stop();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private class FileListener extends FileAlterationListenerAdaptor {

        @Override
        public void onFileCreate(File file) {
            super.onFileCreate(file);
            try {
                if(file.isDirectory()){
                    return;
                }
                // 加载新增的配置文件
                String dir=adapterName + File.separator;
                logger.info("加载新增的配置文件: [{}] [{}] [{}] to canal adapter", adapterName , File.separator , file.getParent());
                String configContent =null;
                if(file.getParent().endsWith(adapterName)){
                    configContent =  MappingConfigsLoader.loadConfig(adapterName + File.separator + file.getName());
                }else {
                    String fileName=file.getAbsolutePath().substring(file.getParent().lastIndexOf(File.separator)+1);
                    configContent = MappingConfigsLoader.loadConfig(adapterName + File.separator + fileName);
                }
                MappingConfig config = YamlUtils
                    .ymlToObj(null, configContent, MappingConfig.class, null, envProperties);
                if (config == null) {
                    return;
                }
                config.validate();
                boolean result = rdbAdapter.addConfig(file.getName(), config);
                if (result) {
                    logger.info("Add a new rdb mapping config: {} to canal adapter", file.getName());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public void onFileChange(File file) {
            super.onFileChange(file);

            try {
                if(file.isDirectory()){
                    return;
                }
                if (rdbAdapter.getRdbMapping().containsKey(file.getName())) {
                    // 加载配置文件
                    logger.info("加载配置文件: [{}] [{}] [{}] to canal adapter", adapterName , File.separator , file.getPath());
                    String configContent =null;
                    if(file.getParent().endsWith(adapterName)){
                        configContent = MappingConfigsLoader
                                .loadConfig(adapterName + File.separator + file.getName());
                    }else {
                        String fileName=file.getAbsolutePath().substring(file.getParent().lastIndexOf(File.separator)+1);
                        configContent = MappingConfigsLoader.loadConfig(adapterName + File.separator + fileName);
                    }

                    if (configContent == null) {
                        onFileDelete(file);
                        return;
                    }
                    MappingConfig config = YamlUtils
                        .ymlToObj(null, configContent, MappingConfig.class, null, envProperties);
                    if (config == null) {
                        return;
                    }
                    config.validate();
                    rdbAdapter.updateConfig(file.getName(), config);
                    logger.info("Change a rdb mapping config: {} of canal adapter", file.getName());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public void onFileDelete(File file) {
            super.onFileDelete(file);

            try {
                //删除目录则目录下的配置文件一并删除
                if(file.isDirectory()){
                    List<File> fileList = Arrays.stream(file.listFiles()).collect(Collectors.toList());
                    for (File file1 : fileList) {
                        if (rdbAdapter.getRdbMapping().containsKey(file1.getName())) {
                            rdbAdapter.deleteConfig(file1.getName());
                            logger.info("Delete a rdb mapping config: {} of canal adapter", file1.getName());
                        }
                    }
                }else {
                    if (rdbAdapter.getRdbMapping().containsKey(file.getName())) {
                        rdbAdapter.deleteConfig(file.getName());

                        logger.info("Delete a rdb mapping config: {} of canal adapter", file.getName());
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
