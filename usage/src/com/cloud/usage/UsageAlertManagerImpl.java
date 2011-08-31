/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
*
 *
 */

package com.cloud.usage;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.ejb.Local;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.alert.AlertManager;
import com.cloud.alert.AlertVO;
import com.cloud.alert.dao.AlertDao;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ComponentLocator;
import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.smtp.SMTPSSLTransport;
import com.sun.mail.smtp.SMTPTransport;

@Local(value={AlertManager.class})
public class UsageAlertManagerImpl implements AlertManager {
    private static final Logger s_logger = Logger.getLogger(UsageAlertManagerImpl.class.getName());

    private String _name = null;
    private EmailAlert _emailAlert;
    private AlertDao _alertDao;
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _name = name;

        ComponentLocator locator = ComponentLocator.getCurrentLocator();
        ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
        if (configDao == null) {
            s_logger.error("Unable to get the configuration dao.");
            return false;
        }

        Map<String, String> configs = configDao.getConfiguration("management-server", params);

        // set up the email system for alerts
        String emailAddressList = configs.get("alert.email.addresses");
        String[] emailAddresses = null;
        if (emailAddressList != null) {
            emailAddresses = emailAddressList.split(",");
        }

        String smtpHost = configs.get("alert.smtp.host");
        int smtpPort = NumbersUtil.parseInt(configs.get("alert.smtp.port"), 25);
        String useAuthStr = configs.get("alert.smtp.useAuth");
        boolean useAuth = ((useAuthStr == null) ? false : Boolean.parseBoolean(useAuthStr));
        String smtpUsername = configs.get("alert.smtp.username");
        String smtpPassword = configs.get("alert.smtp.password");
        String emailSender = configs.get("alert.email.sender");
        String smtpDebugStr = configs.get("alert.smtp.debug");
        boolean smtpDebug = false;
        if (smtpDebugStr != null) {
            smtpDebug = Boolean.parseBoolean(smtpDebugStr);
        }

        _emailAlert = new EmailAlert(emailAddresses, smtpHost, smtpPort, useAuth, smtpUsername, smtpPassword, emailSender, smtpDebug);

        _alertDao = locator.getDao(AlertDao.class);
        if (_alertDao == null) {
            s_logger.error("Unable to get the alert dao.");
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public void clearAlert(short alertType, long dataCenterId, long podId) {
        try {
            if (_emailAlert != null) {
                _emailAlert.clearAlert(alertType, dataCenterId, podId);
            }
        } catch (Exception ex) {
            s_logger.error("Problem clearing email alert", ex);
        }
    }

    @Override
    public void sendAlert(short alertType, long dataCenterId, Long podId, String subject, String body) {
        // TODO:  queue up these messages and send them as one set of issues once a certain number of issues is reached?  If that's the case,
        //         shouldn't we have a type/severity as part of the API so that severe errors get sent right away?
        try {
            if (_emailAlert != null) {
                _emailAlert.sendAlert(alertType, dataCenterId, podId, subject, body);
            }
        } catch (Exception ex) {
            s_logger.error("Problem sending email alert", ex);
        }
    }


    class EmailAlert {
        private Session _smtpSession;
        private InternetAddress[] _recipientList;
        private final String _smtpHost;
        private int _smtpPort = -1;
        private boolean _smtpUseAuth = false;
        private final String _smtpUsername;
        private final String _smtpPassword;
        private final String _emailSender;

        public EmailAlert(String[] recipientList, String smtpHost, int smtpPort, boolean smtpUseAuth, final String smtpUsername, final String smtpPassword, String emailSender, boolean smtpDebug) {
            if (recipientList != null) {
                _recipientList = new InternetAddress[recipientList.length];
                for (int i = 0; i < recipientList.length; i++) {
                    try {
                        _recipientList[i] = new InternetAddress(recipientList[i], recipientList[i]);
                    } catch (Exception ex) {
                        s_logger.error("Exception creating address for: " + recipientList[i], ex);
                    }
                }
            }

            _smtpHost = smtpHost;
            _smtpPort = smtpPort;
            _smtpUseAuth = smtpUseAuth;
            _smtpUsername = smtpUsername;
            _smtpPassword = smtpPassword;
            _emailSender = emailSender;

            if (_smtpHost != null) {
                Properties smtpProps = new Properties();
                smtpProps.put("mail.smtp.host", smtpHost);
                smtpProps.put("mail.smtp.port", smtpPort);
                smtpProps.put("mail.smtp.auth", ""+smtpUseAuth);
                if (smtpUsername != null) {
                    smtpProps.put("mail.smtp.user", smtpUsername);
                }

                smtpProps.put("mail.smtps.host", smtpHost);
                smtpProps.put("mail.smtps.port", smtpPort);
                smtpProps.put("mail.smtps.auth", ""+smtpUseAuth);
                if (smtpUsername != null) {
                    smtpProps.put("mail.smtps.user", smtpUsername);
                }

                if ((smtpUsername != null) && (smtpPassword != null)) {
                    _smtpSession = Session.getInstance(smtpProps, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(smtpUsername, smtpPassword);
                        }
                    });
                } else {
                    _smtpSession = Session.getInstance(smtpProps);
                }
                _smtpSession.setDebug(smtpDebug);
            } else {
                _smtpSession = null;
            }
        }

