package dev.lone.pocketmobs;

import org.bukkit.NamespacedKey;

/**
 * 插件常量类
 * 统一管理所有常量、魔法数字和硬编码字符串
 */
public final class Constants
{
    private Constants() {}
    
    // 插件命名空间
    public static final String NAMESPACE = "pocketmobs";
    
    // 最大数据大小限制（字节）
    public static final int MAX_DATA_SIZE = 1024 * 1024; // 1MB
    
    // 最大缓存数量
    public static final int MAX_CACHE_SIZE = 500;
    
    // 最大命令数量
    public static final int MAX_COMMAND_AMOUNT = 64;
    
    // 最大球名称长度
    public static final int MAX_BALL_NAME_LENGTH = 50;

    // 权限节点
    public static final class Permissions
    {
        public static final String ADMIN_GET = "pocketmob.admin.get";
        public static final String ADMIN_GIVE = "pocketmob.admin.give";
        public static final String ADMIN_RELOAD = "pocketmob.admin.reload";
        public static final String ADMIN_DEBUG = "pocketmob.admin.debug";
        public static final String USER_CRAFT = "pocketmob.user.craft";
        public static final String USER_RECIPES = "pocketmob.user.recipes";
        public static final String USER_BUY = "pocketmob.user.buy";
        public static final String USER_CATCH = "pocketmob.user.catch";
        public static final String USER_RELEASE = "pocketmob.user.release";
    }
    
    // 创建NamespacedKey的工具方法
    public static NamespacedKey key(String key)
    {
        return new NamespacedKey(NAMESPACE, key.toLowerCase());
    }
}
