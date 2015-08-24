package com.github.reiseburo.iceserver

import asia.stampy.server.listener.subscription.StampyAcknowledgementHandler

/**
 * Created by tyler on 8/23/15.
 */
class SystemAcknowledgementHandler implements StampyAcknowledgementHandler {

  /*
   * (non-Javadoc)
   *
   * @see
   * asia.stampy.server.mina.subscription.StampyAcknowledgementHandler#ackReceived
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void ackReceived(String id, String receipt, String transaction) throws Exception {

  }

  /*
   * (non-Javadoc)
   *
   * @see
   * asia.stampy.server.mina.subscription.StampyAcknowledgementHandler#nackReceived
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void nackReceived(String id, String receipt, String transaction) throws Exception {

  }

  /*
   * (non-Javadoc)
   *
   * @see asia.stampy.server.mina.subscription.StampyAcknowledgementHandler#
   * noAcknowledgementReceived(java.lang.String)
   */
  @Override
  public void noAcknowledgementReceived(String id) {
    System.out.println("No acknowledgement received for " + id);
  }
}
