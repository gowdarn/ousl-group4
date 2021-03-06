package ousl.group4.webapp.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

import ousl.group4.email.model.MailKeyBox;
import ousl.group4.email.model.MailSchedule;
import ousl.group4.email.model.MailSendType;
import ousl.group4.email.service.MailSender;
import ousl.group4.exception.NotificationAPIException;
import ousl.group4.model.PromotionCampaign;
import ousl.group4.model.User;
import ousl.group4.service.UserService;
import ousl.group4.sms.model.SmsKeyBox;
import ousl.group4.sms.model.SmsSchedule;
import ousl.group4.sms.service.SmsSender;
import ousl.group4.webapp.util.SpreadSheetUtil;
import ousl.group4.webapp.validator.PromotionalCampaignValidator;

@Controller
@SessionAttributes(value = "promotionCampaign")
public class PromotionalCampaignController {

    @Autowired
    private UserService userService;
    @Autowired
    private PromotionalCampaignValidator promotionalCampaignValidator;
    @Autowired
    private MailSender mailSender;
    @Autowired
    private SmsSender smsSender;

    /**
     * initialize email campaign form
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/email-promotion.html")
    public String initEmailForm(ModelMap modelMap) {
        PromotionCampaign promotionCampaign = new PromotionCampaign();
        promotionCampaign.setType("email");
        modelMap.addAttribute("promotionCampaign", promotionCampaign);
        return "email-promotion";
    }

    /**
     * initialize sms campaign form
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/sms-promotion.html")
    public String initSmsForm(ModelMap modelMap) {
        PromotionCampaign promotionCampaign = new PromotionCampaign();
        promotionCampaign.setType("sms");
        modelMap.addAttribute("promotionCampaign", promotionCampaign);
        return "sms-promotion";
    }

    /**
     * create email campaign
     *
     * @param promotionCampaign
     * @param bindingResult
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/createEmailPromotion.html")
    public String processEmailPromotionForm(@ModelAttribute("promotionCampaign") PromotionCampaign promotionCampaign,
                                            BindingResult bindingResult, SessionStatus sessionStatus) throws IOException {
        promotionalCampaignValidator.validate(promotionCampaign, bindingResult);
        if (bindingResult.hasErrors()) {
            // validation fails
            return "email-promotion";
        } else {
            List<String> recipientEmailAddress = new ArrayList<String>();
            // if registered user selected
            if (promotionCampaign.getUser().equalsIgnoreCase("R")) {
                for (User user : userService.getUsers()) {
                    recipientEmailAddress.add(user.getEmail());
                }
            }
            // if unregistered users selected
            if (promotionCampaign.getUser().equalsIgnoreCase("U")) {
                MultipartFile spreadSheet = promotionCampaign.getSpreadsheet();
                recipientEmailAddress = SpreadSheetUtil.readSpreadSheet(spreadSheet.getInputStream());
            }
            String[] mailAddresses = (String[]) recipientEmailAddress.toArray(new String[recipientEmailAddress.size()]);
            String[][] recipients = new String[mailAddresses.length][2];
            for (int i = 0; i < mailAddresses.length; i++) {
                recipients[i][0] = mailAddresses[i];
                recipients[i][1] = MailSendType.SEND_TO;
            }

            // get attachments
            List<String> attachmentPath = new ArrayList<String>();
            for (MultipartFile multipartFile : promotionCampaign.getFiles()) {
                if (multipartFile.getSize() > 0) {
                    String fileName = multipartFile.getOriginalFilename();
                    File file = new File("/tmp/" + fileName);
                    multipartFile.transferTo(file);
                    attachmentPath.add(file.getAbsolutePath());
                }
            }
            String[] attachments = (String[]) attachmentPath.toArray(new String[attachmentPath.size()]);

            // generate email
            Map<String, Object> mailMap = new HashMap<String, Object>();
            mailMap.put(MailKeyBox.SENDER, "ouslgroup4@gmail.com");
            mailMap.put(MailKeyBox.SUBJECT, promotionCampaign.getSubject());
            mailMap.put(MailKeyBox.MAIL_BODY, promotionCampaign.getMessage());
            mailMap.put(MailKeyBox.ATTACHMENTS, attachments);
            mailMap.put(MailKeyBox.RECIPIENTS, recipients);

            //if not select schedule
            if(!promotionCampaign.getSchedule()){
                try {
                    mailSender.send(mailMap);
                } catch (NotificationAPIException e) {
                    e.printStackTrace();
                }
            } else {
                MailSchedule mailSchedule = mailSender.isScheduleJobExist(promotionCampaign.getJobName());
                if(mailSchedule == null){
                    mailSchedule = new MailSchedule();
                    mailSchedule.setJobName(promotionCampaign.getJobName());
                    mailSchedule.setScheduleType(MailKeyBox.FIRE_ONCE);
                    Date date = promotionCampaign.getScheduleDate();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.set(Calendar.SECOND, promotionCampaign.getSecond());
                    calendar.set(Calendar.MINUTE, promotionCampaign.getMinute());
                    calendar.set(Calendar.HOUR_OF_DAY, promotionCampaign.getHour());
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    mailSchedule.setScheduleDateTime(calendar.getTime());
                    mailSender.saveMailSchedule(mailSchedule);

                    try {
                        mailSender.scheduleMail(mailMap, mailSchedule);
                    } catch (NotificationAPIException e) {
                        e.printStackTrace();
                    }
                }

            }

            sessionStatus.setComplete();
            // validation pass
            return "promotion-success";
        }
    }

    /**
     * create sms campaign
     *
     * @param promotionCampaign
     * @param bindingResult
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/createSmsPromotion.html")
    public String processSmsPromotionForm(@ModelAttribute("promotionCampaign") PromotionCampaign promotionCampaign,
                                          BindingResult bindingResult, SessionStatus sessionStatus) throws IOException, NotificationAPIException {
        promotionalCampaignValidator.validate(promotionCampaign, bindingResult);
        if (bindingResult.hasErrors()) {
            // validation fails
            return "sms-promotion";
        } else {
            List<String> phoneNumbers = new ArrayList<String>();
            // if registered user selected
            if (promotionCampaign.getUser().equalsIgnoreCase("R")) {
                for (User user : userService.getUsers()) {
                    phoneNumbers.add(user.getMobileNumber());
                }
            }
            // if unregistered users selected
            if (promotionCampaign.getUser().equalsIgnoreCase("U")) {
                MultipartFile spreadSheet = promotionCampaign.getSpreadsheet();
                phoneNumbers = SpreadSheetUtil.readSpreadSheet(spreadSheet.getInputStream());
            }
            // generate sms
            Map<String, Object> smsMap = new HashMap<String, Object>();
            smsMap.put(SmsKeyBox.SENDER, "0720260442");
            smsMap.put(SmsKeyBox.RECIPIENTS, (String[]) phoneNumbers.toArray(new String[phoneNumbers.size()]));
            smsMap.put(SmsKeyBox.SMS_BODY, promotionCampaign.getMessage());

            //if not select schedule
            if(!promotionCampaign.getSchedule()){
                smsSender.send(smsMap);
            } else {
                SmsSchedule smsSchedule = smsSender.isScheduleJobExist(promotionCampaign.getJobName());
                if(smsSchedule == null){
                    smsSchedule = new SmsSchedule();
                    smsSchedule.setJobName(promotionCampaign.getJobName());
                    smsSchedule.setScheduleType(SmsKeyBox.FIRE_ONCE);
                    Date date = promotionCampaign.getScheduleDate();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.set(Calendar.SECOND, promotionCampaign.getSecond());
                    calendar.set(Calendar.MINUTE, promotionCampaign.getMinute());
                    calendar.set(Calendar.HOUR_OF_DAY, promotionCampaign.getHour());
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    smsSchedule.setScheduleDateTime(calendar.getTime());
                    smsSender.saveSmsSchedule(smsSchedule);

                    try {
                        smsSender.scheduleSms(smsMap, smsSchedule);
                    } catch (NotificationAPIException e) {
                        e.printStackTrace();
                    }
                }
            }

            sessionStatus.setComplete();
            // validation pass
            return "promotion-success";
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(promotionalCampaignValidator);
        binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @ModelAttribute("hours")
    public List<Integer> populateHours(){
        List<Integer> hours = new ArrayList<Integer>();
        for(int i=0; i<24; i++){
            hours.add(i);
        }
        return hours;
    }

    @ModelAttribute("minutes")
    public List<Integer> populateMinutes(){
        List<Integer> minutes = new ArrayList<Integer>();
        for(int i=0; i<60; i++){
            minutes.add(i);
        }
        return minutes;
    }

    @ModelAttribute("seconds")
    public List<Integer> populateSeconds(){
        List<Integer> seconds = new ArrayList<Integer>();
        for(int i=0; i<60; i++){
            seconds.add(i);
        }
        return seconds;
    }
}
