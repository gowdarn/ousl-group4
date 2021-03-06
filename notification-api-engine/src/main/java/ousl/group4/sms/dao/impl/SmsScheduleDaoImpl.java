package ousl.group4.sms.dao.impl;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import ousl.group4.sms.dao.SmsScheduleDao;
import ousl.group4.sms.model.SmsSchedule;
import ousl.group4.util.HibernateUtil;

public class SmsScheduleDaoImpl implements SmsScheduleDao{

    /**
     * persist sms schedule
     *
     * @param smsSchedule
     */
    @Override
    public SmsSchedule saveSmsSchedule(SmsSchedule smsSchedule) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
            session.save(smsSchedule);
            session.getTransaction().commit();
            return smsSchedule;
        } catch (Exception ex) {
            ex.printStackTrace();
            session.getTransaction().rollback();
            return null;
        } finally {
        }
    }

    /**
     * return SmsSchedule object if job already exist
     *
     * @param jobName
     * @return
     */
    @Override
    public SmsSchedule isScheduleJobExist(String jobName) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
            SmsSchedule mailSchedule = (SmsSchedule)session
                    .createCriteria(SmsSchedule.class).add(Restrictions.eq("jobName", jobName)).uniqueResult();
            session.getTransaction().commit();
            return mailSchedule;
        } catch (Exception ex){
            ex.printStackTrace();
            session.getTransaction().rollback();
            return null;
        } finally {
        }
    }
}
