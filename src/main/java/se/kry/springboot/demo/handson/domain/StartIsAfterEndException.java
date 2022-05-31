package se.kry.springboot.demo.handson.domain;

import java.time.LocalDateTime;

public class StartIsAfterEndException extends IllegalArgumentException {

  private final LocalDateTime start;
  private final LocalDateTime end;

  public StartIsAfterEndException(LocalDateTime start, LocalDateTime end) {
    super(String.format("Start %s is after endTime %s", start, end));
    this.start = start;
    this.end = end;
  }
}
