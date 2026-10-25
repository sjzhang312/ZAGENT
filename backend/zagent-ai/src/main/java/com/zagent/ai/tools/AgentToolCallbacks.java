package com.zagent.ai.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/**
 * 智能体工具回调工厂：read / write / edit / bash 四个工具的 Spring AI ToolCallback 定义。
 * 按智能体配置的 enabled_tools 过滤后交给适配层。
 *
 * @author zagent
 */
@Component
public class AgentToolCallbacks
{
    public static final String READ = "read";

    public static final String WRITE = "write";

    public static final String EDIT = "edit";

    public static final String BASH = "bash";

    private final AgentToolExecutor executor;

    public AgentToolCallbacks(AgentToolExecutor executor)
    {
        this.executor = executor;
    }

    public List<String> allToolNames()
    {
        return Arrays.asList(READ, WRITE, EDIT, BASH);
    }

    /** 按 enabled_tools（逗号分隔）过滤构建回调列表 */
    public List<ToolCallback> build(String enabledTools)
    {
        Set<String> enabled = (enabledTools == null || enabledTools.isBlank())
                ? Set.of()
                : Arrays.stream(enabledTools.split(",")).map(String::trim).collect(Collectors.toSet());
        List<ToolCallback> callbacks = new ArrayList<>();
        if (enabled.contains(READ))
        {
            callbacks.add(tool(READ, "读取沙箱目录内的文本文件内容",
                    "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\",\"description\":\"沙箱内的相对或绝对路径\"}},\"required\":[\"path\"]}",
                    args -> executor.read(args.getString("path"))));
        }
        if (enabled.contains(WRITE))
        {
            callbacks.add(tool(WRITE, "将文本内容写入沙箱目录内的文件（覆盖写入，自动建目录）",
                    "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\",\"description\":\"沙箱内的文件路径\"},\"content\":{\"type\":\"string\",\"description\":\"写入的完整文本内容\"}},\"required\":[\"path\",\"content\"]}",
                    args -> executor.write(args.getString("path"), args.getString("content"))));
        }
        if (enabled.contains(EDIT))
        {
            callbacks.add(tool(EDIT, "精确替换文件中的一段文本（old_text 必须与文件内容完全一致且唯一）",
                    "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\",\"description\":\"沙箱内的文件路径\"},\"old_text\":{\"type\":\"string\",\"description\":\"要被替换的原文本\"},\"new_text\":{\"type\":\"string\",\"description\":\"替换后的新文本\"}},\"required\":[\"path\",\"old_text\",\"new_text\"]}",
                    args -> executor.edit(args.getString("path"), args.getString("old_text"), args.getString("new_text"))));
        }
        if (enabled.contains(BASH))
        {
            callbacks.add(tool(BASH, "在沙箱目录下执行 shell 命令并返回退出码与输出。注意：Windows 下为 cmd.exe 语法——多命令用 & 分隔（不要用 ; ）、删除文件用 del、变量用 %var%、路径分隔用 \\",
                    "{\"type\":\"object\",\"properties\":{\"command\":{\"type\":\"string\",\"description\":\"要执行的命令\"}},\"required\":[\"command\"]}",
                    args -> executor.bash(args.getString("command"))));
        }
        return callbacks;
    }

    private ToolCallback tool(String name, String description, String inputSchema, JsonInvoker invoker)
    {
        ToolDefinition definition = DefaultToolDefinition.builder()
                .name(name)
                .description(description)
                .inputSchema(inputSchema)
                .build();
        return new ToolCallback()
        {
            @Override
            public ToolDefinition getToolDefinition()
            {
                return definition;
            }

            @Override
            public String call(String toolInput)
            {
                JSONObject args;
                try
                {
                    args = JSON.parseObject(toolInput);
                }
                catch (Exception e)
                {
                    throw new IllegalArgumentException("工具参数必须是 JSON 对象: " + toolInput);
                }
                try
                {
                    return invoker.invoke(args);
                }
                catch (RuntimeException e)
                {
                    throw e;
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage(), e);
                }
            }
        };
    }

    @FunctionalInterface
    private interface JsonInvoker
    {
        String invoke(JSONObject args) throws Exception;
    }
}
