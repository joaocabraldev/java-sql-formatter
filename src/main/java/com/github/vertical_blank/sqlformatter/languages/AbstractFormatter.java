package com.github.vertical_blank.sqlformatter.languages;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.github.vertical_blank.sqlformatter.core.DialectConfig;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.core.Formatter;
import com.github.vertical_blank.sqlformatter.core.Params;

public class AbstractFormatter implements Function<FormatConfig, Formatter> {

	private final Function<FormatConfig, Formatter> underlying;

	public AbstractFormatter(Function<FormatConfig, Formatter> underlying) {
		this.underlying = underlying;
	}

	@Override
	public Formatter apply(FormatConfig t) {
		return underlying.apply(t);
	}

	public AbstractFormatter extend(UnaryOperator<DialectConfig> operator) {
		return new AbstractFormatter(cfg -> 
			new Formatter(cfg) {
				@Override
				public DialectConfig dialectConfig() {
					return operator.apply(
						AbstractFormatter.this.underlying.apply(cfg).dialectConfig());
				}
			}
		);
	}

	public String format(String query, FormatConfig cfg) {
		return this.apply(cfg).format(query);
	}

	public String format(String query, String indent, List<?> params) {
		return format(
						query,
						FormatConfig.builder()
										.indent(indent)
										.params(Params.Holder.of(params))
										.build());
	}
	public String format(String query, List<?> params) {
		return format(
						query,
						FormatConfig.builder()
										.params(Params.Holder.of(params))
										.build());
	}

	public String format(String query, String indent, Map<String, ?> params) {
		return format(
						query,
						FormatConfig.builder()
										.indent(indent)
										.params(Params.Holder.of(params))
										.build());
	}
	public String format(String query, Map<String, ?> params) {
		return format(
						query,
						FormatConfig.builder()
										.params(Params.Holder.of(params))
										.build());
	}

	public String format(String query, String indent) {
		return format(
						query,
						FormatConfig.builder()
										.indent(indent)
										.build());
	}
	public String format(String query) {
		return format(
						query,
						FormatConfig.builder().build());
	}

}
