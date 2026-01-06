package com.kyberdocs.docs.users;

public enum UserStatus {
    // The normal state. The user is alive, logging in and resetting their heartbeat.
    STATUS_ACTIVE,
    // When the last_heartbeat is approaching the timeout, show a warning.
    STATUS_WARNING,
    // The inactivity timeout has passed. The system has generated the link and emailed the beneficiary.
    STATUS_TRIGGERED,
    // The beneficiary has successfully used the link and downloaded the files.
    STATUS_CLAIMED
}
