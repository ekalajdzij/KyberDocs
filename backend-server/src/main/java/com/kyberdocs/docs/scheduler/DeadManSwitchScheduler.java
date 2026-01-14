package com.kyberdocs.docs.scheduler;

import com.kyberdocs.docs.beneficiaries.AccessCondition;
import com.kyberdocs.docs.beneficiaries.BeneficiaryRepository;
import com.kyberdocs.docs.notifications.EmailService;
import com.kyberdocs.docs.users.User;
import com.kyberdocs.docs.users.UserRepository;
import com.kyberdocs.docs.users.UserStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class DeadManSwitchScheduler {

    private final UserRepository userRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final EmailService emailService;

    private static final long WARNING_GRACE_PERIOD_SECONDS = 24 * 60 * 60;

    public DeadManSwitchScheduler(UserRepository userRepository,
                                  BeneficiaryRepository beneficiaryRepository,
                                  EmailService emailService) {
        this.userRepository = userRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkInactivity() {
        checkWarnings();
        checkTriggers();
        System.out.println("Checking inactivity!!!");
    }

    private void checkWarnings() {
        List<User> activeUsers = userRepository.findAll();
        Instant now = Instant.now();

        for (User user : activeUsers) {
            if (user.getStatus() != UserStatus.STATUS_ACTIVE) continue;

            long timeoutSeconds = user.getInactivityTimeout();
            long elapsedSeconds = now.getEpochSecond() - user.getLastHeartbeat().toInstant().getEpochSecond();
            long remainingSeconds = timeoutSeconds - elapsedSeconds;

            if (remainingSeconds <= WARNING_GRACE_PERIOD_SECONDS && remainingSeconds > 0) {
                user.setStatus(UserStatus.STATUS_WARNING);
                userRepository.save(user);
                emailService.sendEmail(
                        user.getEmail(),
                        "Urgent: 24 Hours Remaining",
                        "Warning! Your Dead Man's Switch will trigger in less than 24 hours. Log in immediately to reset the timer."
                );
            }
        }
    }

    private void checkTriggers() {
        List<User> potentialTriggers = userRepository.findAll();
        Instant now = Instant.now();

        for (User user : potentialTriggers) {

            if (user.getStatus() == UserStatus.STATUS_TRIGGERED || user.getStatus() == UserStatus.STATUS_CLAIMED) continue;

            long timeoutSeconds = user.getInactivityTimeout();
            long elapsedSeconds = now.getEpochSecond() - user.getLastHeartbeat().toInstant().getEpochSecond();

            if (elapsedSeconds > timeoutSeconds) {
                triggerSwitch(user);
            }
        }
    }

    private void triggerSwitch(User user) {
        user.setStatus(UserStatus.STATUS_TRIGGERED);
        userRepository.save(user);

        emailService.sendEmail(user.getEmail(), "Protocol Triggered", "Your inactivity protocol has been executed.");

        var beneficiaries = beneficiaryRepository.findByOwner(user);
        for (var b : beneficiaries) {
            if (b.getAccessCondition() == AccessCondition.ON_INACTIVITY) {
                emailService.sendEmail(
                        b.getLinkedUser().getEmail(),
                        "Legacy Access Granted",
                        "The inactivity protocol for " + user.getUsername() + " has triggered. You may now access the document: " + b.getDocument().getFilename()
                );
            }
        }
    }
}