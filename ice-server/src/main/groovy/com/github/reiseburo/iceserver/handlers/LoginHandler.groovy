package com.github.reiseburo.iceserver.handlers

import asia.stampy.server.listener.login.NotLoggedInException
import asia.stampy.server.listener.login.StampyLoginHandler
import asia.stampy.server.listener.login.TerminateSessionException

/**
 */
class LoginHandler implements StampyLoginHandler {
    static final String SEE_THE_SYSTEM_ADMINISTRATOR = "See the system administrator"
    static final String GOOD_USER = "gooduser"
    static final String BAD_USER = "baduser"
    private int maxFailedLoginAttempts = 3
    private int failedLoginAttempts = 0

    @Override
    void login(String username, String password) throws NotLoggedInException, TerminateSessionException {
        if (GOOD_USER.equals(username))
            return

        failedLoginAttempts++

        if (failedLoginAttempts >= getMaxFailedLoginAttempts()) {
            throw new TerminateSessionException(SEE_THE_SYSTEM_ADMINISTRATOR)
        }

        throw new NotLoggedInException("Username " + username + " cannot be logged in")
    }

    int getMaxFailedLoginAttempts() {
        return maxFailedLoginAttempts
    }

    void setMaxFailedLoginAttempts(int maxFailedLoginAttempts) {
        this.maxFailedLoginAttempts = maxFailedLoginAttempts
    }
}
