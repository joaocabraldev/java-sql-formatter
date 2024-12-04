package com.github.vertical_blank.sqlformatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.vertical_blank.sqlformatter.languages.Dialect;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class PostgreSqlFormatterTest {

  private final SqlFormatter.Formatter formatter = SqlFormatter.of(Dialect.PostgreSql);

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
    String result = formatter.format("SELECT \"foo\", 'bar', $$baz$$ FROM table");
    String expected = "SELECT\n  \"foo\",\n  'bar',\n  $$baz$$\nFROM\n  table";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsBetween() {
    String result = formatter.format("SELECT * FROM table WHERE a BETWEEN 1 AND 10");
    String expected = "SELECT\n  *\nFROM\n  table\nWHERE\n  a BETWEEN 1 AND 10";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsSchema() {
    String result = formatter.format("SELECT * FROM schema.table");
    String expected = "SELECT\n  *\nFROM\n  schema.table";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsOperators() {
    String result =
        formatter.format(
            "SELECT a % b ^ c ! d !! e @ f != g & h | i ~ j # k << l >> m ||/ n |/ o :: p ->> q -> r ~~* s ~~ t !~~* u !~~ v ~* w !~* x !~ y @@ z @@@ aa");
    String expected =
        "SELECT\n  a % b ^ c ! d !! e @ f != g & h | i ~ j # k << l >> m ||/ n |/ o :: p ->> q -> r ~~* s ~~ t !~~* u !~~ v ~* w !~* x !~ y @@ z @@@ aa";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsJoin() {
    String result = formatter.format("SELECT * FROM table1 JOIN table2 ON table1.id = table2.id");
    String expected = "SELECT\n  *\nFROM\n  table1\n  JOIN table2 ON table1.id = table2.id";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsDollarPlaceholders() {
    String result = formatter.format("SELECT $1, $2 FROM tbl");
    String expected = "SELECT\n  $1,\n  $2\nFROM\n  tbl";
    assertEquals(expected, result);
  }

  @Test
  public void testReplacesDollarPlaceholdersWithParamValues() {
    String result =
        formatter.format(
            "SELECT $1, $2 FROM tbl", Map.of("1", "\"variable value\"", "2", "\"blah\""));
    String expected = "SELECT\n  \"variable value\",\n  \"blah\"\nFROM\n  tbl";
    assertEquals(expected, result);
  }

  @Test
  public void testSupportsNamePlaceholders() {
    String result = formatter.format("foo = :bar");
    String expected = "foo = :bar";
    assertEquals(expected, result);
  }

  @Test
  public void testReplacesNamePlaceholdersWithParamValues() {
    String result =
        formatter.format(
            "foo = :bar AND :\"field\" = 10 OR col = :'val'",
            Map.of("bar", "'Hello'", "field", "some_col", "val", 7));
    String expected = "foo = 'Hello'\nAND some_col = 10\nOR col = 7";
    assertEquals(expected, result);
  }
}
