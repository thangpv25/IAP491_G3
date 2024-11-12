package org.example.Loader;

public class Contraints {


    /**
     * Java内部包名过滤正则
     */
    public static final String JAVA_INTERNAL_PACKAGES = "^(java|javax|jakarta|(com\\.)?sun)\\..*";

    public static final String AGENT_NAME = "Inspectra";

    public static final String ENCODING = "UTF-8";

    public static final String AGENT_FILE_NAME = AGENT_NAME + "-AgentCore.jar";

    /**
     * 定义Agent loader文件名称
     */
    public static final String AGENT_LOADER_FILE_NAME = AGENT_NAME + "-loader.jar";



}