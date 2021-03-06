package ousl.group4.email.service;

import java.util.List;
import java.util.Map;

import ousl.group4.email.model.Mail;
import ousl.group4.exception.NotificationAPIException;
import ousl.group4.email.model.MailSchedule;

/**
 * End users use implementation of this interface to send email
 */
public interface MailSender {

    /**
     * @param mailMap Must contains the below key names with relevant values
     *                <p>
     *                sender : the sender email address. cannot be null<br/>
     *                recipients : the receiver(s) email addresses and send type (String[][] array object). first array element: email address. second array element: pass one of constant in MailSendType. cannot be null. refer: MailSendType<br/>
     *                subject : the subject of email. cannot be null<br/>
     *                mailBody : the body of email. cannot be null<br/>
     *                attachments : Attachment(s) if necessary (Optional). must contain absolute path of file(s) (String[] array object).<br/>
     *                inlineImages : Inline image(s) if necessary (Optional). must contain absolute path of image(s) and content id (String[][] array object).</p>
     * @see ousl.group4.email.model.MailKeyBox
     * @throws ousl.group4.exception.NotificationAPIException
     */
    void send(Map<String, Object> mailMap) throws NotificationAPIException;

    /**
     * @param mailMap      Must contains the below key names with relevant values
     *                     <p>
     *                     sender : the sender email address. cannot be null<br/>
     *                     recipients : the receiver(s) email addresses and send type (String[][] array object). first array element: email address. second array element: pass one of constant in MailSendType. cannot be null. refer: MailSendType<br/>
     *                     subject : the subject of email. cannot be null<br/>
     *                     attachments : Attachment(s) if necessary (Optional). must contain absolute path of file(s) (String[] array object).<br/>
     *                     inlineImages : Inline image(s) if necessary (Optional). must contain absolute path of image(s) and content id (String[][] array object).<br/>
     *                     other key value pair of velocity template's place holders, cannot be null</p>
     * @see ousl.group4.email.model.MailKeyBox
     * @param templatePath classpath of template. cannot be null
     * @throws Exception
     */
    void send(Map<String, Object> mailMap, String templatePath) throws Exception;

    /**
     * @param mailMap      Must contains the below key names with relevant values
     *                     <p>
     *                     sender : the sender email address. cannot be null<br/>
     *                     recipients : the receiver(s) email addresses and send type (String[][] array object). first array element: email address. second array element: pass one of constant in MailSendType. cannot be null. refer: MailSendType<br/>
     *                     subject : the subject of email. cannot be null<br/>
     *                     mailBody : the body of email. cannot be null<br/>
     *                     attachments : Attachment(s) if necessary (Optional). must contain absolute path of file(s) (String[] array object).<br/>
     *                     inlineImages : Inline image(s) if necessary (Optional). must contain absolute path of image(s) and content id (String[][] array object).</p>
     * @see ousl.group4.email.model.MailKeyBox
     * @param mailSchedule contains schedule job data
     * @throws Exception
     */
    void scheduleMail(Map<String, Object> mailMap, MailSchedule mailSchedule) throws NotificationAPIException;

    /**
     * @param mailMap      Must contains the below key names with relevant values
     *                     <p>
     *                     sender : the sender email address. cannot be null<br/>
     *                     recipients : the receiver(s) email addresses and send type (String[][] array object). first array element: email address. second array element: pass one of constant in MailSendType. cannot be null. refer: MailSendType<br/>
     *                     subject : the subject of email. cannot be null<br/>
     *                     attachments : Attachment(s) if necessary (Optional). must contain absolute path of file(s) (String[] array object).<br/>
     *                     inlineImages : Inline image(s) if necessary (Optional). must contain absolute path of image(s) and content id (String[][] array object).<br/>
     *                     other key value pair of velocity template's place holders, cannot be null</p>
     * @see ousl.group4.email.model.MailKeyBox
     * @param templatePath classpath of template. cannot be null
     * @param mailSchedule contains schedule job data
     * @throws ousl.group4.exception.NotificationAPIException
     */
    void scheduleMail(Map<String, Object> mailMap, String templatePath, MailSchedule mailSchedule) throws Exception;

    /**
     * This method return List object of finished mail notification
     *
     * @return
     */
    List<Mail> getFinishedMailNotifications();

    /**
     * This method return List object of schedule mail notification
     *
     * @return
     */
    List<Mail> getAllScheduleMailNotifications();

    /**
     * return MailSchedule object if job already exist
     *
     * @param jobName
     * @return
     */
    MailSchedule isScheduleJobExist(String jobName);

    /**
     * persist mail schedule
     *
     * @param mailSchedule
     */
    void saveMailSchedule(MailSchedule mailSchedule);
}
