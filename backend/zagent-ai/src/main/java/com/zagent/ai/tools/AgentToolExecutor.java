package com.zagent.ai.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.zagent.common.exception.ServiceException;
import com.zagent.common.utils.StringUtils;
import jakarta.annotation.PostConstruct;

/**
 * 智能体工具执行器：read / write / edit / bash 四个工具的实际执行体。
 * read/write/edit 限制在沙箱目录内（路径规范化防逃逸）；bash 工作目录为沙箱目录，
 * 受超时与输出截断约束（注：bash 命令本身技术上仍可访问全盘，仅建议可信环境使用）。
 *
 * @author zagent
 */
@Component
public class AgentToolExecutor
{
    private static final Logger log = LoggerFactory.getLogger(AgentToolExecutor.class);

    private static final int READ_LIMIT = 256 * 1024;

    private static final int OUTPUT_LIMIT = 64 * 1024;

    @Value("${zagent.ai.workspace:./ai-workspace}")
    private String workspace;

    @Value("${zagent.ai.bashTimeoutSeconds:60}")
    private int bashTimeoutSeconds;

    private Path root;

    @PostConstruct
    public void init() throws IOException
    {
        root = Paths.get(workspace).toAbsolutePath().normalize();
        Files.createDirectories(root);
        log.info("AI 工具沙箱目录：{}", root);
    }

    public String read(String path) throws IOException
    {
        Path target = resolve(path);
        if (!Files.exists(target))
        {
            throw new ServiceException("文件不存在: " + path);
        }
        if (Files.size(target) > READ_LIMIT)
        {
            throw new ServiceException("文件过大（>256KB）: " + path);
        }
        return Files.readString(target, StandardCharsets.UTF_8);
    }

    public String write(String path, String content) throws IOException
    {
        Path target = resolve(path);
        if (target.getParent() != null)
        {
            Files.createDirectories(target.getParent());
        }
        Files.writeString(target, content == null ? "" : content, StandardCharsets.UTF_8);
        return "已写入 " + path + " (" + (content == null ? 0 : content.length()) + " 字符)";
    }

    public String edit(String path, String oldText, String newText) throws IOException
    {
        Path target = resolve(path);
        if (!Files.exists(target))
        {
            throw new ServiceException("文件不存在: " + path);
        }
        String content = Files.readString(target, StandardCharsets.UTF_8);
        int idx = content.indexOf(oldText == null ? "" : oldText);
        if (idx < 0)
        {
            throw new ServiceException("未找到要替换的文本片段（old_text），请先用 read 确认文件内容");
        }
        String updated = content.substring(0, idx) + (newText == null ? "" : newText)
                + content.substring(idx + oldText.length());
        Files.writeString(target, updated, StandardCharsets.UTF_8);
        return "已修改 " + path;
    }

    public String bash(String command) throws Exception
    {
        if (StringUtils.isEmpty(command))
        {
            throw new ServiceException("命令为空");
        }
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        ProcessBuilder pb = windows
                ? new ProcessBuilder("cmd.exe", "/c", command)
                : new ProcessBuilder("sh", "-c", command);
        pb.directory(root.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output;
        boolean finished = process.waitFor(bashTimeoutSeconds, TimeUnit.SECONDS);
        if (!finished)
        {
            process.destroyForcibly();
            throw new ServiceException("命令执行超时（>" + bashTimeoutSeconds + "s）: " + command);
        }
        byte[] bytes = process.getInputStream().readAllBytes();
        output = new String(bytes, StandardCharsets.UTF_8);
        if (output.length() > OUTPUT_LIMIT)
        {
            output = output.substring(0, OUTPUT_LIMIT) + "\n...(输出截断)";
        }
        int code = process.exitValue();
        return "exit=" + code + "\n" + output;
    }

    /** 路径规范化并强制限制在沙箱目录内 */
    private Path resolve(String path)
    {
        if (StringUtils.isEmpty(path))
        {
            throw new ServiceException("路径为空");
        }
        Path p = Paths.get(path);
        Path target = (p.isAbsolute() ? p : root.resolve(path)).normalize();
        if (!target.startsWith(root))
        {
            throw new ServiceException("路径越界（仅允许沙箱目录内）: " + path);
        }
        return target;
    }

    public String getWorkspace()
    {
        return root.toString();
    }

    public File getWorkspaceDir()
    {
        return root.toFile();
    }
}
