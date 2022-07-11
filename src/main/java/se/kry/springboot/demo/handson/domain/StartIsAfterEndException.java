package se.kry.springboot.demo.handson.domain;

import java.time.LocalDateTime;

public class StartIsAfterEndException extends IllegalArgumentException {

  private final LocalDateTime startTime;
  private final LocalDateTime endTime;

  public StartIsAfterEndException(LocalDateTime startTime, LocalDateTime endTime) {
    super(String.format("Start time %s is after end time %s", startTime, endTime));
    this.startTime = startTime;
    this.endTime = endTime;
  }
}
