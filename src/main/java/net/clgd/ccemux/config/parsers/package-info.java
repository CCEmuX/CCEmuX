/**
 * This package contains several classes and interfaces used when parsing config
 * options, most notably the {@link net.clgd.ccemux.config.parsers.Parser
 * Parser} interface, implementations of which are used to parse config options
 * in {@link net.clgd.ccemux.config.Config#bindConfigOptions(java.util.Map)
 * Config.bindConfigOptions}.<br />
 * <br />
 * Multiple reference implementations of <code>Parser<code> are also provided
 * for boxed types or basic data objects.
 * 
 * @see net.clgd.ccemux.config.parsers.Parser Parser
 * @see net.clgd.ccemux.config.Config Config
 * @see net.clgd.ccemux.config.parsers.BooleanParser BooleanParser
 * @see net.clgd.ccemux.config.parsers.DoubleParser DoubleParser
 * @see net.clgd.ccemux.config.parsers.FloatParser FloatParser
 * @see net.clgd.ccemux.config.parsers.IntegerParser IntegerParser
 * @see net.clgd.ccemux.config.parsers.LongParser LongParser
 * @see net.clgd.ccemux.config.parsers.PathParser PathParser
 * @see net.clgd.ccemux.config.parsers.StringParser StringParser
 * @see net.clgd.ccemux.config.parsers.URLParser URLParser
 */
package net.clgd.ccemux.config.parsers;