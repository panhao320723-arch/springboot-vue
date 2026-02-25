package com.campus.lostandfound.config;

import com.campus.lostandfound.entity.Announcement;
import com.campus.lostandfound.entity.Item;
import com.campus.lostandfound.entity.User;
import com.campus.lostandfound.repository.AnnouncementRepository;
import com.campus.lostandfound.repository.ItemRepository;
import com.campus.lostandfound.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 数据库初始化器
 * 用于在系统启动时检测数据库是否为空，并自动填充一些测试数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> 正在检查数据库数据...");
        
        initUsers();
        initAnnouncements();
        initItems();
        
        System.out.println(">>> 数据库检查/初始化完成！");
    }

    private void initUsers() {
        System.out.println(">>> 初始化用户数据...");

        // 1. 超级管理员
        if (userRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // 实际项目中应加密
            admin.setNickname("超级管理员");
            admin.setPhone("13800000000");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println(">>> 创建管理员: admin");
        }

        // 2. 普通用户 - 张三
        if (userRepository.findByUsername("zhangsan") == null) {
            User zhangsan = new User();
            zhangsan.setUsername("zhangsan");
            zhangsan.setPassword("123456");
            zhangsan.setNickname("热心张三");
            zhangsan.setPhone("13911111111");
            zhangsan.setRole("USER");
            userRepository.save(zhangsan);
            System.out.println(">>> 创建用户: zhangsan");
        }

        // 3. 普通用户 - 李四
        if (userRepository.findByUsername("lisi") == null) {
            User lisi = new User();
            lisi.setUsername("lisi");
            lisi.setPassword("123456");
            lisi.setNickname("迷糊李四");
            lisi.setPhone("13922222222");
            lisi.setRole("USER");
            userRepository.save(lisi);
            System.out.println(">>> 创建用户: lisi");
        }
    }

    private void initAnnouncements() {
        if (announcementRepository.count() > 0) return;

        System.out.println(">>> 初始化公告数据...");

        Announcement a1 = new Announcement();
        a1.setTitle("欢迎来到校园失物招领平台");
        a1.setContent("这是一个互帮互助的平台，请大家文明发言，诚实守信。如果在校园内捡到物品，请及时发布招领信息；如果丢失物品，也可以在这里发布寻物启事。");
        announcementRepository.save(a1);

        Announcement a2 = new Announcement();
        a2.setTitle("关于防范诈骗的温馨提示");
        a2.setContent("近期发现有不法分子利用失物招领信息进行诈骗，请大家在认领物品时注意核实对方身份，不要轻易转账汇款。");
        announcementRepository.save(a2);
    }

    private void initItems() {
        if (itemRepository.count() > 0) return;

        System.out.println(">>> 初始化失物招领数据...");

        User zhangsan = userRepository.findByUsername("zhangsan");
        User lisi = userRepository.findByUsername("lisi");

        if (zhangsan == null || lisi == null) return;

        // 1. 张三捡到了一张校园卡
        Item item1 = new Item();
        item1.setType(1); // 招领
        item1.setTitle("在二教门口捡到一张校园卡");
        item1.setDescription("捡到一张计算机学院王同学的校园卡，卡号尾号 1234，请失主尽快联系我。");
        item1.setLocation("第二教学楼正门");
        item1.setDate(new Date());
        item1.setStatus(0); // 未解决
        item1.setUserId(zhangsan.getId());
        item1.setContactInfo("QQ: 10001");
        itemRepository.save(item1);

        // 2. 李四丢了耳机
        Item item2 = new Item();
        item2.setType(0); // 寻物
        item2.setTitle("急！丢失黑色蓝牙耳机");
        item2.setDescription("昨天在操场跑步时不慎丢失一个黑色蓝牙耳机盒，上面有皮卡丘的贴纸。这对我很重要，必有重谢！");
        item2.setLocation("西操场跑道");
        item2.setDate(new Date(System.currentTimeMillis() - 86400000)); // 昨天
        item2.setStatus(0);
        item2.setUserId(lisi.getId());
        item2.setContactInfo("手机: 13922222222");
        itemRepository.save(item2);

        // 3. 张三丢了水杯 (已解决)
        Item item3 = new Item();
        item3.setType(0);
        item3.setTitle("寻找蓝色保温杯");
        item3.setDescription("在图书馆三楼自习室丢失一个蓝色保温杯，膳魔师品牌的。");
        item3.setLocation("图书馆301");
        item3.setDate(new Date(System.currentTimeMillis() - 172800000)); // 前天
        item3.setStatus(1); // 已解决
        item3.setUserId(zhangsan.getId());
        itemRepository.save(item3);
    }
}
