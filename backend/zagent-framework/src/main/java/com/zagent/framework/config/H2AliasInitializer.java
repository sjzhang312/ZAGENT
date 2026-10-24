package com.zagent.framework.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import jakarta.annotation.PostConstruct;

/**
 * H2 兼容别名初始化器（h2 profile）
 * 在 SqlSessionFactory（进而早于字典/参数缓存的 @PostConstruct）之前，
 * 于 H2 数据库内注册 MySQL 方言兼容函数：FIND_IN_SET / DATE_FORMAT。
 * 使用内联 Java 源码别名，由 H2 服务端自行编译，不依赖应用类路径。
 * 说明：mapper XML 中的 sysdate() 已统一替换为两库通用的 CURRENT_TIMESTAMP。
 *
 * @author zagent
 */
@Profile("h2")
@Configuration
public class H2AliasInitializer
{
    private static final Logger log = LoggerFactory.getLogger(H2AliasInitializer.class);

    /** 等价 MySQL FIND_IN_SET(str, strlist)：返回位置（1 起），未找到返回 0 */
    private static final String ALIAS_FIND_IN_SET =
        "CREATE ALIAS IF NOT EXISTS FIND_IN_SET AS "
        + "'int findInSet(String str, String list) { "
        + "  if (str == null || list == null) { return 0; } "
        + "  String[] a = list.split(\",\"); "
        + "  for (int i = 0; i < a.length; i++) { if (a[i].trim().equals(str)) { return i + 1; } } "
        + "  return 0; }'";

    /** 等价 MySQL DATE_FORMAT(date, pattern)：支持 %Y %y %m %d %H %i %s */
    private static final String ALIAS_DATE_FORMAT =
        "CREATE ALIAS IF NOT EXISTS DATE_FORMAT AS "
        + "'String dateFormat(String date, String pattern) { "
        + "  if (date == null || pattern == null) { return null; } "
        + "  String d = date.length() > 19 ? date.substring(0, 19) : date; "
        + "  java.util.Date x; "
        + "  try { x = new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\").parse(d); } "
        + "  catch (Exception e) { "
        + "    try { x = new java.text.SimpleDateFormat(\"yyyy-MM-dd\").parse(d); } "
        + "    catch (Exception e2) { throw new RuntimeException(\"bad date: \" + date); } "
        + "  } "
        + "  StringBuilder sb = new StringBuilder(); "
        + "  for (int i = 0; i < pattern.length(); i++) { "
        + "    char c = pattern.charAt(i); "
        + "    if (c == 37 && i + 1 < pattern.length()) { "
        + "      char n = pattern.charAt(++i); "
        + "      if (n == 89) { sb.append(\"yyyy\"); } "
        + "      else if (n == 121) { sb.append(\"yy\"); } "
        + "      else if (n == 109) { sb.append(\"MM\"); } "
        + "      else if (n == 100) { sb.append(\"dd\"); } "
        + "      else if (n == 72) { sb.append(\"HH\"); } "
        + "      else if (n == 105) { sb.append(\"mm\"); } "
        + "      else if (n == 115) { sb.append(\"ss\"); } "
        + "      else { sb.append(n); } "
        + "    } else { sb.append(c); } "
        + "  } "
        + "  return new java.text.SimpleDateFormat(sb.toString()).format(x); "
        + "}'";

    private final DataSource dataSource;

    public H2AliasInitializer(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init()
    {
        // 先经 Druid 池探测数据源类型（连接失败时给出友好提示）
        String h2Url;
        try (Connection conn = dataSource.getConnection())
        {
            DatabaseMetaData meta = conn.getMetaData();
            if (!meta.getURL().startsWith("jdbc:h2:"))
            {
                log.info("非 H2 数据源（{}），跳过兼容别名注册", meta.getURL());
                return;
            }
            h2Url = meta.getURL();
        }
        catch (Exception e)
        {
            throw new IllegalStateException(
                "初始化 H2 失败：无法连接 H2 数据库。请先运行项目根目录的 启动H2数据库.bat（jdbc:h2:tcp://localhost:9101）。"
                    + e.getMessage(), e);
        }
        // DriverManager 直连注册别名（绕过 Druid 过滤链，避免 stat 插件解析 H2 DDL 产生的报错噪音）
        try (Connection conn = java.sql.DriverManager.getConnection(h2Url, "sa", "");
             Statement st = conn.createStatement())
        {
            st.execute(ALIAS_FIND_IN_SET);
            st.execute(ALIAS_DATE_FORMAT);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("注册 H2 兼容别名失败: " + e.getMessage(), e);
        }
        log.info("H2 兼容别名注册完成：FIND_IN_SET / DATE_FORMAT");
    }

    /**
     * 保证本初始化器先于 SqlSessionFactory 执行：
     * 给 sqlSessionFactory 增加 dependsOn("h2AliasInitializer")，
     * 而所有 Service 的 @PostConstruct（字典/参数缓存）均依赖 Mapper → SqlSessionFactory，从而间接后置。
     */
    @Profile("h2")
    @Configuration
    public static class DependencyOrderConfig implements BeanDefinitionRegistryPostProcessor
    {
        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
        {
            // 无需处理：仅通过 postProcessBeanFactory 调整依赖顺序
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
        {
            if (beanFactory.containsBeanDefinition("sqlSessionFactory"))
            {
                BeanDefinition bd = beanFactory.getBeanDefinition("sqlSessionFactory");
                bd.setDependsOn(merge(bd.getDependsOn(), "h2AliasInitializer"));
            }
        }

        private String[] merge(String[] existing, String dep)
        {
            if (existing == null || existing.length == 0)
            {
                return new String[] { dep };
            }
            for (String e : existing)
            {
                if (dep.equals(e))
                {
                    return existing;
                }
            }
            String[] merged = new String[existing.length + 1];
            System.arraycopy(existing, 0, merged, 0, existing.length);
            merged[existing.length] = dep;
            return merged;
        }
    }
}
