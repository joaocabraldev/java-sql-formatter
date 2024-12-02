package com.github.vertical_blank.sqlformatter;

import static org.junit.jupiter.api.Assertions.*;

import com.github.vertical_blank.sqlformatter.languages.Dialect;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SqlFormatterTest {

  @Test
  void simple() {
    final String actual =
        SqlFormatter.format(
            "SELECT foo, bar, CASE baz WHEN 'one' THEN 1 WHEN 'two' THEN 2 ELSE 3 END FROM table");
    assertEquals(
        "SELECT\n"
            + "  foo,\n"
            + "  bar,\n"
            + "  CASE\n"
            + "    baz\n"
            + "    WHEN 'one' THEN 1\n"
            + "    WHEN 'two' THEN 2\n"
            + "    ELSE 3\n"
            + "  END\n"
            + "FROM\n"
            + "  table",
        actual);
  }

  @Test
  void withIndent() {
    final String actual =
        SqlFormatter.format(
            "SELECT foo, bar, CASE baz WHEN 'one' THEN 1 WHEN 'two' THEN 2 ELSE 3 END FROM table",
            "    ");
    assertEquals(
        "SELECT\n"
            + "    foo,\n"
            + "    bar,\n"
            + "    CASE\n"
            + "        baz\n"
            + "        WHEN 'one' THEN 1\n"
            + "        WHEN 'two' THEN 2\n"
            + "        ELSE 3\n"
            + "    END\n"
            + "FROM\n"
            + "    table",
        actual);
  }

  @Test
  void withNamedParams() {
    final Map<String, String> namedParams = new HashMap<>();
    namedParams.put("foo", "'bar'");

    final String actual =
        SqlFormatter.of(Dialect.TSql).format("SELECT * FROM tbl WHERE foo = @foo", namedParams);
    assertEquals("SELECT\n" + "  *\n" + "FROM\n" + "  tbl\n" + "WHERE\n" + "  foo = 'bar'", actual);
  }

  @Test
  void withFatArrow() {
    final String actual =
        SqlFormatter.extend(config -> config.plusOperators("=>"))
            .format("SELECT * FROM tbl WHERE foo => '123'");
    assertEquals(
        "SELECT\n" + "  *\n" + "FROM\n" + "  tbl\n" + "WHERE\n" + "  foo => '123'", actual);
  }

  @Test
  void withIndexedParams() {
    final String actual =
        SqlFormatter.format("SELECT * FROM tbl WHERE foo = ?", Arrays.asList("'bar'"));
    assertEquals("SELECT\n" + "  *\n" + "FROM\n" + "  tbl\n" + "WHERE\n" + "  foo = 'bar'", actual);
  }

  @Test
  void withLambdasParams() {
    final String actual =
        SqlFormatter.of(Dialect.SparkSql)
            .format("SELECT aggregate(array(1, 2, 3), 0, (acc, x) -> acc + x, acc -> acc * 10);");
    assertEquals(
        "SELECT\n" + "  aggregate(array(1, 2, 3), 0, (acc, x) -> acc + x, acc -> acc * 10);",
        actual);
  }

  @Test
  void withNotEquals() {
    final String actual = SqlFormatter.format("SELECT * FROM TEST WHERE ABC != '4'");
    assertEquals("SELECT\n" + "  *\n" + "FROM\n" + "  TEST\n" + "WHERE\n" + "  ABC != '4'", actual);
  }

  @Test
  void doesNothingWithEmptyInput() {
    final String actual = SqlFormatter.format("");
    assertEquals("", actual);
  }

  @Test
  void formatsLonelySemicolon() {
    final String actual = SqlFormatter.format(";");
    assertEquals(";", actual);
  }

  @Test
  void formatsSimpleSelectQuery() {
    final String actual = SqlFormatter.format("SELECT count(*),Column1 FROM Table1;");
    assertEquals("SELECT\n  count(*),\n  Column1\nFROM\n  Table1;", actual);
  }

  @Test
  void formatsComplexSelect() {
    final String actual =
        SqlFormatter.format(
            "SELECT DISTINCT name, ROUND(age/7) field1, 18 + 20 AS field2, 'some string' FROM foo;");
    assertEquals(
        "SELECT\n  DISTINCT name,\n  ROUND(age / 7) field1,\n  18 + 20 AS field2,\n  'some string'\nFROM\n  foo;",
        actual);
  }

  @Test
  void formatsSelectWithComplexWhere() {
    final String actual =
        SqlFormatter.format(
            "SELECT * FROM foo WHERE Column1 = 'testing'\nAND ( (Column2 = Column3 OR Column4 >= NOW()) );");
    assertEquals(
        "SELECT\n  *\nFROM\n  foo\nWHERE\n  Column1 = 'testing'\n  AND (\n    (\n      Column2 = Column3\n      OR Column4 >= NOW()\n    )\n  );",
        actual);
  }

  @Test
  void formatsSelectWithTopLevelReservedWords() {
    final String actual =
        SqlFormatter.format(
            "SELECT * FROM foo WHERE name = 'John' GROUP BY some_column\nHAVING column > 10 ORDER BY other_column LIMIT 5;");
    assertEquals(
        "SELECT\n  *\nFROM\n  foo\nWHERE\n  name = 'John'\nGROUP BY\n  some_column\nHAVING\n  column > 10\nORDER BY\n  other_column\nLIMIT\n  5;",
        actual);
  }

  @Test
  void formatsLimitWithTwoCommaSeparatedValuesOnSingleLine() {
    final String actual = SqlFormatter.format("LIMIT 5, 10;");
    assertEquals("LIMIT\n  5, 10;", actual);
  }

  @Test
  void formatsLimitOfSingleValueFollowedByAnotherSelectUsingCommas() {
    final String actual = SqlFormatter.format("LIMIT 5; SELECT foo, bar;");
    assertEquals("LIMIT\n  5;\nSELECT\n  foo,\n  bar;", actual);
  }

  @Test
  void formatsLimitOfSingleValueAndOffset() {
    final String actual = SqlFormatter.format("LIMIT 5 OFFSET 8;");
    assertEquals("LIMIT\n  5 OFFSET 8;", actual);
  }

  @Test
  void recognizesLimitInLowercase() {
    final String actual = SqlFormatter.format("limit 5, 10;");
    assertEquals("limit\n  5, 10;", actual);
  }

  @Test
  void preservesCaseOfKeywords() {
    final String actual = SqlFormatter.format("select distinct * frOM foo WHERe a > 1 and b = 3");
    assertEquals("select\n  distinct *\nfrOM\n  foo\nWHERe\n  a > 1\n  and b = 3", actual);
  }

  @Test
  void formatsSelectQueryWithSelectQueryInsideIt() {
    final String actual =
        SqlFormatter.format(
            "SELECT *, SUM(*) AS sum FROM (SELECT * FROM Posts LIMIT 30) WHERE a > b");
    assertEquals(
        "SELECT\n  *,\n  SUM(*) AS sum\nFROM\n  (\n    SELECT\n      *\n    FROM\n      Posts\n    LIMIT\n      30\n  )\nWHERE\n  a > b",
        actual);
  }

  @Test
  void formatsSimpleInsertQuery() {
    final String actual =
        SqlFormatter.format(
            "INSERT INTO Customers (ID, MoneyBalance, Address, City) VALUES (12,-123.4, 'Skagen 2111','Stv');");
    assertEquals(
        "INSERT INTO\n  Customers (ID, MoneyBalance, Address, City)\nVALUES\n  (12, -123.4, 'Skagen 2111', 'Stv');",
        actual);
  }

  @Test
  void formatsOpenParenAfterComma() {
    final String actual =
        SqlFormatter.format(
            "WITH TestIds AS (VALUES (4),(5), (6),(7),(9),(10),(11)) SELECT * FROM TestIds;");
    assertEquals(
        "WITH TestIds AS (\n  VALUES\n    (4),\n    (5),\n    (6),\n    (7),\n    (9),\n    (10),\n    (11)\n)\nSELECT\n  *\nFROM\n  TestIds;",
        actual);
  }

  @Test
  void keepsShortParenthesizedListWithNestedParenthesisOnSingleLine() {
    final String actual = SqlFormatter.format("SELECT (a + b * (c - NOW()));");
    assertEquals("SELECT\n  (a + b * (c - NOW()));", actual);
  }

  @Test
  void breaksLongParenthesizedListsToMultipleLines() {
    final String actual =
        SqlFormatter.format(
            "INSERT INTO some_table (id_product, id_shop, id_currency, id_country, id_registration) (\nSELECT IF(dq.id_discounter_shopping = 2, dq.value, dq.value / 100),\nIF (dq.id_discounter_shopping = 2, 'amount', 'percentage') FROM foo);");
    assertEquals(
        "INSERT INTO\n  some_table (\n    id_product,\n    id_shop,\n    id_currency,\n    id_country,\n    id_registration\n  ) (\n    SELECT\n      IF(\n        dq.id_discounter_shopping = 2,\n        dq.value,\n        dq.value / 100\n      ),\n      IF (\n        dq.id_discounter_shopping = 2,\n        'amount',\n        'percentage'\n      )\n    FROM\n      foo\n  );",
        actual);
  }

  @Test
  void formatsSimpleUpdateQuery() {
    final String actual =
        SqlFormatter.format(
            "UPDATE Customers SET ContactName='Alfred Schmidt', City='Hamburg' WHERE CustomerName='Alfreds Futterkiste';");
    assertEquals(
        "UPDATE\n  Customers\nSET\n  ContactName = 'Alfred Schmidt',\n  City = 'Hamburg'\nWHERE\n  CustomerName = 'Alfreds Futterkiste';",
        actual);
  }

  @Test
  void formatsSimpleDeleteQuery() {
    final String actual =
        SqlFormatter.format("DELETE FROM Customers WHERE CustomerName='Alfred' AND Phone=5002132;");
    assertEquals(
        "DELETE FROM\n  Customers\nWHERE\n  CustomerName = 'Alfred'\n  AND Phone = 5002132;",
        actual);
  }

  @Test
  void formatsSimpleDropQuery() {
    final String actual = SqlFormatter.format("DROP TABLE IF EXISTS admin_role;");
    assertEquals("DROP TABLE IF EXISTS admin_role;", actual);
  }

  @Test
  void formatsIncompleteQuery() {
    final String actual = SqlFormatter.format("SELECT count(");
    assertEquals("SELECT\n  count(", actual);
  }

  @Test
  void formatsUpdateQueryWithAsPart() {
    final String actual =
        SqlFormatter.format(
            "UPDATE customers SET total_orders = order_summary.total  FROM ( SELECT * FROM bank) AS order_summary");
    assertEquals(
        "UPDATE\n  customers\nSET\n  total_orders = order_summary.total\nFROM\n  (\n    SELECT\n      *\n    FROM\n      bank\n  ) AS order_summary",
        actual);
  }

  @Test
  void formatsTopLevelAndNewlineMultiWordReservedWordsWithInconsistentSpacing() {
    final String actual =
        SqlFormatter.format("SELECT * FROM foo LEFT \t   \n JOIN bar ORDER \n BY blah");
    assertEquals("SELECT\n  *\nFROM\n  foo\n  LEFT JOIN bar\nORDER BY\n  blah", actual);
  }

  @Test
  void formatsLongDoubleParenthizedQueriesToMultipleLines() {
    final String actual =
        SqlFormatter.format("((foo = '0123456789-0123456789-0123456789-0123456789'))");
    assertEquals("(\n  (\n    foo = '0123456789-0123456789-0123456789-0123456789'\n  )\n)", actual);
  }

  @Test
  void formatsShortDoubleParenthizedQueriesToOneLine() {
    final String actual = SqlFormatter.format("((foo = 'bar'))");
    assertEquals("((foo = 'bar'))", actual);
  }

  @Test
  void formatsLogicalOperators() {
    assertEquals("foo ALL bar", SqlFormatter.format("foo ALL bar"));
    assertEquals("foo = ANY (1, 2, 3)", SqlFormatter.format("foo = ANY (1, 2, 3)"));
    assertEquals("EXISTS bar", SqlFormatter.format("EXISTS bar"));
    assertEquals("foo IN (1, 2, 3)", SqlFormatter.format("foo IN (1, 2, 3)"));
    assertEquals("foo LIKE 'hello%'", SqlFormatter.format("foo LIKE 'hello%'"));
    assertEquals("foo IS NULL", SqlFormatter.format("foo IS NULL"));
    assertEquals("UNIQUE foo", SqlFormatter.format("UNIQUE foo"));
  }

  @Test
  void formatsAndOrOperators() {
    assertEquals("foo\nAND bar", SqlFormatter.format("foo AND bar"));
    assertEquals("foo\nOR bar", SqlFormatter.format("foo OR bar"));
  }

  @Test
  void keepsSeparationBetweenMultipleStatements() {
    assertEquals("foo;\nbar;", SqlFormatter.format("foo;bar;"));
    assertEquals("foo;\nbar;", SqlFormatter.format("foo\n;bar;"));
    assertEquals("foo;\nbar;", SqlFormatter.format("foo\n\n\n;bar;\n\n"));

    final String actual =
        SqlFormatter.format(
            "SELECT count(*),Column1 FROM Table1;\nSELECT count(*),Column1 FROM Table2;");
    assertEquals(
        "SELECT\n  count(*),\n  Column1\nFROM\n  Table1;\nSELECT\n  count(*),\n  Column1\nFROM\n  Table2;",
        actual);
  }

  @Test
  void formatsUnicodeCorrectly() {
    final String actual = SqlFormatter.format("SELECT 结合使用, тест FROM table;");
    assertEquals("SELECT\n  结合使用,\n  тест\nFROM\n  table;", actual);
  }

  @Test
  void correctlyIndentsCreateStatementAfterSelect() {
    final String actual =
        SqlFormatter.format(
            "SELECT * FROM test;\nCREATE TABLE TEST(id NUMBER NOT NULL, col1 VARCHAR2(20), col2 VARCHAR2(20));");
    assertEquals(
        "SELECT\n  *\nFROM\n  test;\nCREATE TABLE TEST(\n  id NUMBER NOT NULL,\n  col1 VARCHAR2(20),\n  col2 VARCHAR2(20)\n);",
        actual);
  }

  @Test
  void correctlyHandlesFloatsAsSingleTokens() {
    final String actual =
        SqlFormatter.format("SELECT 1e-9 AS a, 1.5e-10 AS b, 3.5E12 AS c, 3.5e12 AS d;");
    assertEquals("SELECT\n  1e-9 AS a,\n  1.5e-10 AS b,\n  3.5E12 AS c,\n  3.5e12 AS d;", actual);
  }

  @Test
  void doesNotSplitUnionAllInHalf() {
    final String actual = SqlFormatter.format("SELECT * FROM tbl1\nUNION ALL\nSELECT * FROM tbl2;");
    assertEquals("SELECT\n  *\nFROM\n  tbl1\nUNION ALL\nSELECT\n  *\nFROM\n  tbl2;", actual);
  }
}
