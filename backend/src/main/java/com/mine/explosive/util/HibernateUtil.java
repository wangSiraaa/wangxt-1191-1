package com.mine.explosive.util;

import com.mine.explosive.entity.PickupApplication;
import com.mine.explosive.entity.Shift;
import com.mine.explosive.entity.WorkPlan;
import com.mine.explosive.entity.User;
import org.hibernate.Hibernate;

public class HibernateUtil {

    public static void initShift(Shift shift) {
        if (shift == null) {
            return;
        }
        if (shift.getBlaster() != null) {
            Hibernate.initialize(shift.getBlaster());
        }
        if (shift.getWorkPlan() != null) {
            Hibernate.initialize(shift.getWorkPlan());
            initWorkPlan(shift.getWorkPlan());
        }
    }

    public static void initWorkPlan(WorkPlan workPlan) {
        if (workPlan == null) {
            return;
        }
        if (workPlan.getBlaster() != null) {
            Hibernate.initialize(workPlan.getBlaster());
        }
    }

    public static void initPickupApplication(PickupApplication app) {
        if (app == null) {
            return;
        }
        if (app.getBlaster() != null) {
            Hibernate.initialize(app.getBlaster());
        }
        if (app.getShift() != null) {
            Hibernate.initialize(app.getShift());
            initShift(app.getShift());
        }
        if (app.getReviewer() != null) {
            Hibernate.initialize(app.getReviewer());
        }
    }
}
