package com.github.vertical_blank.sqlformatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.vertical_blank.sqlformatter.languages.Dialect;
import org.junit.jupiter.api.Test;

public class MariaDbFormatterTest {

  private final SqlFormatter.Formatter formatter = SqlFormatter.of(Dialect.MariaDb);

  @Test
  public void testSupportsCase() {
    String result = formatter.format("CASE WHEN a THEN b ELSE c END");
    String expected = "CASE\n  WHEN a THEN b\n  ELSE c\nEND";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsCreateTable() {
    String result = formatter.format("CREATE TABLE foo (id INT PRIMARY KEY, name VARCHAR(100))");
    String expected = "CREATE TABLE foo (id INT PRIMARY KEY, name VARCHAR(100))";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsAlterTable() {
    String result = formatter.format("ALTER TABLE foo ADD COLUMN bar INT");
    String expected = "ALTER TABLE\n  foo\nADD\n  COLUMN bar INT";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsStrings() {
    String result = formatter.format("SELECT \"foo\", 'bar', `baz` FROM table");
    String expected = "SELECT\n  \"foo\",\n  'bar',\n  `baz`\nFROM\n  table";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsBetween() {
    String result = formatter.format("SELECT * FROM table WHERE a BETWEEN 1 AND 10");
    String expected = "SELECT\n  *\nFROM\n  table\nWHERE\n  a BETWEEN 1 AND 10";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsOperators() {
    String result =
        formatter.format("SELECT a % b & c | d ^ e ~ f != g ! h <=> i << j >> k && l || m := n");
    String expected = "SELECT\n  a % b & c | d ^ e ~ f != g ! h <=> i << j >> k && l || m := n";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsJoin() {
    String result =
        formatter.format("SELECT * FROM table1 STRAIGHT_JOIN table2 NATURAL LEFT JOIN table3");
    String expected =
        "SELECT\n  *\nFROM\n  table1\n  STRAIGHT_JOIN table2\n  NATURAL LEFT JOIN table3";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsHashComments() {
    String result = formatter.format("SELECT a # comment\nFROM b # comment");
    String expected = "SELECT\n  a # comment\nFROM\n  b # comment";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsVariables() {
    String result = formatter.format("SELECT @foo, @bar");
    String expected = "SELECT\n  @foo,\n  @bar";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsSettingVariables() {
    String result = formatter.format("SET @foo := (SELECT * FROM tbl);");
    String expected = "SET\n  @foo := (\n    SELECT\n      *\n    FROM\n      tbl\n  );";
    assertEquals(expected, result);
  }
}
