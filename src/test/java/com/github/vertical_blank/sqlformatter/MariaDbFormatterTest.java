package com.github.vertical_blank.sqlformatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.vertical_blank.sqlformatter.languages.Dialect;
import org.junit.jupiter.api.Test;

class MariaDbFormatterTest {

  private final SqlFormatter.Formatter formatter = SqlFormatter.of(Dialect.MariaDb);

  @Test
  void supportsHashComments() {
    final String actual = this.formatter.format("SELECT a # comment\nFROM b # comment");
    final String expected = "SELECT\n  a # comment\nFROM\n  b # comment";
    assertEquals(expected, actual);
  }

  @Test
  void supportsAtVariables() {
    final String actual = this.formatter.format("SELECT @foo, @bar");
    final String expected = "SELECT\n  @foo,\n  @bar";
    assertEquals(expected, actual);
  }

  @Test
  void supportsSettingVariables() {
    final String actual = this.formatter.format("SET @foo := (SELECT * FROM tbl);");
    final String expected = "SET\n  @foo := (\n    SELECT\n      *\n    FROM\n      tbl\n  );";
    assertEquals(expected, actual);
  }
}
