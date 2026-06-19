package com.mine.explosive.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.entity.*;
import com.mine.explosive.enums.ExplosiveType;
import com.mine.explosive.enums.Role;
import com.mine.explosive.repository.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Component

public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    public DataInitializer(UserRepository userRepository, BlasterRepository blasterRepository, ExplosiveRepository explosiveRepository, WorkPlanRepository workPlanRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.blasterRepository = blasterRepository;
        this.explosiveRepository = explosiveRepository;
        this.workPlanRepository = workPlanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private final UserRepository userRepository;
    private final BlasterRepository blasterRepository;
    private final ExplosiveRepository explosiveRepository;
    private final WorkPlanRepository workPlanRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            initUsers();
            initExplosives();
            initWorkPlans();
            log.info("========================================");
            log.info("演示数据初始化完成！");
            log.info("爆破员账号: blaster / password");
            log.info("库管账号: storekeeper / password");
            log.info("安全负责人账号: safety / password");
            log.info("========================================");
        }
    }

    private void initUsers() {
        User blaster = new User();
        blaster.setUsername("blaster");
        blaster.setPassword(passwordEncoder.encode("password"));
        blaster.setName("张三");
        blaster.setRole(Role.BLASTER);
        blaster.setPhone("13800138001");
        userRepository.save(blaster);

        Blaster blasterInfo = new Blaster();
        blasterInfo.setUser(blaster);
        blasterInfo.setLicenseNo("BP2024001");
        blasterInfo.setLicenseExpiryDate(LocalDate.now().plusYears(1));
        blasterInfo.setIdCard("110101199001011234");
        blasterRepository.save(blasterInfo);

        User storekeeper = new User();
        storekeeper.setUsername("storekeeper");
        storekeeper.setPassword(passwordEncoder.encode("password"));
        storekeeper.setName("李四");
        storekeeper.setRole(Role.STOREKEEPER);
        storekeeper.setPhone("13800138002");
        userRepository.save(storekeeper);

        User safetyOfficer = new User();
        safetyOfficer.setUsername("safety");
        safetyOfficer.setPassword(passwordEncoder.encode("password"));
        safetyOfficer.setName("王五");
        safetyOfficer.setRole(Role.SAFETY_OFFICER);
        safetyOfficer.setPhone("13800138003");
        userRepository.save(safetyOfficer);

        User expiredBlaster = new User();
        expiredBlaster.setUsername("blaster_expired");
        expiredBlaster.setPassword(passwordEncoder.encode("password"));
        expiredBlaster.setName("赵六(过期)");
        expiredBlaster.setRole(Role.BLASTER);
        expiredBlaster.setPhone("13800138004");
        userRepository.save(expiredBlaster);

        Blaster expiredBlasterInfo = new Blaster();
        expiredBlasterInfo.setUser(expiredBlaster);
        expiredBlasterInfo.setLicenseNo("BP2023001");
        expiredBlasterInfo.setLicenseExpiryDate(LocalDate.now().minusDays(10));
        expiredBlasterInfo.setIdCard("110101199001015678");
        blasterRepository.save(expiredBlasterInfo);
    }

    private void initExplosives() {
        for (int i = 1; i <= 20; i++) {
            Explosive detonator = new Explosive();
            detonator.setSerialNo("DTN" + String.format("%06d", i));
            detonator.setType(ExplosiveType.DETONATOR);
            detonator.setName("电雷管");
            detonator.setSpecification("8号");
            detonator.setQuantity(50);
            detonator.setAvailableQuantity(50);
            detonator.setBatchNo("BATCH-DTN-2024-001");
            detonator.setProductionDate(LocalDateTime.now().minusMonths(3));
            detonator.setExpiryDate(LocalDateTime.now().plusYears(2));
            explosiveRepository.save(detonator);
        }

        for (int i = 1; i <= 15; i++) {
            Explosive explosive = new Explosive();
            explosive.setSerialNo("EXP" + String.format("%06d", i));
            explosive.setType(ExplosiveType.EXPLOSIVE);
            explosive.setName("乳化炸药");
            explosive.setSpecification("Φ32mm 200g");
            explosive.setQuantity(100);
            explosive.setAvailableQuantity(100);
            explosive.setBatchNo("BATCH-EXP-2024-001");
            explosive.setProductionDate(LocalDateTime.now().minusMonths(2));
            explosive.setExpiryDate(LocalDateTime.now().plusYears(1));
            explosiveRepository.save(explosive);
        }
    }

    private void initWorkPlans() {
        User blaster = userRepository.findByUsername("blaster").orElse(null);

        WorkPlan plan1 = new WorkPlan();
        plan1.setPlanNo("WP20240601001");
        plan1.setWorkFace("1101工作面");
        plan1.setDesignedHoles(30);
        plan1.setEstimatedDetonators(30);
        plan1.setEstimatedExplosives(120);
        plan1.setWorkDate(LocalDate.now());
        plan1.setBlaster(blaster);
        plan1.setDescription("1101工作面中深孔爆破作业");
        workPlanRepository.save(plan1);

        WorkPlan plan2 = new WorkPlan();
        plan2.setPlanNo("WP20240601002");
        plan2.setWorkFace("1202工作面");
        plan2.setDesignedHoles(20);
        plan2.setEstimatedDetonators(20);
        plan2.setEstimatedExplosives(80);
        plan2.setWorkDate(LocalDate.now());
        plan2.setBlaster(blaster);
        plan2.setDescription("1202工作面浅孔爆破作业");
        workPlanRepository.save(plan2);

        WorkPlan plan3 = new WorkPlan();
        plan3.setPlanNo("WP20240601003");
        plan3.setWorkFace("1303工作面");
        plan3.setDesignedHoles(50);
        plan3.setEstimatedDetonators(50);
        plan3.setEstimatedExplosives(200);
        plan3.setWorkDate(LocalDate.now().plusDays(1));
        plan3.setBlaster(blaster);
        plan3.setDescription("1303工作面深孔爆破作业");
        workPlanRepository.save(plan3);
    }
}
