package net.clgd.ccemux.rendering;

import lombok.EqualsAndHashCode;
import net.clgd.ccemux.init.Config;

/**
 * Contains fields that may be used by {@link RendererFactory}
 * 
 * @author apemanzilla
 *
 */
@EqualsAndHashCode
public class RendererConfig {
	public final double termScale;

	public RendererConfig(double termScale) {
		this.termScale = termScale;
	}

	public RendererConfig(Config generalConfig) {
		this(generalConfig.getTermScale());
	}
}