        // TODO:  make sure this handles SSL transport (useAuth is true) and regular
        public void sendAlert(short alertType, long dataCenterId, Long podId, String subject, String content) throws MessagingException, UnsupportedEncodingException {
            AlertVO alert = null;
            
            if ((alertType != AlertManager.ALERT_TYPE_HOST) &&
                (alertType != AlertManager.ALERT_TYPE_USERVM) &&
                (alertType != AlertManager.ALERT_TYPE_DOMAIN_ROUTER) &&
                (alertType != AlertManager.ALERT_TYPE_CONSOLE_PROXY) &&
                (alertType != AlertManager.ALERT_TYPE_STORAGE_MISC) &&
                (alertType != AlertManager.ALERT_TYPE_MANAGMENT_NODE)) {
                alert = _alertDao.getLastAlert(alertType, dataCenterId, podId);
            }
            
            if (alert == null) {
                // set up a new alert
                AlertVO newAlert = new AlertVO();
                newAlert.setType(alertType);
                newAlert.setSubject(subject);
                newAlert.setPodId(podId);
                newAlert.setDataCenterId(dataCenterId);
                newAlert.setSentCount(1); // initialize sent count to 1 since we are now sending an alert
                newAlert.setLastSent(new Date());
                _alertDao.persist(newAlert);
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Have already sent: " + alert.getSentCount() + " emails for alert type '" + alertType + "' -- skipping send email");
                }
                return;
            }

            if (_smtpSession != null) {
                SMTPMessage msg = new SMTPMessage(_smtpSession);
                msg.setSender(new InternetAddress(_emailSender, _emailSender));
                msg.setFrom(new InternetAddress(_emailSender, _emailSender));
                for (InternetAddress address : _recipientList) {
                    msg.addRecipient(RecipientType.TO, address);
                }
                msg.setSubject(subject);
                msg.setSentDate(new Date());
                msg.setContent(content, "text/plain");
                msg.saveChanges();

                SMTPTransport smtpTrans = null;
                if (_smtpUseAuth) {
                    smtpTrans = new SMTPSSLTransport(_smtpSession, new URLName("smtp", _smtpHost, _smtpPort, null, _smtpUsername, _smtpPassword));
                } else {
                    smtpTrans = new SMTPTransport(_smtpSession, new URLName("smtp", _smtpHost, _smtpPort, null, _smtpUsername, _smtpPassword));
                }
                smtpTrans.connect();
                smtpTrans.sendMessage(msg, msg.getAllRecipients());
                smtpTrans.close();
            }
        }

        public void clearAlert(short alertType, long dataCenterId, Long podId) {
            if (alertType != -1) {
                AlertVO alert = _alertDao.getLastAlert(alertType, dataCenterId, podId);
                if (alert != null) {
                    AlertVO updatedAlert = _alertDao.createForUpdate();
                    updatedAlert.setResolved(new Date());
                    _alertDao.update(alert.getId(), updatedAlert);
                }
            }
        }
    }

    @Override
    public void recalculateCapacity() {
        // TODO Auto-generated method stub
        
    }
}
